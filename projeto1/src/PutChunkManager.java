import java.util.concurrent.TimeUnit;

public class PutChunkManager implements Runnable{

    private final Peer peer;
    private final Chunk chunk;
    private int tries;
    private final int desired_rep_degree;
    private static final int MAX_TRIES = 5;
    private final byte[] msg;
    private int time;

    public PutChunkManager(Peer peer, Chunk chunk, byte[] msg, int time, int rep_degree){
            this.peer = peer;
            this.chunk = chunk;
            this.time = time;
            this.desired_rep_degree = rep_degree;
            this.msg = msg;
            this.tries = 1;
    }

    @Override
    public void run() {
        int curr_rep_degree = this.peer.getStorage().getChunkReplicationDegree(chunk.getFileId(), chunk.getNumber());
        if(curr_rep_degree < desired_rep_degree){
            this.peer.getScheduler().execute(new MessageSender(this.peer, "MDB", this.msg));
            this.time *= 2;
            this.tries++;

            if(this.tries < MAX_TRIES) {
                this.peer.getScheduler().schedule(this, this.time, TimeUnit.SECONDS);
            }
            if(this.tries == MAX_TRIES)
                System.out.println("PUTCHUNK reached limit tries. Current replication degree = " + curr_rep_degree);
        }
        else
            System.out.println("Chunk " + chunk.getNumber() + " stored! Replication degree = " + curr_rep_degree);
    }
}
