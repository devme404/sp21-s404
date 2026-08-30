package gitlet;
import java.io.Serializable;
import java.util.*;

public class References implements Serializable {

    private final String HEAD = "HEAD";
    private final String DEFAULT_BRANCH = "master";
    private Map<String, String> pointers = new HashMap<>();
    private String branch = DEFAULT_BRANCH;

    public References(String id) {
        updateHEAD(id);
        updatePointer(DEFAULT_BRANCH, id);
    }

    public String getHEAD() {
        return pointers.get(HEAD);
    }

    public void updateHEAD(String id) {
        pointers.put(HEAD, id);
    }

    public void updatePointer(String pointer, String id) {
        pointers.put(pointer, id);
    }

    public String getBranch() {
        return branch;
    }

    public List<String> getBranches() {
        List<String> branches = new ArrayList<>();
        for (Map.Entry<String, String> entry : pointers.entrySet()) {
            String b = entry.getKey();
            if (b.equals(HEAD)) {
                continue;
            }
            branches.add(b);
        }
        Collections.sort(branches);
        return branches;
    }
    
    public void updateBranch(String newBranch) {
        this.branch = newBranch;
    }

    public boolean hasPointer(String pointer) {
        return pointers.containsKey(pointer);
    }

    public String getPointer(String pointer) {
        if (hasPointer(pointer)) {
            return pointers.get(pointer);
        }
        return "";
    }

    public void removePointer(String pointer) {
        if (!hasPointer(pointer)) {
            return;
        }
        pointers.remove(pointer);
    }
}
