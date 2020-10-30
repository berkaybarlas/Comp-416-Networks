import models.BaseClient;
import utils.MessageProtocol;

import java.io.*;

public class DataClient extends BaseClient {
    public static final int DEFAULT_DATA_SERVER_PORT = 4545;

    public DataClient()
    {
        this(DEFAULT_SERVER_ADDRESS, DEFAULT_DATA_SERVER_PORT);
    }

    public DataClient(String address, int port)
    {
        super(address, port);
    }

    public void connect()
    {
        try
        {
            connectToServer();
            /*
            Read and write buffers on the socket
             */

            // TODO: Update writer and readers
            // single directional no need for writer
            is = new DataInputStream(s.getInputStream());
            os = new DataOutputStream(s.getOutputStream());
            //is = new BufferedReader(new InputStreamReader(s.getInputStream()));
            // os = new PrintWriter(s.getOutputStream());

            System.out.println("Successfully connected to " + serverAddress + " on port " + serverPort
                    + " with local address" + s.getLocalSocketAddress());
        }
        catch (IOException e)
        {
            System.err.println("Error: no server has been found on " + serverAddress + "/" + serverPort);
        }
    }

    public void waitForData() {
        try
        {
            String response = new MessageProtocol(is.readAllBytes()).payload;
            System.out.println("response" + response);
        }
        catch (IOException e)
        {
            //e.printStackTrace();
            System.err.println("Error: no server has been found on " + serverAddress + "/" + serverPort);
        }
    }
}
