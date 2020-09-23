import java.io.Serializable;

public class Chunk implements Serializable {
    private String fileID;
    private int number;
    private int size; //All chunks of a file, except the last one, have the maximum size (64000 bytes)
    private byte[] content;
    private int desiredReplicationDegree;
    private int currentReplicationDegree;

    static int MAX_CHUNK_SIZE = 64000;

    public Chunk(int chunkNo, String fileID, byte[] content, int size, int desiredReplicationDegree){
        this.number = chunkNo;
        this.fileID = fileID;
        this.content = content;
        this.size = size;
        this.desiredReplicationDegree = desiredReplicationDegree;
        this.currentReplicationDegree = 0;
    }

    public int getNumber() {
        return number;
    }

    public byte[] getContent() {
        return content;
    }

    public void incrementCurRepDeg(){
        currentReplicationDegree++;
    }

    public void decrementCurRepDeg(){
        currentReplicationDegree--;
    }

    public String getFileId() {
        return fileID;
    }

    public int getSize() {
        return size;
    }

    public int getCurrRepDeg() {
        return currentReplicationDegree;
    }

    public int getDesRepDeg() {
        return desiredReplicationDegree;
    }
}