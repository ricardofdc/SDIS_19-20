public class DeleteFileInfoThread implements Runnable {
    private String fileId;
    private Peer peer;
    private double version;

    public DeleteFileInfoThread(Peer peer, double version, String fileId) {
        this.peer = peer;
        this.version = version;
        this.fileId = fileId;
    }

    @Override
    public void run() {
        for(FileInfo file: this.peer.getStorage().getFiles()){
            if(this.fileId.equals(file.getID())) {
                this.peer.getStorage().deleteFile(file);
                System.out.println("File " + this.fileId + " deleted.");
                return;
            }
        }
    }
}

