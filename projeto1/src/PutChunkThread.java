import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class PutChunkThread implements Runnable{
    private double version;
    private String fileId;
    private int chunkNo;
    private int replicationDeg;
    private byte[] body;
    private Peer peer;

    public PutChunkThread(Peer peer, double version, String fileId, int chunkNo, int replicationDeg, byte[] body){
        this.version = version;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
        this.replicationDeg = replicationDeg;
        this.body = body;
        this.peer = peer;
    }

    @Override
    public void run() {
        Chunk chunk = new Chunk(chunkNo, fileId, body, body.length, replicationDeg);
        if(!this.peer.getStorage().storeChunk(chunk))
            return;

        String header = this.version + " STORED " + this.peer.getID() + " " + this.fileId + " " +
                this.chunkNo + " \r\n\r\n";

        Random rand = new Random();
        byte[] msg = header.getBytes(US_ASCII);
        this.peer.getScheduler().schedule(new MessageSender(this.peer, "MC", msg), rand.nextInt(401), TimeUnit.MILLISECONDS);
    }
}
