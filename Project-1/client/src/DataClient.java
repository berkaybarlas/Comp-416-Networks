import models.BaseClient;
import utils.MessageProtocol;

import java.io.*;
import java.util.Arrays;

public class DataClient extends BaseClient {
    private int MAX_FILE_SIZE = 10000000; // 10mb
    private String CLIENT_DOWNLOAD_PATH = "/client/downloads/";
    public static final int DEFAULT_DATA_SERVER_PORT = 4545;

    public String clientIdentifier;

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
            // os = new DataOutputStream(s.getOutputStream());
            clientIdentifier = "" + s.getLocalSocketAddress();

            System.out.println("Successfully connected to " + serverAddress + " on port " + serverPort
                    + " with local address" + s.getLocalSocketAddress());
        }
        catch (IOException e)
        {
            System.err.println("Error: no server has been found on " + serverAddress + "/" + serverPort);
        }
    }

    public void waitForData(String dataType) {

    }

    public byte[] waitForData() {
        byte[] data = new byte[MessageProtocol.MAX_BYTE_SIZE];
        try
        {
            is.read(data);
            String response = new MessageProtocol(data).payload;
            System.out.println("Data server response: " + response);
        }
        catch (IOException e)
        {
            //e.printStackTrace();
            System.err.println("Error: no server has been found on " + serverAddress + "/" + serverPort);
        }
        return data;
    }

    public byte[] waitForFile() {
        byte [] fileByteArray  = new byte [MAX_FILE_SIZE];
        int bytesRead;
        int currentSize = 0;
        try
        {
            // receive file

            String outputFileName = "test.png";
            String localDir = System.getProperty("user.dir");
            String fileLoc = localDir + CLIENT_DOWNLOAD_PATH + outputFileName;

            FileOutputStream fos = new FileOutputStream(fileLoc);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            System.out.println("Wait for File...");
            bytesRead = is.read(fileByteArray,0,fileByteArray.length);
            currentSize = bytesRead;

            // Read until transfer ends
            System.out.println("File receiving ... " + currentSize);
//            do {
//                bytesRead = is.read(fileByteArray, currentSize, (fileByteArray.length-currentSize));
//                if(bytesRead >= 0) currentSize += bytesRead;
//                System.out.println("Readfile" + bytesRead);
//            } while(bytesRead > -1);

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
}
