import models.BaseClient;
import utils.MessageProtocol;

import java.io.*;
import java.net.Socket;

/**
 * Created by Yahya Hassanzadeh on 20/09/2017.
 */

public class ConnectionToServer extends BaseClient
{
    public static final int DEFAULT_SERVER_PORT = 4444;


    /**
     *
     * @param address IP address of the server, if you are running the server on the same computer as client, put the address as "localhost"
     * @param port port number of the server
     */
    public ConnectionToServer(String address, int port)
    {
        super(address, port);
    }

    /**
     * Establishes a socket connection to the server that is identified by the serverAddress and the serverPort
     */
    public void connect()
    {
        try
        {
            connectToServer();
            /*
            Read and write buffers on the socket
             */
            is = new DataInputStream(s.getInputStream());
            os = new DataOutputStream(s.getOutputStream());

            System.out.println("Successfully connected to " + serverAddress + " on port " + serverPort);
        }
        catch (IOException e)
        {
            //e.printStackTrace();
            System.err.println("Error: no server has been found on " + serverAddress + "/" + serverPort);
        }
    }

    /**
     * sends the message String to the server and retrives the answer
     * @param message input message string to the server
     * @return the received server answer
     */
    public MessageProtocol sendForAnswer(MessageProtocol message)
    {
        MessageProtocol response = null;
        try
        {
            /*
            Sends the message to the server via Data Stream
             */
            os.write(message.getByteMessage());
            os.flush();
            /*
            Reads a line from the server via Data Stream
             */
            byte[] data = new byte[MessageProtocol.MAX_BYTE_SIZE];
            is.read(data);
            response = new MessageProtocol(data);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            System.out.println("ConnectionToServer: Socket read Error");
        }
        return response;
    }
}
