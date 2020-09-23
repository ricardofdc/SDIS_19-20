import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class GetChunkThread implements Runnable {
    private final Peer peer;
    private final double version;
    private final String fileId;
    private final int chunkNo;

    public GetChunkThread(Peer peer, double version, String fileId, int chunkNo) {
        this.peer = peer;
        this.version = version;
        this.fileId = fileId;
        this.chunkNo = chunkNo;
    }

    @Override
    public void run() {
        if(this.peer.getStorage().exists(fileId, chunkNo)){
            int numberChunkMessages = this.peer.getNumberChunkMessages(fileId, chunkNo);
            String header = this.version + " CHUNK " + this.peer.getID() + " " + this.fileId + " " +
                    this.chunkNo + " \r\n\r\n";

            byte[] header_bytes = header.getBytes(US_ASCII);
            byte[] body = this.peer.getStorage().getChunk(fileId, chunkNo).getContent();
            byte[] msg = Utils.concatenateArrays(header_bytes, body);

            Random rand = new Random();

            try {
                Thread.sleep(rand.nextInt(401));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if(numberChunkMessages != this.peer.getNumberChunkMessages(fileId, chunkNo)) //it means someone has already sent this chunk
                return;

            this.peer.incrementNumberChunkMessages(fileId, chunkNo);
            this.peer.getScheduler().schedule(new MessageSender(this.peer, "MDR", msg),
                    rand.nextInt(401), TimeUnit.MILLISECONDS);
        }
    }
}
