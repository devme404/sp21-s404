package gitlet;

import java.util.*;
import java.io.Serializable;

public class References implements Serializable {

    private final String HEAD = "HEAD";
    private final String DEFAULT_BRANCH = "master";
    public HashMap<String, String> pointers_ = new HashMap<>();
    public String branch_ = DEFAULT_BRANCH;

    public References(CommitTree commit_tree) {
        Map<String, Commit> commits = commit_tree.getCommitTree();
        for (Map.Entry<String, Commit> entry : commits.entrySet()) {
            if (entry.getValue().isInitialCommit()) {
                String id = entry.getValue().getID();
                pointers_.put(HEAD, id);
                pointers_.put(DEFAULT_BRANCH, id);
            }
        }
    }

    public String getHEAD() {
        return pointers_.get(HEAD);
    }

    public void updateHEAD(String id) {
        pointers_.put(HEAD, id);
    }

    public void updatePointer(String pointer, String id) {
        pointers_.put(pointer, id);
    }

    public String getBranch() {
        return branch_;
    }

    public List<String> getBranches() {
        List<String> branches = new ArrayList<>();
        for (Map.Entry<String, String> entry : pointers_.entrySet()) {
            String branch = entry.getKey();
            if (branch.equals(HEAD)) {
                continue;
            }
            branches.add(branch);
        }
        Collections.sort(branches);
        return branches;
    }
    
    public void updateBranch(String branch) {
        branch_ = branch;
    }

    public boolean hasPointer(String pointer) {
        return pointers_.containsKey(pointer);
    }

    public String getPointer(String pointer) {
        if (hasPointer(pointer)) {
            return pointers_.get(pointer);
        }
        return "";
    }

    public void removePointer(String pointer) {
        if (!hasPointer(pointer)) {
            return;
        }
        pointers_.remove(pointer);
    }
}
