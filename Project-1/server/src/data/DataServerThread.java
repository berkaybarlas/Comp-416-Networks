package data;

import java.io.*;
import java.net.Socket;

public class DataServerThread extends Thread {
    protected DataInputStream is;
    protected DataOutputStream os;
    protected Socket s;
    private String line = new String();
    private String lines = new String();
    /**
     * Creates a data server thread on the input socket
     *
     * @param s input socket to create a thread on
     */
    public DataServerThread(Socket s)
    {
        this.s = s;
        try
        {
//            is = new DataInputStream(s.getInputStream());
            os = new DataOutputStream(s.getOutputStream());


        }
        catch (IOException e)
        {
            System.err.println("Server Thread. Run. IO error in server thread");
        }
    }

    public void sendData(byte[] byteMessage) {
        try
        {
            os.write(byteMessage);
            os.flush();
        }
        catch (IOException e)
        {
            System.err.println("Server Thread. Run. IO error in server thread");
        }
    }

    public void sendFileData(File file) {
        try
        {
            System.out.println("Reading file...");
            byte [] fileByteArray  = new byte [(int)file.length()];
            FileInputStream fis = new FileInputStream(file);

            BufferedInputStream bis = new BufferedInputStream(fis);
            bis.read(fileByteArray,0,fileByteArray.length);
            System.out.println("File read complete. File size: " + fileByteArray.length);
            os.write(fileByteArray,0, fileByteArray.length);
            os.flush();
            System.out.println("File sent...");
        }
        catch (IOException e)
        {
            System.err.println("Server Thread. Run. IO error in server thread");
        }
    }

    public void closeThreadAndSocket() {
        try
        {
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
