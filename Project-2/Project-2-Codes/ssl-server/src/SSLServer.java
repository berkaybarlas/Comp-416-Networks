import javax.net.ssl.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;

import java.io.File;
import java.io.IOException;


/**
 * Copyright [2017] [Yahya Hassanzadeh-Nazarabadi]

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
public class SSLServer extends Thread
{
    private final String KS_FILE = "keystore.jks";
    private final String KS_PASS = "storepass";
    private final String SK_PASS = "keypass";
    public final static String FILE_TO_SEND = "server_crt.crt";
    private SSLServerSocket sslSocket;
    private SSLServerSocketFactory sslFactory;

    //private ServerControlPanel serverControlPanel;


    public SSLServer(int port)
    {

        try
        {

            /*
            Instance of SSL protocol with TLS variance
             */
            SSLContext sc = SSLContext.getInstance("TLS");

            /*
            Key management of the server
             */
            char ksPass[] = KS_PASS.toCharArray();
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(new FileInputStream(KS_FILE), ksPass);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, SK_PASS.toCharArray());
            sc.init(kmf.getKeyManagers(), null, null);


            /*
            SSL socket factory which creates SSLSockets
             */
            sslFactory = sc.getServerSocketFactory();
            sslSocket = (SSLServerSocket) sslFactory.createServerSocket(port);

            System.out.println("SSL server is up and running on port " + port);
            while (true)
            {
                ListenAndAccept();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    /*
    Listens to the line and starts a connection on receiving a request with the client
     */
    private void ListenAndAccept()
    {
        SSLSocket s;
        try
        {
            s = (SSLSocket) sslSocket.accept();
            System.out.println("An SSL connection was established with a client on the address of " + s.getRemoteSocketAddress());
            SSLServerThread st = new SSLServerThread(s);
            st.start();

        }

        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Server Class.Connection establishment error inside listen and accept function");
        }
    }




}
