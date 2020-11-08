import auth.AuthSystem;
import data.DataServer;
import data.DataServerThread;
import owm.OWMManager;
import owm.RequestError;
import utils.*;

import java.io.*;
import java.net.Socket;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

class ServerThread extends Thread
{
    // Timeout time in millisecond
    private final int TIME_OUT = 120000;
    protected DataInputStream is;
    protected DataOutputStream os;
    protected Socket s;

    private DataServer dataServer;
    private AuthSystem authManager;
    private int authTryCount = 0;

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
            byte[] data = new byte[MessageProtocol.MAX_BYTE_SIZE];
            is.read(data);
            message = new MessageProtocol(data);
            while (message.payload.compareTo("QUIT") != 0)
            {
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

                                token = authManager.generateNewToken(username);

                                sendMessageToClient(MessageType.AUTH_SUCCESS, token);
                            } else {
                                sendMessageToClient(MessageType.AUTH_CHALLENGE, authManager.getCurrentQuestion());
                            }
                        } else {
                            if (authTryCount++ >= 2) {
                                sendMessageToClient(MessageType.CONNECTION_CLOSED, "Too many incorrect answers");
                                closeThreadAndSocket();
                                continue;
                            }
                            sendMessageToClient(MessageType.AUTH_FAIL, "Incorrect Answer. Trial number: " + authTryCount);
                        }

                        break;
                    case DATA_CONNECTION_REQUEST:
                        String clientID = message.payload;
                        DSThread = dataServer.getDSThread(clientID);
                        // TODO check TOKEN
                        checkAuthToken();
                        if (DSThread != null) {
                            sendMessageToClient(MessageType.DATA_CONNECTION_ACCEPTED, "Connection found");
                            owmManager = new OWMManager();
                        } else {
                            sendMessageToClient(MessageType.DATA_CONNECTION_DECLINED, "Connection not found");
                        }

                        break;
                    case API_REQUEST:
                        // TODO check TOKEN
                        checkAuthToken();
                        handleOWMAPIRequest(message);
                        break;
                    case API_DATA_FAILED:
                    case API_REQUEST_DATA:
                        /** check TOKEN */
                        checkAuthToken();

                        byte[] fileByteArray = FileManager.fileToByte(requestedFileLocation);

                        String hashedData = HashUtils.generateSHA256(fileByteArray);
                        /** For demo broken hash */
                        if (username.equals("bedevi") && message.type.value != MessageType.API_DATA_FAILED.value) {
                            hashedData = "ASDFASDFSADFADSFA";
                        }
                        sendMessageToClient(MessageType.API_DATA_HASH, hashedData);


                        if (DSThread != null) {
                            System.out.println("Sending Data to client " + s.getRemoteSocketAddress());
                            // Send string or image
                            DSThread.sendFileData(fileByteArray);
                        }
                        break;
                    case API_DATA_RECEIVED:
                        sendMessageToClient(MessageType.API_PROCESS_COMPLETE, "Process Complete");
                        break;
                    default:
                        sendMessageToClient(MessageType.AUTH_CHALLENGE, "Question");
                }

                is.read(data);
                message = new MessageProtocol(data);
            }
        }
        catch (WrongTokenException e) {
            System.out.println("Client sent wrong Token");
        }
        catch (SocketTimeoutException e)
        {
            System.err.println("Timeout");
            try {
                sendMessageToClient(MessageType.TIMEOUT, "Your session has expired.");
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        catch (IOException e)
        {
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

    private void checkAuthToken() throws WrongTokenException, IOException{
        if (!message.checkToken(token)) {
            if (DSThread != null) {
                DSThread.closeThreadAndSocket();
            }
            this.sendMessageToClient(MessageType.CONNECTION_CLOSED, "Wrong token GoodBye, do not come back!");
            throw new WrongTokenException("Token does not match");
        }
    }
    private void handleOWMAPIRequest(MessageProtocol message) throws IOException {
        String errorReason = "Wrong request";

        String[] params = message.params;
        RequestType requestType = message.requestType;
        if ( requestType == null || params == null || params.length == 0) {
            errorReason = "Request type does not exists";
            this.sendMessageToClient(MessageType.API_RESPONSE_FAIL, errorReason);
            return;
        }

        try {
            /** Send request to OWM reason*/
            requestedFileLocation = owmManager.requestData(requestType, params);
            String fileType = FileManager.getFileType(requestedFileLocation);
            this.sendMessageToClient(MessageType.API_RESPONSE_SUCCESS, fileType);
        } catch (RequestError requestError) {
            errorReason = requestError.toString();
            this.sendMessageToClient(MessageType.API_RESPONSE_FAIL, errorReason);
        }

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

class WrongTokenException extends Exception {
    String message;

    /**
     * Custom error for wrong requests
     */
    WrongTokenException(String message) {
        this.message = message;
    }

    public String toString() {
        return ("RequestError: " + message);
    }
}
