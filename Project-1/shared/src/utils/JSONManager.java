package utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JSONManager {

    //
    //  takes String and returns pretty printable String
    //
    public static String printJSON(String s) {
        String prettyJsonString = null;
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JSONParser jp = new JSONParser();
            Object je =jp.parse(s);
            prettyJsonString = gson.toJson(je);
            System.out.println(prettyJsonString);
        }catch (ParseException e) {
            e.printStackTrace();
        }

        return prettyJsonString;
    }
}
