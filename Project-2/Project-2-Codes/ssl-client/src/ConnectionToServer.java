import java.io.*;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by Yahya Hassanzadeh on 20/09/2017.
 */

public class ConnectionToServer
{
    private int MAX_FILE_SIZE = 10000000; // 10mb
    private String CLIENT_DOWNLOAD_PATH = "/Project-2-Codes/ssl-client/certs/";
    static public final int MAX_BYTE_SIZE = 1024;
    public static final int DEFAULT_SERVER_PORT = 4444;
    public static final String DEFAULT_SERVER_ADDRESS = "localhost";

    protected Socket s;
    protected DataInputStream is;
    protected DataOutputStream os;

    protected String serverAddress;
    protected int serverPort;

    /**
     *
     * @param address IP address of the server, if you are running the server on the same computer as client, put the address as "localhost"
     * @param port port number of the server
     */
    public ConnectionToServer(String address, int port)
    {
        serverAddress = address;
        serverPort    = port;
    }

    protected void connectToServer() throws IOException {
        s = new Socket(serverAddress, serverPort);
        initDownloadPath();
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
    public String sendForAnswer(String message)
    {
        String response = "";
        try
        {
            /*
            Sends the message to the server via Data Stream
             */
            byte[] byteM = message.getBytes();
            os.writeInt(byteM.length);
            os.write(byteM);
            os.flush();
            /*
            Reads a line from the server via Data Stream
             */
            byte[] data = new byte[MAX_BYTE_SIZE];
            is.read(data);
            response = new String(data);
        }
        catch(IOException e)
        {
            e.printStackTrace();
            System.out.println("ConnectionToServer: Socket read Error");
        }
        return response;
    }

    public void askForCertificate() {
        System.out.println("Asking Certificate from Server");
        try
        {
            /*
            Sends the message to the server via Data Stream
             */
            os.write("CERT".getBytes());
            os.flush();
            /*
            Reads a line from the server via Data Stream
             */
            waitForFile("server_crt","crt");
        }
        catch(IOException e)
        {
            e.printStackTrace();
            System.out.println("ConnectionToServer: Socket read Error");
        }
    }

    public byte[] waitForFile(String fileName, String fileType) {
        byte [] fileByteArray  = new byte [MAX_FILE_SIZE];
        int bytesRead;
        int currentSize = 0;
        try
        {
            // receive file

            String outputFileName = fileName + "." + fileType;
            String localDir = System.getProperty("user.dir");
            String fileLoc = localDir + CLIENT_DOWNLOAD_PATH + outputFileName;

            FileOutputStream fos = new FileOutputStream(fileLoc);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            System.out.println("Wait for " + fileType + " File...");

            // Read until transfer ends
            bytesRead = is.read(fileByteArray,0,fileByteArray.length);
            currentSize = bytesRead;
            System.out.println("File receiving with size:  " + currentSize);
            // read to file
            bos.write(fileByteArray, 0 , currentSize);
            bos.flush();

            bos.close();

            System.out.println("File recieved and saved to: " + fileLoc);

        }
        catch (IOException e)
        {
            //e.printStackTrace();
            System.err.println("Error: no server has been found on " + serverAddress + "/" + serverPort);
        }
        return Arrays.copyOfRange(fileByteArray, 0, currentSize);
    }

    private void initDownloadPath() {
        String localDir = System.getProperty("user.dir");
        File directory = new File(localDir + CLIENT_DOWNLOAD_PATH);
        if (! directory.exists()){
            directory.mkdirs();
        }
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
            System.out.println("BaseClient: Connection Closed");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        } catch (NullPointerException e) {
            //
        }
    }
}
