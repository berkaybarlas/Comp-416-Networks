import auth.AuthSystem;
import data.DataServer;
import data.DataServerThread;
import utils.HashUtils;
import utils.MessageProtocol;
import utils.MessageType;

import java.io.*;
import java.net.Socket;
import java.util.*;

class ServerThread extends Thread
{
    // Timeout time in millisecond
    private final int TIME_OUT = 10000;
    protected DataInputStream is;
    protected DataOutputStream os;
    protected Socket s;
    private DataServer dataServer;
    private MessageProtocol message;
    private String line = new String();
    private String lines = new String();
    private HashMap<String, HashMap<String, String>> oldauthSystem = new HashMap<String, HashMap<String, String>>();
    private AuthSystem authManager;
    private String username = new String();

    private String token = new String();

    /**
     * Creates a server thread on the input socket
     *
     * @param s input socket to create a thread on
     */
    public ServerThread(Socket s, DataServer dataServer)
    {
        this(s);
        this.dataServer = dataServer;
    }

    /**
     * Creates a server thread on the input socket
     *
     * @param s input socket to create a thread on
     */
    private ServerThread(Socket s)
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
            is = new DataInputStream(s.getInputStream());
            os = new DataOutputStream(s.getOutputStream());

        }
        catch (IOException e)
        {
            System.err.println("Server Thread. Run. IO error in server thread");
        }

        try
        {
            byte[] data = new byte[MessageProtocol.MAX_BYTE_SIZE];
            is.read(data);
            message = new MessageProtocol(data);
            while (message.payload.compareTo("QUIT") != 0)
            {
                // TODO: IMPLEMENT AUTHENTICATION
                // TODO: DATA COMMUNICATION
                // TODO: TIMEOUT
                // TODO: WHETHER API
		        lines = " messaged : " + message.type + "| " + message.payload + " @thread#" + Thread.currentThread().getId();
                System.out.println("Client " + s.getRemoteSocketAddress() + lines);

                switch (MessageType.getMessageType(message.type)) {
                    case AUTH_REQUEST:
                        if(!authManager.doesUserExist(message.payload) && username.equals("")) {
                            sendMessageToClient(MessageType.AUTH_FAILURE, "User Does Not Exist\n");
                            break;
                        }

                        if (username.equals("") && authManager.doesUserExist(message.payload)) {
                            username = message.payload;
                            authManager.setUser(username);
                        }

                        if (authManager.isAnswerCorrect(message.payload)) {
                            if (authManager.doesAuthGranted()) {

                                token = authManager.generateToken(username);

                                sendMessageToClient(MessageType.AUTH_SUCCESS, token);
                            } else {
                                sendMessageToClient(MessageType.AUTH_CHALLENGE, authManager.getCurrentQuestion());
                            }
                        } else {
                            sendMessageToClient(MessageType.AUTH_FAILURE, "Incorrect Answer\n");
                            // TODO break connection
                        }

                        break;
                    case API_REQUEST:
                        // succes, fail
                        sendMessageToClient(MessageType.API_RESPONSE, "API_RESPONSE");
                        break;
                    case API_REQUEST_DATA:
                        String clientID = message.payload;
                        byte[] dataPacket = message.payload.getBytes();
                        String hashedData = HashUtils.generateSHA256(dataPacket);
                        sendMessageToClient(MessageType.API_DATA_HASH, hashedData);

                        DataServerThread DSThread = dataServer.getDSThread(clientID);
                        //Stop timeout or increase
                        if (DSThread != null) {
                            System.out.println("Sending Data to client " + s.getRemoteSocketAddress());
                            // Send string or image
                            String localDir = System.getProperty("user.dir");
                            File file = new File(localDir + "/server/downloads/833-clouds_new-image.png");
                            DSThread.sendFileData(file);
                        }
                        break;
                    case API_DATA_RECEIVED:
                        // Start timeout
                        sendMessageToClient(MessageType.UNDEFINED, "UNDEF");
                        break;
                    default:
                        sendMessageToClient(MessageType.AUTH_CHALLENGE, "Question");
                }

                is.read(data);
                message = new MessageProtocol(data);
            }
        }
        catch (IOException e)
        {
            line = this.getName(); //reused String line for getting thread name
            System.err.println("Server Thread. Run. IO Error/ Client " + line + " terminated abruptly");
        }
        catch (NullPointerException e)
        {
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
    private void sendMessageToClient(MessageType type, String str) throws IOException {
        MessageProtocol message = new MessageProtocol(type.value, str);
        System.out.println("sendMessageToClient: " + str);
        os.write(message.getByteMessage());
        os.flush();
    }

    private void closeThreadAndSocket() {
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
    }
}
