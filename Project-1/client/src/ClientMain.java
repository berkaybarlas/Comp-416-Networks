

import utils.HashUtils;
import utils.MessageProtocol;
import utils.MessageType;

import java.util.Scanner;

public class ClientMain
{


    public static void main(String args[])
    {
        ConnectionToServer connectionToServer = new ConnectionToServer(ConnectionToServer.DEFAULT_SERVER_ADDRESS, ConnectionToServer.DEFAULT_SERVER_PORT);
        connectionToServer.connect();

        DataClient dataClient = null;
        boolean authenticated = false;
        boolean waitUserInput = true;
        String token = "";

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
                    message = new MessageProtocol(MessageType.API_REQUEST.value, token, textMessage);
                    waitUserInput = false;
                    break;
                    // TODO: missing auth fail
                case API_RESPONSE:
                    // Start dataclient and send socket
                    dataClient = new DataClient();
                    dataClient.connect();
                    message = new MessageProtocol(MessageType.API_REQUEST_DATA.value,token, dataClient.clientIdentifier);
                    waitUserInput = false;
                    break;
                case API_DATA_HASH:
                    // Try to recieve data
                    System.out.println("Waiting for data");
                    byte [] receivedData = dataClient.waitForFile();
                    System.out.println("Data recieved");

                    // Check hash
                    boolean isDataValid = HashUtils.checkSHA256Integrity(serverResponse.payload, receivedData);
                    System.out.println("Does data correct: " + isDataValid);

                    // Send recieved type message
                    // TODO update payload
                    message = new MessageProtocol(MessageType.API_DATA_RECEIVED.value,token, "" + isDataValid);
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
}
