package gitlet;

import java.util.*;
import java.io.Serializable;

public class Stage implements Serializable {
    private Map<String, String> addition_stage = new HashMap<>();

    private Map<String, String> deletion_stage = new HashMap<>();

    public void stageAddition(String filename, String hash) {
        addition_stage.put(filename, hash);
    }

    public void unstageAddition(String filename) {
        addition_stage.remove(filename);
    }

    public String getAddition(String filename) {
        return addition_stage.get(filename);
    }

    public boolean isStagedAddition(String filename) {
        return addition_stage.containsKey(filename);
    }

    public Map<String, String> getAdditions() {
        return Collections.unmodifiableMap(addition_stage);
    }

    public boolean isAdditionEmpty() {
        return addition_stage.isEmpty();
    }

    public void clearAddition() {
        addition_stage.clear();
    }

    public void stageRemoval(String filename, String hash) {
        deletion_stage.put(filename, hash);
    }

    public void unstageRemoval(String filename) {
        deletion_stage.remove(filename);
    }

    public String getRemoval(String filename) {
        return deletion_stage.get(filename);
    }

    public boolean isStagedRemoval(String filename) {
        return deletion_stage.containsKey(filename);
    }

    public boolean isRemovalEmpty() {
        return deletion_stage.isEmpty();
    }

    public Map<String, String> getRemovals() {
        return Collections.unmodifiableMap(deletion_stage);
    }

    public void clearRemoval() {
        deletion_stage.clear();
    }
}
