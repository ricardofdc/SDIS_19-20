import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.nio.file.attribute.BasicFileAttributes;

public class FileInfo implements Serializable {
    private String pathname;
    private File file;
    private String fileId;
    private int rep_degree;
    private ArrayList<Chunk> chunks;

    public FileInfo(String pathname, int rep_degree){
        this.pathname = pathname;
        this.file = new File(pathname);
        this.rep_degree = rep_degree;
        this.chunks = new ArrayList<>();

        encriptId();
        createChunks();
    }

    private void createChunks() {
        byte[] buf = new byte[Chunk.MAX_CHUNK_SIZE];
        int nr=0;

        try {
            RandomAccessFile f = new RandomAccessFile(this.file, "r");
            int bytesRead;

            while((bytesRead = f.read(buf)) > 0){
                byte[] chunkBody = Arrays.copyOf(buf, bytesRead);
                Chunk chunk = new Chunk(nr, this.fileId, chunkBody, bytesRead, this.rep_degree);
                this.chunks.add(chunk);
                nr++;
                //char_buf = new char[Chunk.MAX_CHUNK_SIZE/2];
                buf = new byte[Chunk.MAX_CHUNK_SIZE];
            }

            if(this.file.length() % Chunk.MAX_CHUNK_SIZE == 0){
                Chunk chunk = new Chunk(nr, this.fileId, buf, 0, this.rep_degree);
                this.chunks.add(chunk);
            }




        } catch (IOException e) {
            System.err.println("File error: " + e.toString());
            e.printStackTrace();
        }
    }

    private void encriptId() {

        try {
            String filename = this.file.getName();
            Path filepath = Paths.get(filename);
            String owner = Files.getOwner(filepath).getName();
            String modifiedDate = String.valueOf(this.file.lastModified());

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fileId_byte = digest.digest((filename + '-' + modifiedDate + '-' + owner).getBytes(StandardCharsets.UTF_8));
            this.fileId = Base64.getEncoder().encodeToString(fileId_byte);

            //System.out.println("fileId = " + fileId);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Chunk> getChunks(){
        return chunks;
    }

    public String getID() {
        return fileId;
    }


    public boolean equalTo(FileInfo file) {
        return this.fileId.equals(file.getFileId());
    }

    private String getFileId() {
        return fileId;
    }

    public String getFilePathName() {
        return pathname;
    }

    public int getRepDegree(){
        return rep_degree;
    }
}
