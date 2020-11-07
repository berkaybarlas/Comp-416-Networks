

import utils.*;

import java.util.Arrays;
import java.util.Scanner;

public class ClientMain
{
    protected static ConnectionToServer connectionToServer = null;
    protected static DataClient dataClient = null;
    protected static boolean authenticated = false;
    protected static boolean disconnected = false;
    protected static String token = "";
    protected static String userName = "";

    public static void main(String args[])
    {
        connectionToServer = new ConnectionToServer(ConnectionToServer.DEFAULT_SERVER_ADDRESS, ConnectionToServer.DEFAULT_SERVER_PORT);
        connectionToServer.connect();

        MessageProtocol message = null;
        String fileType = "";

        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter your username to send server");
        String textMessage = ""; // = scanner.nextLine();
        int lastResponseType = -1;
        MessageProtocol serverResponse = null;

        while (!textMessage.equals("QUIT") && !disconnected)
        {
            switch (MessageType.getMessageType(lastResponseType)) {
                case AUTH_CHALLENGE:
                    textMessage = scanner.nextLine();
                    message = new MessageProtocol(MessageType.AUTH_REQUEST, textMessage);
                    break;
                case AUTH_SUCCESS:
                    authenticated = true;
                    token = serverResponse.payload;
                    /** For test purposes */
                    if (userName.equals("hacker")) {
                        token = "goodTry";
                    }
                    // Start dataclient and send socket
                    dataClient = new DataClient();
                    dataClient.connect();
                    // Request for data connection
                    message = new MessageProtocol(MessageType.DATA_CONNECTION_REQUEST,token, dataClient.clientIdentifier);
                    break;
                case DATA_CONNECTION_ACCEPTED:
                    System.out.println("Now, you can send request to server: ");
                    textMessage = scanner.nextLine();
                    message = generateAPIRequest(textMessage);
                    break;
                case DATA_CONNECTION_DECLINED:
                    System.out.println("Connection closed with reason: " + serverResponse.payload);
                    message = null;
                    closeConnections();
                    break;
                case API_RESPONSE_SUCCESS:
                    fileType = serverResponse.payload;
                    System.out.println("Ready to receive :" + fileType);
                    message = new MessageProtocol(MessageType.API_REQUEST_DATA,token, "Ready to receive");
                    break;
                    // TODO MISSING RESPONSE FAIL CASE
                case API_DATA_HASH:
                    // Try to recieve data
                    System.out.println("Waiting for " + fileType +" data");
                    // Get file type
                    String recevingFileName = textMessage + "-" + System.currentTimeMillis();

                    byte [] receivedData = dataClient.waitForFile(recevingFileName, fileType);
                    System.out.println("Data recieved");

                    // Check hash
                    boolean isDataValid = HashUtils.checkSHA256Integrity(serverResponse.payload, receivedData);
                    System.out.println("Does hash value match: " + isDataValid);

                    // Send received type message
                    if (isDataValid) {
                        message = new MessageProtocol(MessageType.API_DATA_RECEIVED,token, "Data hash matches.");
                    } else {
                        message = new MessageProtocol(MessageType.API_DATA_FAILED,token, "Data hash does not match!");
                    }

                    break;
                case CONNECTION_CLOSED:
                case TIMEOUT:
                    // Timeout: close connection
                    disconnected = true;
                    continue;
                case AUTH_FAIL:
                default:
                    if (authenticated) {
                        System.out.println("Send another request to server: ");
                        textMessage = scanner.nextLine();
                        message = generateAPIRequest(textMessage);
                    } else {
                        textMessage = scanner.nextLine();
                        if (userName == "") {
                            userName = textMessage;
                        }
                        message = new MessageProtocol(MessageType.AUTH_REQUEST, textMessage);
                    }
            }

            serverResponse = connectionToServer.sendForAnswer(message);
            lastResponseType = serverResponse.type.value;
            System.out.println("Response from server: " + serverResponse.payload + " type: " + lastResponseType);
        }
        closeConnections();
    }

    static private MessageProtocol generateAPIRequest(String textMessage) {
        RequestType req;
        String[] parsed = textMessage.split("\\s+");

        req = RequestType.getRequestType(parsed[0]);

        String[] params = Arrays.copyOfRange(parsed, 1, parsed.length);

        return MessageProtocol.createAPIRequest(token, req, params);
    }

    static private void closeConnections() {
        if (dataClient != null) {
            dataClient.disconnect();
        }
        connectionToServer.disconnect();
    }
}
