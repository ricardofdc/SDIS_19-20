public class DeleteChunksThread implements Runnable {
    private String fileId;
    private Peer peer;
    private double version;

    public DeleteChunksThread(Peer peer, double version, String fileId) {
        this.peer = peer;
        this.version = version;
        this.fileId = fileId;
    }

    @Override
    public void run() {
        this.peer.getStorage().deleteFileIdChunks(this.fileId);
    }
}

