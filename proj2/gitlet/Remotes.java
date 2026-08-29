package gitlet;
import java.util.*;
import java.io.File;
import gitlet.Utils.*;
import java.io.Serializable;

class Remotes implements Serializable {
    Map<String, String> remotes = new TreeMap<>();

    void add(String remote, String path) {
        if (remotes.containsKey(remote)) {
            throw Utils.error("A remote with that name already exists."); 
        }
        path = path.replace("/", File.separator);
        remotes.put(remote, path);
    }

    String get(String remote) {
        if (!remotes.containsKey(remote)) {
            throw Utils.error("A remote with that name does not exist.");
        }
        return remotes.get(remote);
    }

    void remove(String remote) {
        if (!remotes.containsKey(remote)) {
            throw Utils.error("A remote with that name does not exist.");
        }
        remotes.remove(remote);
    }
}