package owm;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import utils.RequestType;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

public class OWMManager {
    private String SERVER_RESOURCE_PATH = "/server/src/resources/";
    private String SERVER_DOWNLOAD_PATH = "/server/downloads/";
    private String CITY_LIST = "city.list.json";

    private String GLOBAL_URL = "https://api.openweathermap.org/";
    private String OWM_MAP_URL = "https://tile.openweathermap.org/map/";
    private String APPID = "78f6fce93c7671e98bd7e6d954ae3ad3";

    private String constructedURL;
    protected HashMap<Object,double[]>map;
    protected double lat;
    protected double lon;

    public OWMManager() {
        initDownloadPath();
        map = createCityIdCoordinateHashMap();
    }

    public String requestData(RequestType requestType, String[] params) throws RequestError{
        String dataLocation = "";
        if (params.length < 1) {
            throw new RequestError("Missing params");
        }
        try {
            Long cityIdLong;
            try {
                cityIdLong = Long.parseLong(params[0]);
            } catch (NumberFormatException e) {
                throw new RequestError("CityId must be an integer");
            }
            if (map.get(cityIdLong) == null) {
                throw new RequestError("CityId is wrong or doesn't exists");
            }
            switch (requestType) {
                case CURRENT:
                    dataLocation = this.getCityWheather("current", cityIdLong);
                    break;
                case DAILY:
                    dataLocation = this.getCityWheather("daily", cityIdLong);
                    break;
                case MINUTELY:
                    dataLocation = this.getCityWheather("minutely", cityIdLong);
                    break;
                case HISTORY:
                    if (params.length < 2) {
                        throw new RequestError("Missing params");
                    }
                    int day = 0;
                    try {
                        day = Integer.parseInt(params[1]);
                    } catch (NumberFormatException e) {
                        throw new RequestError("Second parameter must be integer");
                    }

                    dataLocation = this.getCityWheatherHistory(cityIdLong, day);

                    break;
                case MAP:
                    if (params.length < 2) {
                        throw new RequestError("Missing params");
                    }
                    dataLocation = this.getCityWeatherMap(cityIdLong, params[1]);
                    break;
                default:
                    throw new RequestError("Undefined request type");
            }
        } catch (IOException e) {
            throw new RequestError("Internal error during request");
        }

        return dataLocation;
    }

    /**
    *    function to get current, daily and minutely weather report
    *    @param cityIdLong
    *    @param reqType can take current, daily and minutely
    *    returns weather report as String
    */
    public String getCityWheather(String reqType, Long cityIdLong) throws IOException {

        lon=map.get(cityIdLong)[0];
        lat=map.get(cityIdLong)[1];

        String localDir = System.getProperty("user.dir");

        String fileName = String.format("%s-%s-%d.json", cityIdLong, reqType, System.currentTimeMillis());
        String fileLocation = localDir + SERVER_DOWNLOAD_PATH + fileName;

        if (reqType == "current") {
            constructedURL = GLOBAL_URL + "data/2.5/onecall?lat=" + lat + "&lon=" + lon + "&exclude=daily,hourly,minutely,alerts&appid=" + APPID;
        } else if (reqType == "daily") {
            constructedURL = GLOBAL_URL + "data/2.5/onecall?lat=" + lat + "&lon=" + lon + "&exclude=current,hourly,minutely,alerts&appid=" + APPID;
        } else if (reqType == "minutely") {
            constructedURL = GLOBAL_URL + "data/2.5/onecall?lat=" + lat + "&lon=" + lon + "&exclude=daily,hourly,current,alerts&appid=" + APPID;
        }
        createJSON(ConnectToOWM(constructedURL), fileLocation);
        return fileLocation;

    }

    /**
    **    function to get max 5 days of weather history
    **    @param cityIdLong
    **    @param day of days for historical weather report (max 5)
    **    returns historical weather report as String - most current to least current
    */
    public String getCityWheatherHistory(Long cityIdLong, int day) throws IOException {

        JSONObject days=new JSONObject();
        String returnVal = "";

        lon=map.get(cityIdLong)[0];
        lat=map.get(cityIdLong)[1];

        long ut2 = System.currentTimeMillis() / 1000L;
        int histDays = 5;

        String localDir = System.getProperty("user.dir");

        String fileName = String.format("%s-history-%s-%d.json", cityIdLong, day, System.currentTimeMillis());
        String fileLocation = localDir + SERVER_DOWNLOAD_PATH + fileName;


        if (day < 5) {
            histDays = day;
        }

        for (int i = 0; i < histDays; i++) {
            ut2 -= 86400;
            constructedURL = GLOBAL_URL + "data/2.5/onecall/timemachine?lat=" + lat + "&lon=" + lon + "&dt=" + ut2 + "&appid=" + APPID;
             days.put(++i, ConnectToOWM(constructedURL));
        }
        returnVal=days.toJSONString();
        createJSON(returnVal, fileLocation);
        return fileLocation;
    }

    /**
     * function to get weather map as image
     *
     * @param cityIdLong
     * @param mapType can take "clouds_new", "precipitation_new", "pressure_new", "wind_new", "temp_new"
     *                saves weather map at zoom level of 7 as png file
     */
    public String getCityWeatherMap(Long cityIdLong, String mapType) {

        lon=map.get(cityIdLong)[0];
        lat=map.get(cityIdLong)[1];

        String localDir = System.getProperty("user.dir");

        String imageName = String.format("%s-%s-image-%d.png", cityIdLong, mapType, System.currentTimeMillis());
        String imageLocation = localDir + SERVER_DOWNLOAD_PATH + imageName;

        int x = (int) ((180 + lon) * 128 / 360);
        int y = (int) ((85 - lat) * 128 / 180);

        constructedURL = OWM_MAP_URL + mapType + "/7/" + x + "/" + y + ".png?appid=" + APPID;

        try {
            URL url = new URL(constructedURL);
            InputStream is = url.openStream();


            OutputStream os = new FileOutputStream(imageLocation);

            byte[] b = new byte[2048];
            int length;

            while ((length = is.read(b)) != -1) {
                os.write(b, 0, length);
            }

            is.close();
            os.close();

        } catch (MalformedURLException malformedURLException) {
            malformedURLException.printStackTrace();

        } catch (IOException e) {
            //e.printStackTrace();
            System.err.println("Error: File IO error in OWM");
        }
        return imageLocation;
    }

    /**
    *    connection to OWM server
    *    used in getCityWheather and getCityWheatherHistory
    *    @param u URL for server connection
    *    returns String from server
    */
    public String ConnectToOWM(String u) throws IOException {

        String returnLine = "";

        try {

            URL url = new URL(u);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
                returnLine += inputLine;
            }

            in.close();
            con.disconnect();

        } catch (IOException e) {
            //e.printStackTrace();
            System.err.println("Error: no server has been found ");
        }

        return returnLine;
    }

    /**
     * Initialize download path for files
     */
    private void initDownloadPath() {
        String localDir = System.getProperty("user.dir");
        File directory = new File(localDir + SERVER_DOWNLOAD_PATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    /**
     * @param data the weather information
     * @param fileLocation writes is into a file
     */
    private void createJSON(String data, String fileLocation) {
        try (FileWriter file = new FileWriter(fileLocation)) {

            file.write(data);
            file.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private HashMap createCityIdCoordinateHashMap(){
        JSONParser parser = new JSONParser();
        HashMap<Object,double[]> map=new HashMap<Object,double[]>();
        String localDir = System.getProperty("user.dir");

        try (FileReader reader = new FileReader(localDir + SERVER_RESOURCE_PATH + CITY_LIST)) {
            //Read JSON fileJson
            JSONArray obj = (JSONArray) parser.parse(reader);

            for (int i = 0; i < obj.size(); i++) {
                JSONObject obj1 = (JSONObject) obj.get(i);

                JSONObject co = (JSONObject) obj1.get("coord");
                double[] array1=new double[]{(double) co.get("lon"), (double) co.get("lat")};
                map.put(obj1.get("id"),array1);
            }


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * Deprecated
     *    @param cityID the cityID as long
     *    return: gives latitude and longitude
     */
    public void getLatLon(long cityID) {

        //JSON parser object to parse read file
        JSONParser parser = new JSONParser();
        String localDir = System.getProperty("user.dir");

        // TODO instead of doing create a hashmap <cityId, location>
        try (FileReader reader = new FileReader(localDir + SERVER_RESOURCE_PATH + CITY_LIST)) {
            //Read JSON fileJso
            JSONArray obj = (JSONArray) parser.parse(reader);

            for (int i = 0; i < obj.size(); i++) {
                JSONObject obj1 = (JSONObject) obj.get(i);

                if (obj1.get("id").equals(cityID)) {
                    JSONObject co = (JSONObject) obj1.get("coord");
                    lon = (double) co.get("lon");
                    lat = (double) co.get("lat");
                    break;
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

}
