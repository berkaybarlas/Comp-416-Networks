import java.util.Scanner;

public class ClientMain
{
    public static final String DEFAULT_SERVER_ADDRESS = "localhost";

    public final static int TLS_SERVER_PORT = 54517;
    protected static ConnectionToServer connectionToServer = null;
    protected static boolean authenticated = false;
    protected static boolean disconnected = false;
    protected static String token = "";
    protected static String userName = "";

    public static void main(String args[])
    {
        connectionToServer = new ConnectionToServer(DEFAULT_SERVER_ADDRESS, ConnectionToServer.DEFAULT_SERVER_PORT);
        connectionToServer.connect();


        String fileType = "";
        String serverResponse;

        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter your username to send server");
        String textMessage = scanner.nextLine();
        int lastResponseType = -1;

        serverResponse = connectionToServer.sendForAnswer(textMessage);
        System.out.println("Response from server: " + serverResponse);

        System.out.println("Enter your password to send server");
        textMessage = scanner.nextLine();
        serverResponse = connectionToServer.sendForAnswer(textMessage);
        System.out.println("Response from server: " + serverResponse);

        if (serverResponse.contains("AUTH")) {

            connectionToServer.askForCertificate();
        }

        closeConnections();

        // Start SSL connection
        try {
        /*
        Creates an SSLConnectToServer object on the specified server address and port
         */
        SSLConnectToServer sslConnectToServer = null;

        // Current index of cumulative message
        int currentIndex = 0;
        String message = "";

            while(true) {
                sslConnectToServer = new SSLConnectToServer(DEFAULT_SERVER_ADDRESS, TLS_SERVER_PORT);

                /* Connects to the server */
                sslConnectToServer.Connect();

                /* Sends a message over SSL socket to the server and prints out the received message from the server */

                String response = sslConnectToServer.SendForAnswer(""+currentIndex);

                if (response.charAt(0) == '/') {
                    break;
                } else {
                    message += response.charAt(0);
                    currentIndex++;
                }


                /* Disconnects from the SSL server */
                sslConnectToServer.Disconnect();
            }
            System.out.println("SSL message recieved from server: " + message);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    static private void closeConnections() {

        connectionToServer.disconnect();
    }
}
