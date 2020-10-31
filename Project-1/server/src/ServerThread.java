import data.DataServer;
import utils.MessageProtocol;
import utils.MessageType;

import java.io.*;
import java.net.Socket;
import java.util.*;

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
    private HashMap<String, HashMap<String, String>> QnAs = new HashMap<String, HashMap<String, String>>();
    private String username = new String();
    private String question = new String();
    private String token = new String();
    private int prime1 = 7;
    private int prime2 = 31;
    private int hash = prime1;

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
                QnAs = QuestionsAndAnswers.retrieve();
                QnAs.entrySet().forEach(entry->{
                    System.out.println(entry.getKey() + " " + entry.getValue());
                });
                System.out.println("Client " + s.getRemoteSocketAddress() + " sent :  " + lines);
                switch (MessageType.getMessageType(message.type)) {
                    case AUTH_REQUEST:
                        if(!QnAs.containsKey(message.payload) && username.equals(""))
                        {
                            sendMessageToClient(MessageType.AUTH_FAILURE, "User Does Not Exist\n");
                            break;
                        }
                        if (QnAs.containsKey(message.payload))
                        {
                            username = message.payload;
                            question = QnAs.get(username).get(username);
                            sendMessageToClient(MessageType.AUTH_CHALLANGE, question);
                            break;
                        }
                        else if (QnAs.get(username).containsValue(message.payload))
                        {
                            if (QnAs.get(username).get(question).equals(message.payload))
                            {
                                if (QnAs.get(username).get(message.payload).equals("Success"))
                                {
                                    // 1.generate a random number
                                    // 2.concat it to username
                                    // 3.hash = hash*prime2 + charAt(i) for all i in text
                                    Random rand = new Random();
                                    int rand1 = rand.nextInt();
                                    username += rand1;
                                    for (int i=0; i<username.length(); i++)
                                    {
                                        hash = hash*prime2 + username.charAt(i);
                                    }
                                    token = hash + "";
                                    sendMessageToClient(MessageType.AUTH_SUCCESS, token);
                                    break;
                                }
                                else
                                {
                                    question = QnAs.get(username).get(message.payload);
                                    sendMessageToClient(MessageType.AUTH_CHALLANGE, question);
                                    break;
                                }
                            }
                            else
                            {
                                sendMessageToClient(MessageType.AUTH_FAILURE, "Incorrect Answer\n");
                                break;
                            }
                        }
                        else
                        {
                            sendMessageToClient(MessageType.AUTH_FAILURE, "Incorrect Answer\n");
                            break;
                        }
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
