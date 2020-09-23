import java.io.IOException;
import java.net.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Server {

    private static DatagramSocket socket;
    private static Map<String, String> table;

    public Server(int port) throws SocketException {
        //criar socket
        socket = new DatagramSocket(port);

        //criar tabela
        table = new HashMap<>();
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Syntax: java Server <port_number>");
            return;
        }

        int port = Integer.parseInt(args[0]);

        try {
            Server server = new Server(port);
            server.run();

        } catch (IOException e) {
            System.out.println("Server exception.");
            e.printStackTrace();
        }
    }

    public void run() throws IOException {
        byte[] buffer = new byte[512];

        while (true) {
            //cria packet para o pedido vindo do cliente
            DatagramPacket request = new DatagramPacket(buffer, buffer.length);
            socket.receive(request);

            //extrair informacao do packet
            InetAddress clientAdress = request.getAddress();
            int clientPort = request.getPort();
            String operation = new String(buffer, 0, request.getLength());
            //System.out.println(operation);
            String[] operands = operation.split(" ");


            //efetuar operacao pedida
            String replyBuffer = "";
            String output = "Server: " + operation;
            if (operands[0].equals("REGISTER")) {
                replyBuffer = register(operands[1], operands[2]);
            } else if (operands[0].equals("LOOKUP")) {
                replyBuffer = lookup(operands[1]);
            } else {
                System.out.println("Server: Invalid operation");
                continue;
            }
            System.out.println(output);

            //criar packet e enviar resposta
            DatagramPacket reply = new DatagramPacket(replyBuffer.getBytes(), replyBuffer.length(), clientAdress, clientPort);
            socket.send(reply);

            //eliminar a informação do packet do array de operancoes
            Arrays.fill(operands,null);
        }
    }

    private String lookup(String dns) {
        String ip;

        //verifica se o IP esta associado a algum DNS da tabela
        if(null == (ip = table.get(dns))){
            return "NOT_FOUND";
        }

        return dns + " " + ip;
    }

    private String register(String dns, String ip) {

        //verifica se o ip esta no formato correto
        String ipPattern ="(([0-1]?[0-9]?[0-9]?|2[0-4][0-9]|25[0-5])\\.){3}([0-1]?[0-9]?[0-9]?|2[0-4][0-9]|25[0-5]){1}";
        Pattern r = Pattern.compile(ipPattern);
        Matcher m = r.matcher(ip);

        //verifica se o DNS ja existe na tabela
        if(table.get(dns) != null || !m.find()){
            return "-1";
        }

        //coloca o DNS e o IP na tabela
        table.put(dns, ip);
        return Integer.toString(table.size());
    }
}
