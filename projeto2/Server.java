import java.io.*;
import javax.net.ssl.*;
import java.security.*;
import java.net.*;

public class Server {

    public static void main(String[] args) {
        // try {
        //     SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();  
            
        //    SSLServerSocket server = (SSLServerSocket) ssf.createServerSocket(Integer.parseInt(args[1]), 0, new InetAddress(args[0]));

        //     server.setNeedClientAuth(true);
        //     String[] protocols = new String[1];
        //     protocols[0] = "TLSv1.2";
        //     server.setEnabledProtocols(protocols);
        //     server.setEnabledCipherSuites(server.getSupportedCipherSuites());

        //     SSLSocket socket;

        //     while (true) {
        //         socket = (SSLSocket) server.accept();
        //         // this.node.getExecutor().execute(new Request(this.node, sslSocket));
        //     }
        // } 
        // catch (Exception e) {
        //     e.printStackTrace();
        // }
    }

}