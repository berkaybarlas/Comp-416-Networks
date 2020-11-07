import data.DataServer;
import models.BaseServer;
import java.io.IOException;
import java.net.*;

public class Server extends BaseServer
{
    public static final int DEFAULT_SERVER_PORT = 4444;
    private DataServer dataServer;
    /**
     * Initiates a server socket on the input port using parent constructor, listens to the line, on receiving an incoming
     * connection creates and starts a ServerThread
     * @param port
     */
    public Server(int port)
    {
        super(port);
        dataServer = new DataServer();
        Thread dataServerMainThread = new Thread(dataServer);
        dataServerMainThread.start();
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
            ServerThread st = new ServerThread(s, dataServer);
            st.start();

        }

        catch (Exception e)
        {
            e.printStackTrace();
            System.err.println("Server Class.Connection establishment error inside listen and accept function");
        }
    }

}

