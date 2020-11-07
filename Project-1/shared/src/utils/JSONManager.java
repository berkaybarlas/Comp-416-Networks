package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class JSONManager {

    //
    //  takes String and returns pretty printable String
    //
    public static String prettyJSON(String s) {
        String prettyJsonString = null;
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JSONParser jp = new JSONParser();
            Object je = jp.parse(s);
            prettyJsonString = gson.toJson(je);
            System.out.println(prettyJsonString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return prettyJsonString;
    }

    //takes file name of json file and prints it pretty
    public void printJSON(String FileName) throws IOException {

        String s = "";

        try (FileReader reader = new FileReader(FileName)) {

            int i;
            while ((i = reader.read()) != -1) {
                s += (char) i;
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String prettyJ = prettyJSON(s);
        System.out.println(prettyJ);
    }

}
