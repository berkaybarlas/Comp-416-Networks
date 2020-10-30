package models;

import java.io.*;
import java.net.Socket;

public class BaseClient {
    public static final String DEFAULT_SERVER_ADDRESS = "localhost";

    protected Socket s;
    protected DataInputStream is;
    protected DataOutputStream os;

    protected String serverAddress;
    protected int serverPort;

    public BaseClient(String address, int port)
    {
        serverAddress = address;
        serverPort    = port;
    }

    protected void connectToServer() throws IOException {
        s = new Socket(serverAddress, serverPort);
    }
    /**
     * Disconnects the socket and closes the buffers
     */
    public void disconnect()
    {
        try
        {
            is.close();
            os.close();
            s.close();
            System.out.println("ConnectionToServer. SendForAnswer. Connection Closed");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
