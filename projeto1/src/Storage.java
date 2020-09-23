import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class Storage implements Serializable {
    private int capacity;
    private int usedSpace;
    private ConcurrentHashMap<String, Chunk> chunks; //String -> fileID_chunkNo; Chunk -> chunk

    private ArrayList<FileInfo> files;

    //chunk replication degree of chunks stored in other peers
    private ConcurrentHashMap<String, Integer> chunkReplicationDeg; //String -> fileID_chunkNo; Integer -> current rep degree

    private ArrayList<Chunk> restoreWantedChunks;


    public Storage(){
        this.chunks = new ConcurrentHashMap<>();
        this.chunkReplicationDeg = new ConcurrentHashMap<>();
        this.files = new ArrayList<>();
        this.capacity = 2000000000; //2000 Mbytes = 2000000 Kbytes
        this.usedSpace = 0;
    }

    public void addFile(FileInfo file){
        this.files.add(file);
    }

    public boolean exists(String fileID, int chunkNo){
        String key = fileID + "_" + chunkNo;
        return chunks.containsKey(key);
    }

    public boolean hasSpace(int size) {
        return usedSpace + size < capacity;
    }

    public boolean storeChunk(Chunk chunk) {
        if(exists(chunk.getFileId(), chunk.getNumber()))
            return false;
        String key = chunk.getFileId() + "_" + chunk.getNumber();
        chunks.put(key, chunk);
        usedSpace += chunk.getSize();
        chunk.incrementCurRepDeg();
        return true;
    }

    public Chunk getChunk(String fileId, int chunkNo) {
        return chunks.get(fileId + "_" + chunkNo);
    }

    public void addChunkReplicationDeg(String key, int rep_degree) {
        this.chunkReplicationDeg.put(key, rep_degree);
    }

    public boolean isMyChunk(String fileId, int chunkNo) {
        return this.chunkReplicationDeg.containsKey(fileId + "_" + chunkNo);
    }

    public void incrementChunkRepDegree(String fileId, int chunkNo) {
        String key = fileId + "_" + chunkNo;
        this.chunkReplicationDeg.replace(key, this.chunkReplicationDeg.get(key)+1);
    }

    public void decrementChunkRepDegree(String fileId, int chunkNo) {
        String key = fileId + "_" + chunkNo;
        this.chunkReplicationDeg.replace(key, this.chunkReplicationDeg.get(key)-1);
    }

    public int getChunkReplicationDegree(String fileId, int chunkNo){
        String key = fileId + "_" + chunkNo;
        return this.chunkReplicationDeg.get(key);
    }

    public ArrayList<FileInfo> getFiles() {
        return files;
    }

    public void startRestoreWantedChunks() {
        restoreWantedChunks = new ArrayList<>();
    }

    public ArrayList<Chunk> getRestoreWantedChunks(){
        return restoreWantedChunks;
    }

    public void addChunkToRestoredChunks(Chunk chunk){
        this.restoreWantedChunks.add(chunk);
    }

    public String getFileName(String fileId) {
        for(FileInfo file: files)
            if(file.getID().equals(fileId)){
                return file.getFilePathName();
            }
        return "FileNameNotFound";
    }

    public ArrayList<Chunk> getAllChunks(){
        Collection<Chunk> values = this.chunks.values();
        return new ArrayList<>(values);
    }

    public int getCapacity() {
        return capacity;
    }

    public int getUsedSpace() {
        return usedSpace;
    }

    public void deleteFile(FileInfo file) {
        this.files.remove(file);
    }

    public void deleteFileIdChunks(String fileId) {
        ArrayList<Chunk> chunks = getAllChunks();

        for(Chunk chunk: chunks){
            if(chunk.getFileId().equals(fileId)){
                String key = chunk.getFileId() + "_" + chunk.getNumber();
                this.chunks.remove(key);
                this.chunkReplicationDeg.remove(key);
                usedSpace -= chunk.getSize();
            }
        }
    }

    public boolean reclaim(int space, Peer peer) {
        if(space >= this.usedSpace){
            System.out.println("a");
            this.capacity = space;
            return true;
        }

        //remove chunks with currentReplicationDegree > desiredReplicationDegree
        ArrayList<Chunk> all_chunks = this.getAllChunks();
        for(Chunk c : all_chunks){
            if(c.getCurrRepDeg() > c.getDesRepDeg()){
                String key = c.getFileId() + "_" + c.getNumber();
                this.chunks.remove(key);
                this.chunkReplicationDeg.remove(key);
                usedSpace -= c.getSize();

                //send message to other peers
                String header = peer.getVersion() + " REMOVED " + peer.getID() + " " + c.getFileId() + " " + c.getNumber() + " \r\n\r\n";
                byte[] msg = header.getBytes(US_ASCII);
                peer.getScheduler().execute(new MessageSender(peer, "MC", msg));

                //test if there is enough space
                if(space >= this.usedSpace){
                    this.capacity = space;
                    return true;
                }
            }
        }

        //remove any chunk
        all_chunks = this.getAllChunks();
        for(Chunk c : all_chunks){

            String key = c.getFileId() + "_" + c.getNumber();
            this.chunks.remove(key);
            this.chunkReplicationDeg.remove(key);
            usedSpace -= c.getSize();

            //send message to other peers
            String header = peer.getVersion() + " REMOVED " + peer.getID() + " " + c.getFileId() + " " + c.getNumber() + " \r\n\r\n";
            byte[] msg = header.getBytes(US_ASCII);
            peer.getScheduler().execute(new MessageSender(peer, "MC", msg));

            //test if there is enough space
            if(space >= this.usedSpace){
                this.capacity = space;
                return true;
            }

        }

        return false;
    }
}
