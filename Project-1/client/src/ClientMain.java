

import utils.HashUtils;
import utils.MessageProtocol;
import utils.MessageType;
import utils.RequestType;

import java.util.Arrays;
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

        MessageProtocol message = null;
        String fileType = "";

        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter your username to send server");
        String textMessage = ""; // = scanner.nextLine();
        int lastResponseType = -1;
        MessageProtocol serverResponse = null;

        while (!textMessage.equals("QUIT"))
        {
            switch (MessageType.getMessageType(lastResponseType)) {
                case AUTH_CHALLENGE:
                    textMessage = scanner.nextLine();
                    message = new MessageProtocol(MessageType.AUTH_REQUEST, textMessage);
                    // TODO: missing question
                    break;
                case AUTH_SUCCESS:
                    authenticated = true;
                    token = serverResponse.payload;
                    // Start dataclient and send socket
                    dataClient = new DataClient();
                    dataClient.connect();
                    // Request for data connection
                    message = new MessageProtocol(MessageType.DATA_CONNECTION_REQUEST,token, dataClient.clientIdentifier);
                    break;
                    // TODO: missing auth fail
                case DATA_CONNECTION_ACCEPTED:
                    // TODO improve request type
                    textMessage = scanner.nextLine();
                    message = generateAPIRequest(textMessage);
                    break;
                case DATA_CONNECTION_DECLINED:
                    System.out.println("Connection closed with reason: " + serverResponse.payload);
                    // TODO timeout, throw error and close connection
                    message = null;
                    closeConnections();
                    break;
                case API_RESPONSE_SUCCESS:
                    fileType = message.payload;
                    message = new MessageProtocol(MessageType.API_REQUEST_DATA,token, "Ready to receive");
                    break;
                    // TODO MISSING RESPONSE FAIL CASE
                case API_DATA_HASH:
                    // Try to recieve data
                    System.out.println("Waiting for data");
                    // TODO get file type
                    String recevingFileName = textMessage + "-" + System.currentTimeMillis() + "." + fileType;

                    byte [] receivedData = dataClient.waitForFile(recevingFileName);
                    System.out.println("Data recieved");

                    // Check hash
                    boolean isDataValid = HashUtils.checkSHA256Integrity(serverResponse.payload, receivedData);
                    System.out.println("Does data correct: " + isDataValid);

                    // Send recieved type message
                    // TODO update payload
                    if (isDataValid) {
                        message = new MessageProtocol(MessageType.API_DATA_RECEIVED,token, "Data hash matches.");
                    } else {
                        message = new MessageProtocol(MessageType.API_DATA_FAILED,token, "Data hash does not match!");
                    }

                    break;
                case TIMEOUT:
                    // TODO timeout, throw error and close connection
                    message = null;
                    closeConnections();
                    break;
                default:
                    if (authenticated) {
                        textMessage = scanner.nextLine();
                        message = generateAPIRequest(textMessage);
                    } else {
                        textMessage = scanner.nextLine();
                        message = new MessageProtocol(MessageType.AUTH_REQUEST, textMessage);
                    }

            }

            serverResponse = connectionToServer.sendForAnswer(message);
            lastResponseType = serverResponse.type.value;
            System.out.println("Response from server: " + serverResponse.payload + " type: " + lastResponseType);
        }
        connectionToServer.disconnect();
    }

    static private MessageProtocol generateAPIRequest(String textMessage) {
        // TODO implement parser

        RequestType req;
        String[] parsed = textMessage.split("\\s+");

        req = RequestType.getRequestType(parsed[0]);

        String[] params = Arrays.copyOfRange(parsed, 1, parsed.length);

        return MessageProtocol.createAPIRequest(token, req, params);
    }

    static private void closeConnections() {
        dataClient.disconnect();
        connectionToServer.disconnect();
    }
}
