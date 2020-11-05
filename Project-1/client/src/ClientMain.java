

import utils.HashUtils;
import utils.MessageProtocol;
import utils.MessageType;

import java.util.Scanner;

public class ClientMain
{
    protected static ConnectionToServer connectionToServer = null;
    protected static DataClient dataClient = null;
    protected static boolean authenticated = false;
    protected static String token = "";

    public static void main(String args[])
    {
        connectionToServer = new ConnectionToServer(ConnectionToServer.DEFAULT_SERVER_ADDRESS, ConnectionToServer.DEFAULT_SERVER_PORT);
        connectionToServer.connect();


        boolean waitUserInput = true;


        MessageProtocol message;

        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter your username to send server");
        String textMessage = scanner.nextLine();
        int lastResponseType = -1;
        MessageProtocol serverResponse = null;

        while (!textMessage.equals("QUIT"))
        {
            waitUserInput = true;
            switch (MessageType.getMessageType(lastResponseType)) {
                case AUTH_CHALLENGE:
                    message = new MessageProtocol(MessageType.AUTH_REQUEST.value, textMessage);
                    // TODO: missing question
                    break;
                case AUTH_SUCCESS:
                    authenticated = true;
                    token = serverResponse.payload;
                    // Start dataclient and send socket
                    dataClient = new DataClient();
                    dataClient.connect();
                    // Request for data connection
                    message = new MessageProtocol(MessageType.DATA_CONNECTION_REQUEST.value,token, dataClient.clientIdentifier);
                    waitUserInput = false;
                    break;
                    // TODO: missing auth fail
                case DATA_CONNECTION_ACCEPTED:
                    // TODO improve request type
                    message = new MessageProtocol(MessageType.API_REQUEST.value, textMessage);
                    break;
                case DATA_CONNECTION_DECLINED:
                    System.out.println("Connection closed with reason: " + serverResponse.payload);
                    // TODO timeout, throw error and close connection
                    message = null;
                    closeConnections();
                    break;
                case API_RESPONSE:
                    message = new MessageProtocol(MessageType.API_REQUEST_DATA.value,token, "Ready to receive");
                    waitUserInput = false;
                    break;
                case API_DATA_HASH:
                    // Try to recieve data
                    System.out.println("Waiting for data");
                    // TODO get file type
                    String fileType = ".png";
                    String recevingFileName = textMessage + "-" + System.currentTimeMillis() + fileType;

                    byte [] receivedData = dataClient.waitForFile(recevingFileName);
                    System.out.println("Data recieved");

                    // Check hash
                    boolean isDataValid = HashUtils.checkSHA256Integrity(serverResponse.payload, receivedData);
                    System.out.println("Does data correct: " + isDataValid);

                    // Send recieved type message
                    // TODO update payload
                    message = new MessageProtocol(MessageType.API_DATA_RECEIVED.value,token, "" + isDataValid);
                    break;
                case TIMEOUT:
                    // TODO timeout, throw error and close connection
                    message = null;
                    closeConnections();
                    break;
                default:
                    if (authenticated) {
                        message = new MessageProtocol(MessageType.API_REQUEST.value, textMessage);
                        waitUserInput = false;
                    } else {
                        message = new MessageProtocol(MessageType.AUTH_REQUEST.value, textMessage);
                    }
            }
            serverResponse = connectionToServer.sendForAnswer(message);
            lastResponseType = serverResponse.type;
            System.out.println("Response from server: " + serverResponse.payload + " type: " + lastResponseType);
            if (waitUserInput) {
                textMessage = scanner.nextLine();
            }
        }
        connectionToServer.disconnect();
    }

    static private void closeConnections() {
        dataClient.disconnect();
        connectionToServer.disconnect();
    }
}
