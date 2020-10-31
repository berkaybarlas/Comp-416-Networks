
import java.io.File;
import java.util.*;
import java.io.FileNotFoundException;

public class QuestionsAndAnswers
{
    public static HashMap<String, HashMap<String, String>> retrieve()
    {
        HashMap<String, HashMap<String, String>> result = new HashMap<String, HashMap<String, String>>();
        int lineCounter = 0;
        String prevLine = "";
        String name = "";
        try
        {
            File qa = new File("/Users/baranhokelek/IdeaProjects/Comp-416-Networks/Project-1/server/src/questions_answers.txt");
            Scanner s = new Scanner(qa);
            while (s.hasNextLine())
            {
                String str = s.nextLine();
                if (lineCounter == 0 || prevLine.equals(""))
                {
                    name = str;
                    result.put(str, new HashMap<String, String>());
                }
                else if (str.equals(""))
                {
                    result.get(name).put(prevLine, "Success");
                }
                else
                {
                    result.get(name).put(prevLine, str);
                }
                if (!s.hasNextLine())
                {
                    result.get(name).put(str, "Success");
                }
                prevLine = str;
                lineCounter++;
            }

        }
        catch (FileNotFoundException e)
        {
            System.out.println("Couldn't find file.\n");
            e.printStackTrace();
        }

        return result;
    }
}