

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
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter a message for the echo");
        String textMessage = scanner.nextLine();

        while (!textMessage.equals("QUIT"))
        {
            MessageProtocol message = new MessageProtocol(MessageType.AUTH_REQUEST.value, textMessage);
            MessageProtocol serverMessage = connectionToServer.sendForAnswer(message);
            System.out.println("Response from server: " + serverMessage.payload);
            if (serverMessage.type == MessageType.AUTH_SUCCESS.value) {
                dataClient = new DataClient();
                dataClient.connect();
            }
            textMessage = scanner.nextLine();
        }
        connectionToServer.disconnect();
    }
}
