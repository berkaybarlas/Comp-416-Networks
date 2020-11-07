package auth;

import java.io.File;
import java.util.*;
import java.io.FileNotFoundException;
import java.security.SecureRandom;

public class AuthSystem
{
    private int prime1 = 7;
    private int prime2 = 31;

    private String SUCCESS = "success";
    private HashMap<String, HashMap<String, String>> userQuestionMap;
    private String currentUser;
    private String currentAuthQuestion = new String();

    public AuthSystem() {
        userQuestionMap = retrieveUserQuestions();
        userQuestionMap.entrySet().forEach(entry->{
            System.out.println(entry.getKey() + " " + entry.getValue());
        });
    }

    public void setUser(String username) {
        currentUser = username;
    }

    public boolean doesUserExist(String username) {
        return userQuestionMap.containsKey(username);
    }

    public String getNextQuestion(String prevQuestion) {
        currentAuthQuestion = userQuestionMap.get(currentUser).get(prevQuestion);
        return userQuestionMap.get(currentUser).get(prevQuestion);
    }

//    public Boolean isAnswerCorrect(String question, String answer) {
//        if (question == "" || question == null || question == SUCCESS) {
//            return true;
//        }
//        return getAnswer(question).equals(answer);
//    }

    //

    public Boolean isAnswerCorrect(String answer) {
        boolean isCorrect = false;
        if (currentAuthQuestion.isEmpty() || currentAuthQuestion == SUCCESS) {
            isCorrect = true;
        } else {
            isCorrect = getAnswer(currentAuthQuestion).equals(answer);
        }

        if (isCorrect) {
            getNextQuestion(answer);
        }

        return isCorrect;
    }

    public String getCurrentQuestion() {
        return currentAuthQuestion;
    }


    private static final SecureRandom secureRandom = new SecureRandom(); //threadsafe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe

    public static String generateNewToken(String username) {
        byte[] randomBytes = new byte[6];
        secureRandom.nextBytes(randomBytes);
        String token = base64Encoder.encodeToString(randomBytes);
        System.out.println("Generated token for " + username + ": " + token);
        return token;
    }



    public String generateToken(String username) {
        // 1.generate a random number
        // 2.concat it to username
        // 3.hash = hash*prime2 + charAt(i) for all i in text
        Random rand = new Random();
        int hash = prime1;
        int rand1 = rand.nextInt();
        username += rand1;
        for (int i=0; i<username.length(); i++)  {
            hash = hash*prime2 + username.charAt(i);
        }

        String token = hash + "";


        System.out.println("Generated token for " + username + ": " + token);
        return token;
    }
    public boolean doesAuthGranted() {
        return currentAuthQuestion.equals(SUCCESS);
    }

    private String getAnswer(String question) {
        return userQuestionMap.get(currentUser).get(question);
    }

    private HashMap<String, HashMap<String, String>> retrieveUserQuestions()
    {
        HashMap<String, HashMap<String, String>> result = new HashMap<String, HashMap<String, String>>();
        int lineCounter = 0;

        String name = "";
        try
        {
            String localDir = System.getProperty("user.dir");
            File qa = new File(localDir + "/server/src/resources/questions_answers.txt");
            Scanner s = new Scanner(qa);

            String prevLine = "";
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
                    result.get(name).put(prevLine, SUCCESS);
                }
                else
                {
                    result.get(name).put(prevLine, str);
                }
                if (!s.hasNextLine())
                {
                    result.get(name).put(str, SUCCESS);
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