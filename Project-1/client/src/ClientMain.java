

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
        String token = "";

        MessageProtocol message;

        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter your username to send server");
        String textMessage = scanner.nextLine();
        int lastResponseType = -1;
        MessageProtocol serverResponse;

        while (!textMessage.equals("QUIT"))
        {
            switch (MessageType.getMessageType(lastResponseType)) {
                case AUTH_CHALLANGE:
                    message = new MessageProtocol(MessageType.AUTH_REQUEST.value, textMessage);
                    // TODO: missing question
                    break;
                case AUTH_SUCCESS:
                    authenticated = true;
                    message = new MessageProtocol(MessageType.API_REQUEST.value, token, textMessage);
                    break;
                    // TODO: missing auth fail
                case API_RESPONSE:
                    // Start dataclient and send socket
                    dataClient = new DataClient();
                    message = new MessageProtocol(MessageType.API_REQUEST_DATA.value,token, dataClient.clientIdentifier);
                    break;
                default:
                    if (authenticated) {
                        message = new MessageProtocol(MessageType.API_REQUEST.value, textMessage);
                    } else {
                        message = new MessageProtocol(MessageType.AUTH_REQUEST.value, textMessage);
                    }
            }
            serverResponse = connectionToServer.sendForAnswer(message);
            lastResponseType = serverResponse.type;
            System.out.println("Response from server: " + serverResponse.payload + " type: " + lastResponseType);
            textMessage = scanner.nextLine();
        }
        connectionToServer.disconnect();
    }
}
