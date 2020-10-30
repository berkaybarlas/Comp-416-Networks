package models;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;

public class BaseServer {
    private ServerSocket serverSocket;

    /**
     * Initiates a server socket on the input port, listens to the line, on receiving an incoming
     * connection creates and starts a ServerThread on the client
     * @param port
     */
    public BaseServer(int port) {
        try
        {
            serverSocket = new ServerSocket(port);
            System.out.println("Opened up a server socket on " + Inet4Address.getLocalHost());
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.err.println("Server class.Constructor exception on opening a server socket");
        }
    }

    public ServerSocket getSocket() { return serverSocket; }
}
