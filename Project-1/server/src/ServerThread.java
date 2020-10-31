import data.DataServer;
import utils.MessageProtocol;
import utils.MessageType;

import java.io.*;
import java.net.Socket;

class ServerThread extends Thread
{
    // Timeout time in millisecond
    private final int TIME_OUT = 10000;
    protected DataInputStream is;
    protected DataOutputStream os;
    protected Socket s;
    private DataServer dataServerThread;
    private MessageProtocol message;
    private String line = new String();
    private String lines = new String();

    /**
     * Creates a server thread on the input socket
     *
     * @param s input socket to create a thread on
     */
    public ServerThread(Socket s)
    {
        this.s = s;
    }

    /**
     * The server thread, echos the client until it receives the QUIT string from the client
     */
    public void run()
    {
        try
        {
            is = new DataInputStream(s.getInputStream());
            os = new DataOutputStream(s.getOutputStream());

        }
        catch (IOException e)
        {
            System.err.println("Server Thread. Run. IO error in server thread");
        }

        try
        {
            byte[] data = new byte[MessageProtocol.MAX_BYTE_SIZE];
            is.read(data);
            message = new MessageProtocol(data);
            while (message.payload.compareTo("QUIT") != 0)
            {
                // TODO: IMPLEMENT AUTHENTICATION
                // TODO: DATA COMMUNICATION
                // TODO: TIMEOUT
                // TODO: WHETHER API
		        lines = "Client messaged : " + message.payload + " at  : " + Thread.currentThread().getId();

                System.out.println("Client " + s.getRemoteSocketAddress() + " sent :  " + lines);
                switch (MessageType.getMessageType(message.type)) {
                    case AUTH_REQUEST:
                        sendMessageToClient(MessageType.AUTH_SUCCESS, "Success");
                        break;
                    case API_REQUEST:
                        sendMessageToClient(MessageType.API_RESPONSE, "Success");
                        break;
                    case API_REQUEST_DATA:
                        String clientID = message.payload;
                        dataServerThread.getDSThread(clientID);
                        sendMessageToClient(MessageType.API_DATA_HASH, "HASH");
                        break;
                    default:
                        sendMessageToClient(MessageType.AUTH_CHALLANGE, "Question");
                }

                is.read(data);
                message = new MessageProtocol(data);
            }
        }
        catch (IOException e)
        {
            line = this.getName(); //reused String line for getting thread name
            System.err.println("Server Thread. Run. IO Error/ Client " + line + " terminated abruptly");
        }
        catch (NullPointerException e)
        {
            line = this.getName(); //reused String line for getting thread name
            System.err.println("Server Thread. Run.Client " + line + " Closed");
        } finally
        {
            closeThreadAndSocket();
        }//end finally
    }

    /**
     * The function to send a string message to the client
     *
     * @param str the string to be sent as message
     */
    private void sendMessageToClient(MessageType type, String str) throws IOException{
        MessageProtocol message = new MessageProtocol(type.value, str);
        System.out.println("sendMessageToClient: " + str);
        os.write(message.getByteMessage());
        os.flush();
    }

    private void closeThreadAndSocket() {
        try
        {
            System.out.println("Closing the connection");
            if (is != null)
            {
                is.close();
                System.err.println(" Socket Input Stream Closed");
            }

            if (os != null)
            {
                os.close();
                System.err.println("Socket Out Closed");
            }
            if (s != null)
            {
                s.close();
                System.err.println("Socket Closed");
            }

        }
        catch (IOException ie)
        {
            System.err.println("Socket Close Error");
        }
    }
}
