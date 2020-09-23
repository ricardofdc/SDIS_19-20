import java.util.ArrayList;
import java.io.*;
import java.math.BigInteger;

public class Chunk implements Serializable {

    private int number;
    private int desiredRd;
    private int currentRd = 0;
    private byte[] data;
    private ArrayList<BigInteger> peerStoring;
    private String fileID;
    private String originPeerAddress;
    private Integer originPeerPort;

    public Chunk(int number, byte[] data, String fileID){

        this.number = number;
        this.data = data;
        this.peerStoring = new ArrayList<BigInteger>();
        this.fileID = fileID;
    }

    public Chunk(int number, byte[] data, String fileID, String originPeerAddress, Integer originPeerPort) {

        this.number = number;
        this.data = data;
        this.peerStoring = new ArrayList<BigInteger>();
        this.fileID = fileID;
        this.originPeerAddress = originPeerAddress;
        this.originPeerPort = originPeerPort;
    }

    public byte[] getData(){
        return data;
    }

    public int getNumber(){
        return this.number;
    }

    public int getDesiredRd(){

        return this.desiredRd;
    }

    public int getRD(){
        return this.currentRd;
    }

    public String getFileID() {
        return this.fileID;
    }

    public String getOriginalPeerAddress() {
        return this.originPeerAddress;
    }

    public Integer getOriginalPeerPort() {
        return this.originPeerPort;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public void setDesiredRd(int desiredRd) {
        this.desiredRd = desiredRd;
    }

    public void setRd(int rd) {
        this.currentRd = rd;
    }

    public void addStorer(BigInteger peerID) {
        if(peerStoring.contains(peerID))
            return;
        peerStoring.add(peerID);
        this.currentRd++;
    }

    public void storeBackupChunk(BigInteger peerID) {
        File chunk = new File("chordNode_" + peerID + "/backup/" + this.fileID + "/" + Integer.toString(this.number));
        chunk.getParentFile().mkdirs();

        try {
            FileOutputStream out = new FileOutputStream(chunk);
            out.write(this.data);
            out.close();
        }

        catch(Exception e) {
            e.printStackTrace();

        }
    }
}