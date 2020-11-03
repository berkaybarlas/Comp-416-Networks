package owm;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class ConnectToOWM {
    public String GLOBAL_URL = "https://api.openweathermap.org/";
    public String APPID = "78f6fce93c7671e98bd7e6d954ae3ad3";
    private URL url;
    private HttpURLConnection con;

    private String constructedURL;
    protected double lat;
    protected double lon;

    public ConnectToOWM() {
    }

    //
    //    @param takes the cityID as long
    //    return: gives latitude and longitude
    //
    public void getLatLon(long cityID) {

        //JSON parser object to parse read file
        JSONParser parser = new JSONParser();

        String let = null;
        try (FileReader reader = new FileReader("city.list.json")) {
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

    //
    //    function to get current, daily and minutely weather report
    //    @param citId
    //    @param reqType can take current, daily and minutely
    //    returns weather report as String
    //
    public String getCityWheather(String cityId, String reqType) throws IOException {
        Long cityIdLong = Long.parseLong(cityId);
        getLatLon(cityIdLong);
        if (reqType == "current") {
            constructedURL = GLOBAL_URL + "data/2.5/onecall?lat=" + lat + "&lon=" + lon + "&exclude=daily,hourly,minutely,alerts&appid=" + APPID;
        } else if (reqType == "daily") {
            constructedURL = GLOBAL_URL + "data/2.5/onecall?lat=" + lat + "&lon=" + lon + "&exclude=current,hourly,minutely,alerts&appid=" + APPID;
        } else if (reqType == "minutely") {
            constructedURL = GLOBAL_URL + "data/2.5/onecall?lat=" + lat + "&lon=" + lon + "&exclude=daily,hourly,current,alerts&appid=" + APPID;
        }
        return ConnectToOWM(constructedURL);
    }

    //
    //    function to get max 5 days of weather history
    //    @param cityId
    //    @param number of days for historical weather report (max 5)
    //    returns historical weather report as String - most current to least current
    //
    public String getCityWheatherHistory(String cityId, int day) throws IOException {

        String returnVal = "";
        Long cityIdLong = Long.parseLong(cityId);
        getLatLon(cityIdLong);
        long ut2 = System.currentTimeMillis() / 1000L;
        int histDays=5;

        if(day<5)
        {
            histDays = day;
        }

        for (int i = 0; i < histDays; i++) {
            ut2 -= 86400;
            constructedURL = GLOBAL_URL + "data/2.5/onecall/timemachine?lat=" + lat + "&lon=" + lon + "&dt=" + ut2 + "&appid=" + APPID;
            returnVal += ConnectToOWM(constructedURL);
        }
        return returnVal;
    }

    //
    //    function to get weather map as image
    //    @param cityId
    //    @param mapType can take "clouds_new", "precipitation_new", "pressure_new", "wind_new", "temp_new"
    //    saves weather map at zoom level of 7 as png file
    //
    public void getCityWeatherMap(String cityId, String mapType) {

        Long cityIdLong = Long.parseLong(cityId);
        getLatLon(cityIdLong);
        String url = "https://tile.openweathermap.org/map/";

        int x = (int) ((180 + lon) * 128 / 360);
        int y = (int) ((85 - lat) * 128 / 180);

        constructedURL = url + mapType + "/7/" + x + "/" + y + ".png?appid=" + APPID;

        try {

            URL urll = new URL(constructedURL);
            InputStream is = urll.openStream();
            OutputStream os = new FileOutputStream("image.png");

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
            System.err.println("Error: no server has been found ");
        }

    }

    //
    //    connection to OWM server
    //    used in getCityWheather and getCityWheatherHistory
    //    @param u URL for server connection
    //    returns String from server
    //
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

}