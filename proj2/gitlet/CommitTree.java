package gitlet;

import java.io.Serializable;
import java.util.*;
public class CommitTree implements Serializable {
    private Map<String, Commit> commitTree = new HashMap<>();
    public Commit getCommit(String id) {
        return commitTree.get(id);
    }

    public boolean hasCommit(String id) {
        return commitTree.containsKey(id);
    }

    public void addCommit(String id, Commit commit) {
        commitTree.put(id, commit);
    }

    public List<String> findCommit(String text) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, Commit> entry : commitTree.entrySet()) {
            Commit commit = entry.getValue();
            if (commit.getMessage().equals(text)) {
                result.add(commit.getID());
            }
        }
        return result;
    }

    public String matchCommit(String text) {
        String id = text;
        for (Map.Entry<String, Commit> entry : commitTree.entrySet()) {
            if (entry.getKey().startsWith(text)) {
                id = entry.getValue().getID();
                return id;
            }

            if (entry.getKey().equals(text)) {
                return id;
            }
        }

        return "";
    }

    public Map<String, Commit> getCommitTree() {
        return Collections.unmodifiableMap(commitTree);
    }
}
