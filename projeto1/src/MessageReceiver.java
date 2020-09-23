import java.io.*;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.US_ASCII;


public class MessageReceiver implements Runnable {
    private final byte[] message;
    private final Peer peer;
    private final int size;
    private String[] msg;
    private static int restoreAskerID = 0;
    private static ConcurrentHashMap<String, Integer> numPutChunkMessages = new ConcurrentHashMap<>();

    public MessageReceiver(Peer peer, byte[] buf, int size) {
        this.peer = peer;
        this.size = size;
        this.message = Arrays.copyOfRange(buf, 0, size);
    }

    @Override
    public void run() {
        this.msg = new String(this.message, 0, size).split("\r\n\r\n");
        this.msg[0] = this.msg[0].trim().replaceAll( " +", " "); //remove extra white spaces in header
        String[] header = this.msg[0].split(" ");

        switch (header[1]){
            case "PUTCHUNK":
                putChunk();
                break;
            case "STORED":
                stored();
                break;
            case "GETCHUNK":
                getchunk();
                break;
            case "CHUNK":
                chunk();
                break;
            case "DELETE":
                delete();
                break;
            case "REMOVED":
                removed();
                break;
            default:
                System.out.println("Message type not recognised:");
                System.out.println(msg[0]);
                break;
        }
    }

    private void removed() {
        String[] header = this.msg[0].split(" ");

        double version = Double.parseDouble(header[0]);
        int senderId = Integer.parseInt(header[2]);
        String fileId = header[3];
        int chunkNo = Integer.parseInt(header[4]);


        if(senderId == this.peer.getID()) {
            return;
        }

        this.peer.getStorage().decrementChunkRepDegree(fileId, chunkNo);

        if(this.peer.getStorage().exists(fileId, chunkNo)){
            Chunk chunk = this.peer.getStorage().getChunk(fileId, chunkNo);
            chunk.decrementCurRepDeg();

            if(chunk.getCurrRepDeg() < chunk.getDesRepDeg()){
                int num_putChunk = numPutChunkMessages.get(chunk.getFileId() + "_" + chunk.getNumber());

                String header2 = this.peer.getVersion() + " PUTCHUNK " + this.peer.getID() + " " + chunk.getFileId() + " " +
                        chunk.getNumber() + " " + chunk.getDesRepDeg() + " \r\n\r\n";
                byte[] header_bytes = header2.getBytes(US_ASCII);
                byte[] body = chunk.getContent();

                byte[] msg = Utils.concatenateArrays(header_bytes, body);

                Random rand = new Random();

                try {
                    Thread.sleep(rand.nextInt(401));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if(numPutChunkMessages.get(chunk.getFileId() + "_" + chunk.getNumber()) != num_putChunk){
                    this.peer.getScheduler().execute(new MessageSender(this.peer, "MDB", msg));
                }

            }
        }


    }

    private void delete() {
        String[] header = this.msg[0].split(" ");

        double version = Double.parseDouble(header[0]);
        int senderId = Integer.parseInt(header[2]);
        String fileId = header[3];

        if(this.peer.getID() == senderId)
            this.peer.getScheduler().execute(new DeleteFileInfoThread(this.peer, version, fileId));
        else this.peer.getScheduler().execute(new DeleteChunksThread(this.peer, version, fileId));

    }

    private void getchunk() {
        String[] header = this.msg[0].split(" ");

        double version = Double.parseDouble(header[0]);
        int senderId = Integer.parseInt(header[2]);
        String fileId = header[3];
        int chunkNo = Integer.parseInt(header[4]);

        restoreAskerID = senderId;

        if(this.peer.getID() == senderId){
            return;
        }

        this.peer.getScheduler().execute(new GetChunkThread(this.peer, version, fileId, chunkNo));
    }

    private void chunk() {
        String[] header = this.msg[0].split(" ");
        byte[] body;
        if((body = getBody()) == null)
            return;

        double version = Double.parseDouble(header[0]);
        int senderId = Integer.parseInt(header[2]);
        String fileId = header[3];
        int chunkNo = Integer.parseInt(header[4]);

        if(this.peer.getID() == senderId){
            return;
        }

        if(restoreAskerID == this.peer.getID()){
            Chunk chunk = new Chunk(chunkNo, fileId, body, body.length, 0);
            for(Chunk c: this.peer.getStorage().getRestoreWantedChunks()){
                if(c.getFileId().equals(fileId) && c.getNumber() == chunkNo)
                    return;
            }
            this.peer.getStorage().addChunkToRestoredChunks(chunk);
            if(this.peer.getStorage().getRestoreWantedChunks().size() == this.peer.getNumberChunksToRestore()){ //all chunks gathered

                System.out.println("All chunks gathered. Building file.");
                buildRestoredFile(fileId);
            }
            else System.out.println("Chunks still missing. Collecting chunks.");
        }
        else{
            peer.incrementNumberChunkMessages(fileId, chunkNo);
        }




    }

    private void buildRestoredFile(String fileId) {
        try {
            String filename = "Peer" + this.peer.getID() + Utils.getPathSeperator() + "RestoredFiles" +
                    Utils.getPathSeperator() + this.peer.getStorage().getFileName(fileId);
            File file = new File(filename);
            if(!file.exists()){
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            RandomAccessFile f = new RandomAccessFile(filename, "rw");

            byte[] buf;
            char[] char_buf = new char[Chunk.MAX_CHUNK_SIZE/2];

            //write chunks to new file in correct order
            for(int i=0; i<this.peer.getNumberChunksToRestore(); i++){
                for(Chunk chunk : this.peer.getStorage().getRestoreWantedChunks()){
                    if(chunk.getNumber() == i){
                        buf = chunk.getContent();

                        f.write(buf);
                        buf = new byte[Chunk.MAX_CHUNK_SIZE];
                        break;

                    }
                }
            }

            System.out.println("File restored successfully");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void stored() {
        String[] header = this.msg[0].split(" ");

        double version = Double.parseDouble(header[0]);
        int senderId = Integer.parseInt(header[2]);
        String fileId = header[3];
        int chunkNo = Integer.parseInt(header[4]);

        if(this.peer.getID() == senderId)
            return;
        if(this.peer.getStorage().exists(fileId, chunkNo)) {
            Chunk chunk = this.peer.getStorage().getChunk(fileId, chunkNo);
            chunk.incrementCurRepDeg();
        }
        if(this.peer.getStorage().isMyChunk(fileId, chunkNo)){
            this.peer.getStorage().incrementChunkRepDegree(fileId, chunkNo);
        }

    }

    private void putChunk() {
        String[] header = this.msg[0].split(" ");
        byte[] body;
        if((body = getBody()) == null)
            return;

        double version = Double.parseDouble(header[0]);
        int senderId = Integer.parseInt(header[2]);
        String fileId = header[3];
        int chunkNo = Integer.parseInt(header[4]);
        int replicationDeg = Integer.parseInt(header[5]);
        Storage storage = this.peer.getStorage();

        //to work with remove()
        String key = fileId + "_" + chunkNo;
        if(numPutChunkMessages.containsKey(key))
            numPutChunkMessages.replace(key, numPutChunkMessages.get(key)+1);
        else numPutChunkMessages.put(fileId + "_" + chunkNo, 0);
        ///

        if(this.peer.getID() == senderId || !storage.hasSpace(body.length) || storage.exists(fileId, chunkNo)){
            return;
        }

        this.peer.getScheduler().execute(new PutChunkThread(this.peer, version, fileId, chunkNo, replicationDeg, body));
    }

    private byte[] getBody(){
        byte[] bytes = this.message;
        for (int i = 0; i < bytes.length; i++) {
            byte b = bytes[i];
            if (bytes[i] == 0xD && bytes[i+1] == 0xA && bytes[i+2] == 0xD && bytes[i+3] == 0xA){
                return Arrays.copyOfRange(bytes, i+4, bytes.length);
            }
        }
        return null;
    }
}
