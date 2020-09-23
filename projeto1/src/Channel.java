import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

public class Channel implements Runnable{
    protected InetAddress address;
    protected int port;
    protected Peer peer;

    public Channel(String IPAddress, int port, Peer peer){
        try {
            this.address = InetAddress.getByName(IPAddress);
            this.port = port;
            this.peer = peer;
        } catch (UnknownHostException e) {
            System.err.println("Channel unknown IP address. " + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        MulticastSocket ms;
        try {
            ms = new MulticastSocket(port);
            ms.joinGroup(address);

            while (true){
                byte[] buf = new byte[Chunk.MAX_CHUNK_SIZE + 1000];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                ms.receive(packet);

                peer.getScheduler().execute(new MessageReceiver(peer, buf, packet.getLength()));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void sendMessage(byte[] message) {
        try {
            MulticastSocket socket = new MulticastSocket(this.port);
            DatagramPacket packet = new DatagramPacket(message, message.length, this.address, this.port);
            socket.send(packet);
            socket.close();
        } catch (IOException e) {
            System.err.println("Send message error: " + e.toString());
            e.printStackTrace();
        }

    }
}