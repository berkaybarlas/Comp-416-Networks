package data;

import models.BaseServer;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;

public class DataServer extends BaseServer implements Runnable {
    public static final int DEFAULT_DATA_SERVER_PORT = 4545;
    private HashMap<String, DataServerThread> dataServerThreads = new HashMap<>();

    /**
     * Initiates a server socket on the default port, listens to the line, on receiving an incoming
     * connection creates and starts a DataThread on the client
     */
    public DataServer() {
        this(DEFAULT_DATA_SERVER_PORT);
    }

    /**
     * Initiates a server socket on the input port, listens to the line, on receiving an incoming
     * connection creates and starts a DataThread on the client
     * @param port
     */
    public DataServer(int port) {
        super(port);
    }

    public void run() {
        while (true) {
            try {
                acceptConnection();
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("data.DataServer Class.Connection establishment error inside listen and accept function");
            }
        }
    }

    /**
     * Get the file server thread with the given identifier
     *
     * @param id Identifier for the data server thread
     * @return Data server thread
     */
    public DataServerThread getDSThread(String id) {
        return dataServerThreads.get(id);
    }

    /**
     * Starts a single direction connection
     * The connection is started and initiated as a DataThread object
     * @return Data server thread
     */
    private DataServerThread acceptConnection() throws IOException
    {
        Socket s = getSocket().accept();
        // TODO: CHECK REMOTE ADDRESS WITH AUTHENTICATED ADRESSES
        String socketId = "" + s.getRemoteSocketAddress();
        System.out.println("A data connection was established with a client on the address of " + s.getRemoteSocketAddress());
        DataServerThread st = new DataServerThread(s);
        st.start();
        dataServerThreads.put(socketId, st);
        return st;

    }
}
