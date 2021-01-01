import java.io.File;
import java.io.FileNotFoundException;
import java.security.SecureRandom;
import java.util.*;

public class AuthSystem
{

    private String SUCCESS = "success";
    private HashMap<String, String> userQuestionMap;
    private String currentUser;
    private Boolean authGranted = false;


    public AuthSystem() {
        userQuestionMap = retrieveUserPasswords();
        userQuestionMap.entrySet().forEach(entry->{
            System.out.println(entry + " ");
        });
    }

    public void setUser(String username) {
        currentUser = username;
    }

    public boolean doesUserExist(String username) {
        Set<String> match =  userQuestionMap.keySet();
        for (String temp : match) {
            if (username.contains(temp)) {
                this.currentUser = temp;
                return true;
            }
        }

        return userQuestionMap.containsKey(username);
    }

    public Boolean isPasswordCorrect(String password) {
        boolean isCorrect = false;
        if (password.contains(userQuestionMap.get(currentUser))) {
            System.out.println("Auth succesfull");
            isCorrect = true;
            authGranted = true;
        }

        return isCorrect;
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

    public boolean doesAuthGranted() {
        return authGranted;
    }

    private String getAnswer(String question) {
        return userQuestionMap.get(currentUser);
    }

    private HashMap<String, String> retrieveUserPasswords()
    {
        HashMap<String, String> result = new HashMap<String, String>();
        int lineCounter = 0;

        String name = "";
        try
        {
            String localDir = System.getProperty("user.dir");
            System.out.println(localDir);
            File qa = new File(localDir + "/Project-2-Codes/ssl-server/src/resources/user_passwords.txt");
            Scanner s = new Scanner(qa);

            String prevLine = "";
            while (s.hasNextLine())
            {
                String str = s.nextLine();
                if (lineCounter == 0 || prevLine.equals(""))
                {
                    name = str;
                }
                else if (!str.equals(""))
                {
                    result.put(name, str.replaceAll("(\\r|\\n)", ""));
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