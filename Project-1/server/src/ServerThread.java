import auth.AuthSystem;
import data.DataServer;
import data.DataServerThread;
import owm.OWMManager;
import utils.FileManager;
import utils.HashUtils;
import utils.MessageProtocol;
import utils.MessageType;

import java.io.*;
import java.net.Socket;

class ServerThread extends Thread
{
    // Timeout time in millisecond
    private final int TIME_OUT = 10000;
    protected DataInputStream is;
    protected DataOutputStream os;
    protected Socket s;

    private DataServer dataServer;
    private AuthSystem authManager;

    private MessageProtocol message;
    private String requestedFileLocation;
    private String line = new String();
    private String lines = new String();

    private String username = new String();
    private String token = new String();
    private DataServerThread DSThread;
    private OWMManager owmManager;

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

                switch (MessageType.getMessageType(message.type.value)) {
                    case AUTH_REQUEST:
                        if(!authManager.doesUserExist(message.payload) && username.equals("")) {
                            sendMessageToClient(MessageType.AUTH_FAIL, "User Does Not Exist\n");
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
                            sendMessageToClient(MessageType.AUTH_FAIL, "Incorrect Answer\n");
                            // TODO break connection
                        }

                        break;
                    case DATA_CONNECTION_REQUEST:
                        String clientID = message.payload;
                        DSThread = dataServer.getDSThread(clientID);
                        // TODO check TOKEN
                        if (DSThread != null) {
                            sendMessageToClient(MessageType.DATA_CONNECTION_ACCEPTED, "Connection found");
                            owmManager = new OWMManager();
                        } else {
                            sendMessageToClient(MessageType.DATA_CONNECTION_DECLINED, "Connection not found");
                        }

                        break;
                    case API_REQUEST:
                        // TODO check TOKEN
                        // succes, fail
                        // TODO implement request types
                        String[] params = message.params;
                        // Check params
                        requestedFileLocation = owmManager.getCityWeatherMap(params[0], params[1]);
                        String fileType = FileManager.getFileType(requestedFileLocation);
                        sendMessageToClient(MessageType.API_RESPONSE_SUCCESS, fileType);
                        break;
                    case API_REQUEST_DATA:
//                        String clientID = message.payload;
                        // TODO check TOKEN
//                        String localDir = System.getProperty("user.dir");
//                        File file = new File(localDir + "/server/downloads/833-clouds_new-image.png");
                        byte[] fileByteArray = FileManager.fileToByte(requestedFileLocation);

                        String hashedData = HashUtils.generateSHA256(fileByteArray);
                        sendMessageToClient(MessageType.API_DATA_HASH, hashedData);


                        //Stop timeout or increase
                        if (DSThread != null) {
                            System.out.println("Sending Data to client " + s.getRemoteSocketAddress());
                            // Send string or image
                            DSThread.sendFileData(fileByteArray);
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
        MessageProtocol message = new MessageProtocol(type, str);
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
