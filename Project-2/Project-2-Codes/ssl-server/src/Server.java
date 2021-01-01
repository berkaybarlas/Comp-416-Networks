import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;

public class Server
{
    public static final int DEFAULT_SERVER_PORT = 4444;
    private ServerSocket serverSocket;
    /**
     * Initiates a server socket on the input port using parent constructor, listens to the line, on receiving an incoming
     * connection creates and starts a ServerThread
     * @param port
     */
    public Server(int port)
    {
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

        while (true)
        {
            listenAndAccept();
        }
    }

    /**
     * Listens to the line and starts a connection on receiving a request from the client
     * The connection is started and initiated as a ServerThread object
     */
    private void listenAndAccept()
    {
        Socket s;
        try
        {
            s = this.getSocket().accept();
            System.out.println("A connection was established with a client on the address of " + s.getRemoteSocketAddress());
            ServerThread st = new ServerThread(s);
            st.start();

        }

        catch (Exception e)
        {
            e.printStackTrace();
            System.err.println("Server Class.Connection establishment error inside listen and accept function");
        }
    }

    public ServerSocket getSocket() { return serverSocket; }

}

