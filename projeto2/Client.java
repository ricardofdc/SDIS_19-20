import javax.net.ssl.SSLSocket;
import javax.net.ssl.*;

public class Client {

    public static void main(String[] args) {
        try {
            SSLSocketFactory ssf = (SSLSocketFactory) SSLSocketFactory.getDefault();  

            SSLSocket s = (SSLSocket) ssf.createSocket(args[0], Integer.parseInt(args[1]));

            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}