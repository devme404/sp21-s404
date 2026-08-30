package gitlet;

import java.io.File;
import static gitlet.Utils.*;
public class BlobStore {
    private File workDir;

    public BlobStore(File workDir) {
        this.workDir = workDir;
    }

    public void store(String hash, byte[] content) {
        File file = Utils.join(workDir, hash);
        Utils.writeContents(file, content);
    }

    public byte[] load(String hash) {
        File file = Utils.join(workDir, hash);
        if (file.exists()) {
            return Utils.readContents(file);
        }
        return null;
    }

    public String loadString(String hash) {
        byte[] data = load(hash);
        if (data == null) {
            return "";
        }
        return new String(data, java.nio.charset.StandardCharsets.UTF_8);
    }

    public boolean contains(String hash) {
        File file = Utils.join(workDir, hash);
        return file.exists();
    }

    public boolean delete(String hash) {
        File file = Utils.join(workDir, hash);
        if (file.exists()) {
            file.delete();
            return true;
        }
        return false;
    }
}
