import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;

class ServerThread extends Thread
{
    // Timeout time in millisecond
    static public final int MAX_MESSAGE_SIZE = 1024;
    private final int TIME_OUT = 120000;
    protected DataInputStream is;
    protected DataOutputStream os;
    protected Socket s;

    private AuthSystem authManager;

    private String CERTIFICATE_NAME = "server_crt.crt";
    private String line = new String();

    private String username = new String();
    private String token = new String();

    /**
     * Creates a server thread on the input socket
     *
     * @param s input socket to create a thread on
     */
    public ServerThread(Socket s)
    {
        this.s = s;
        initializeServer();
    }

    /**
     * The server thread, echos the client until it receives the QUIT string from the client
     */
    public void run()
    {
        try
        {

            s.setSoTimeout(TIME_OUT);
            is = new DataInputStream(s.getInputStream());
            os = new DataOutputStream(s.getOutputStream());

        }
        catch (IOException e)
        {
            System.err.println("Server Thread. Run. IO error in server thread");
        }

        try
        {
            byte[] data = new byte[MAX_MESSAGE_SIZE];

            int usernameLength = is.readInt();
            is.readFully(data,0,usernameLength);
            username = new String(data).replaceAll("(\\r|\\n)", "");

            authManager.setUser(username);

            if (authManager.doesUserExist(username)) {
                sendMessageToClient("What is your password?");
            } else {
                sendMessageToClient("User doesn't exist");
                closeThreadAndSocket();
            }

            int passwordLength = is.readInt();
            is.readFully(data,0,passwordLength);
            String password = new String(data);

            authManager.isPasswordCorrect(password);

            if(authManager.doesAuthGranted()) {
                sendMessageToClient("AUTH");
                // Send certificate

                sendCertToClient();

                // Start SSL
            } else {
                // Close Connection
                sendMessageToClient("Wrong password");
                closeThreadAndSocket();
            }



        }
        catch (SocketTimeoutException e)
        {
            System.err.println("Timeout");
        }
        catch (IOException e)
        {
            e.printStackTrace();
            line = this.getName(); //reused String line for getting thread name
            System.err.println("Server Thread. Run. IO Error/ Client " + line + " terminated abruptly");
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            line = this.getName(); //reused String line for getting thread name
            System.err.println("Null Pointer in Server Thread. Run.Client " + line + " Closed");
            System.err.println(e);
        } finally
        {
            closeThreadAndSocket();
        }//end finally
    }


    private void initializeServer() {
        authManager = new AuthSystem();
    }
    /**
     * The function to send a string message to the client
     *
     * @param str the string to be sent as message
     */
    private void sendMessageToClient(String str) throws IOException {

        System.out.println("sendMessageToClient: " + str);
        byte[] payloadBytes = str.getBytes();
        os.write(payloadBytes);
        os.flush();
    }

    private void sendCertToClient() throws IOException {

        String localDir = System.getProperty("user.dir");
        String requestedFileLocation = localDir + "/Project-2-Codes/ssl-server/"+ CERTIFICATE_NAME;

        System.out.println(requestedFileLocation);
        byte[] fileByteArray = FileManager.fileToByte(requestedFileLocation);

        os.write(fileByteArray,0, fileByteArray.length);
        os.flush();
        System.out.println("File sent...");

    }

    private void closeThreadAndSocket() {
        System.out.println("Closing socket and thread");
        try
        {
            System.out.println("Closing the connection");
            if (is != null)
            {
                is.close();
                System.err.println(" Socket Input Stream Closed");
            }

            if (os != null)
            {
                os.close();
                System.err.println("Socket Out Closed");
            }
            if (s != null)
            {
                s.close();
                System.err.println("Socket Closed");
            }

        }
        catch (IOException ie)
        {
            System.err.println("Socket Close Error");
        }
        this.interrupt();
    }
}

