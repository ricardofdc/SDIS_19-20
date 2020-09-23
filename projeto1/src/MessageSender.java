import java.io.FileOutputStream;

public class MessageSender implements Runnable {
    private Peer peer;
    private String channel;
    private byte[] message;

    public MessageSender(Peer peer, String channel, byte[] message){
        this.peer = peer;
        this.channel = channel;
        this.message = message;
    }

    @Override
    public void run() {
        this.peer.getChannel(this.channel).sendMessage(this.message);
    }
}
