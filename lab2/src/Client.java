import java.io.IOException;
import java.net.*;

public class Client {
    public static void main(String[] args) {

        //verificar syntax
        if (args.length < 4 || args.length > 5) {
            System.out.println("Syntax: java Client <mcast_addr> <mcast_port> <oper> <opnd> *");
            return;
        }

        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String operation = args[2];

        if (operation.equals("register") && args.length != 5) {
            System.out.println("Syntax: java Client <host> <port> <oper> <opnd>*\n");
            System.out.println("If <oper> = \"register\" => <opnd>* = <DNS name> <IP address>");
            return;
        } else if (operation.equals("lookup") && args.length != 4) {
            System.out.println("Syntax: java Client <host> <port> <oper> <opnd>*\n");
            System.out.println("If <oper> = \"lookup\" => <opnd>* = <DNS name>");
            return;
        }

        try {
            //criar socket
            InetAddress address = InetAddress.getByName(host);
            DatagramSocket socket = new DatagramSocket();

            //criar e enviar packet para o pedido
            DatagramPacket request;
            if (operation.equals("register")) {
                request = register(args[3], args[4], address, port);
                socket.send(request);
            }
            else if (operation.equals("lookup")) {
                request = lookup(args[3], address, port);
                socket.send(request);
            }

            //receber packet com a resposta
            byte[] buffer = new byte[512];
            DatagramPacket response = new DatagramPacket(buffer, buffer.length);
            socket.receive(response);

            //preparar mensagem para imprimir no terminal
            String reply = new String(response.getData());
            String output = "Client: " + operation + " " + args[3];

            if(operation.equals("register")){
                output += " " + args[4];
            }

            output += " : ";

            if(reply.equals("-1") || reply.equals("NOT_FOUND")){
                output += "ERROR\n";
            }
            else{
                output += reply + "\n";
            }

            System.out.println(output);

            socket.close();

        } catch (SocketTimeoutException ex) {
            System.out.println("Timeout error: " + ex.getMessage());
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("Client error: " + ex.getMessage());
            ex.printStackTrace();
        }

    }

    public static DatagramPacket register(String dns, String ip, InetAddress address, int port){
        String buf = "REGISTER " + dns + " " + ip;
        DatagramPacket packet = new DatagramPacket(buf.getBytes(), buf.length(), address, port);
        return packet;
    }

    public static DatagramPacket lookup(String dns, InetAddress address, int port){
        String buf = "LOOKUP " + dns;
        DatagramPacket packet = new DatagramPacket(buf.getBytes(), buf.length(), address, port);
        return packet;
    }
}
