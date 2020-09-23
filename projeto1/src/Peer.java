import java.io.*;
import java.io.File;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class Peer implements RMIinterface {
    private String protocol_version;
    private int ID;
    private ConcurrentHashMap<String,Channel> channels;
    private ScheduledThreadPoolExecutor scheduler;
    private Storage storage;
    private ConcurrentHashMap<String, Integer> restore_numberChunkMessages;
    private int numberChunksToRestore;

    public Peer(String prtcl_version, int id, String MCaddr, int MCprt, String MDBaddr, int MDBprt, String MDRaddr, int MDRprt) {
        this.ID = id;
        this.protocol_version = prtcl_version;
        this.restore_numberChunkMessages = new ConcurrentHashMap<>();

        this.channels = new ConcurrentHashMap<>();
        this.channels.put("MC", new Channel(MCaddr, MCprt, this));
        this.channels.put("MDB", new Channel(MDBaddr, MDBprt, this));
        this.channels.put("MDR", new Channel(MDRaddr, MDRprt, this));

        this.scheduler = (ScheduledThreadPoolExecutor) Executors.newScheduledThreadPool(300);

        downloadStorage(); //downloads storage from file if existing or creates new storage file

        for(Channel channel: channels.values()){
            this.scheduler.execute(channel);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(this::uploadStorage)); //saves peer's storage if CTRL-C is pressed
        System.out.println("Peer " + this.ID + " created successfully.");
    }

    private void downloadStorage() {
        try {
            String filename = "Peer" + this.ID + Utils.getPathSeperator() + "storage.txt";
            File file = new File(filename);
            if(!file.exists() ){
                this.storage = new Storage();
                return;
            }
            FileInputStream fin = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fin);
            this.storage = (Storage) in.readObject();
            in.close();
            fin.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Peer " + this.ID + " error: Storage class not found.");
            e.printStackTrace();
        }
    }

    private void uploadStorage(){
        try {
            String filename = "Peer" + this.ID + Utils.getPathSeperator() + "storage.txt";
            File file = new File(filename);
            if(!file.exists()){
                file.getParentFile().mkdirs();
                file.createNewFile();
            }

            FileOutputStream fout = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fout);
            out.writeObject(this.storage);
            out.close();
            fout.close();
            System.out.println("Upload peer" + this.ID + " to " + filename + " successfully.");

        } catch (IOException e) {
            System.err.println("Peer " + this.ID + " error: Storage file creation error.");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if(args.length != 9) {
            System.err.println("Peer usage: java Peer <protocol_version> <peer_id> <peer_ap> <MCaddress> <MCport> <MDBaddress> <MDBport> <MDRaddress> <MDRport>");
            System.exit(1);
        }

        String[] peer_ap = args[2].split(":");
        String peer_ap_name = peer_ap[0];
        int peer_ap_port = Integer.parseInt(peer_ap[1]);

        try {
            Peer p = new Peer(args[0], Integer.parseInt(args[1]), args[3], Integer.parseInt(args[4]), args[5],
                    Integer.parseInt(args[6]), args[7], Integer.parseInt(args[8]));

            RMIinterface stub = (RMIinterface) UnicastRemoteObject.exportObject(p,0);
            Registry reg = LocateRegistry.createRegistry(peer_ap_port);
            reg.rebind(peer_ap_name, stub);

            System.out.println("Remote connection created successfully.");
        }catch (RemoteException e){
            if (e.getMessage().contains("Port already in use")) {
                System.out.println("Port " + peer_ap_port + " already in use. Try to restart with a new one...");
                System.exit(1);
            }
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Peer exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public ScheduledThreadPoolExecutor getScheduler(){
        return scheduler;
    }


    @Override
    public boolean backup(String pathname, int replicationDegree) {
        FileInfo file = new FileInfo(pathname, replicationDegree);
        for(FileInfo f: this.storage.getFiles()){
            if(f.equalTo(file)){
                return false;
            }

        }
        this.storage.addFile(file);

        ArrayList<Chunk> chunks = file.getChunks();
        for(Chunk chunk: chunks){
            this.storage.addChunkReplicationDeg(chunk.getFileId() + "_" + chunk.getNumber(), 0);
            String header = this.protocol_version + " PUTCHUNK " + this.ID + " " + file.getID() + " " +
                    chunk.getNumber() + " " + replicationDegree + " \r\n\r\n";
            byte[] header_bytes = header.getBytes(US_ASCII);
            byte[] body = chunk.getContent();

            byte[] msg = Utils.concatenateArrays(header_bytes, body);

            this.scheduler.execute(new MessageSender(this, "MDB", msg));
            this.scheduler.schedule(new PutChunkManager(this, chunk, msg, 1, replicationDegree), 1, TimeUnit.SECONDS);

        }
        return true;
    }

    @Override
    public boolean restore(String pathname) {
        for(FileInfo file: this.storage.getFiles()){
            if(file.getFilePathName().equals(pathname)){
                numberChunksToRestore = file.getChunks().size();
                this.storage.startRestoreWantedChunks();
                 for(Chunk chunk : file.getChunks()){
                     String header = this.protocol_version + " GETCHUNK " + this.ID + " " + file.getID() + " " +
                             chunk.getNumber() + " \r\n\r\n";
                     byte[] msg = header.getBytes(US_ASCII);

                     this.scheduler.execute(new MessageSender(this, "MC", msg));
                 }
                 return true;
            }
        }
        return false;
    }

    @Override
    public boolean delete(String pathname) {

        for(FileInfo file: this.storage.getFiles()){
            if(file.getFilePathName().equals(pathname)){
                String header = this.protocol_version + " DELETE " + this.ID + " " + file.getID() + " \r\n\r\n";
                byte[] msg = header.getBytes(US_ASCII);

                //send message 3 times to prevent loss of some messages
                this.scheduler.execute(new MessageSender(this, "MC", msg));
                this.scheduler.schedule(new MessageSender(this, "MC", msg),2, TimeUnit.SECONDS);
                this.scheduler.schedule(new MessageSender(this, "MC", msg),4, TimeUnit.SECONDS);

                return true;
            }
        }
        return false;
    }

    @Override
    public boolean reclaim(int space) {
        return this.storage.reclaim(space * 1000, this);
    }

    @Override
    public boolean state() {
        System.out.println("================");
        System.out.println("== PEER STATE ==");
        System.out.println("================");
        System.out.println();
        System.out.println("Peer ID: " + this.ID);
        System.out.println();
        System.out.println("Files backup in other peers:");
        for(FileInfo file : storage.getFiles()){
            System.out.println();
            System.out.println("File path name: " + file.getFilePathName());
            System.out.println("File ID: " + file.getID());
            System.out.println("File replication degree: " + file.getRepDegree());
            System.out.println("File number of chunks: " + file.getChunks().size());
        }
        System.out.println();
        System.out.println("Chunks stored in this peer:");
        for(Chunk chunk : storage.getAllChunks()){
            System.out.println();
            System.out.println("Chunk file ID: " + chunk.getFileId());
            System.out.println("Chunk number: " + chunk.getNumber());
            System.out.println("Chunk size: " + chunk.getSize() + " bytes");
            System.out.println("Chunk perceived replication degree: " + chunk.getCurrRepDeg());
        }
        System.out.println();
        System.out.println("Storage info:");
        System.out.println("Storage capacity: " + storage.getCapacity() + " bytes");
        System.out.println("Storage used space: " + storage.getUsedSpace() + " bytes");
        System.out.println("Storage free space: " + (storage.getCapacity() - storage.getUsedSpace()) + " bytes");
        return false;
    }

    public Channel getChannel(String channel) {
        return channels.get(channel);
    }

    public int getID() {
        return this.ID;
    }

    public Storage getStorage() {
        return this.storage;
    }

    public int getNumberChunkMessages(String fileId, int chunkNo) {
        String key = fileId + "_" + chunkNo;
        return restore_numberChunkMessages.getOrDefault(key, 0);
    }

    public void incrementNumberChunkMessages(String fileId, int chunkNo) {
        String key = fileId + "_" + chunkNo;
        if(restore_numberChunkMessages.containsKey(key))
            restore_numberChunkMessages.replace(key, restore_numberChunkMessages.get(key)+1);
        else restore_numberChunkMessages.put(key, 1);
    }

    public int getNumberChunksToRestore() {
        return numberChunksToRestore;
    }

    public String getVersion() {
        return protocol_version;
    }
}