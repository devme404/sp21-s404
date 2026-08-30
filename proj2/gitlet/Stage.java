package gitlet;

import java.util.*;
import java.io.Serializable;

public class Stage implements Serializable {
    private Map<String, String> additionStage = new HashMap<>();

    private Map<String, String> deletionStage = new HashMap<>();

    public void stageAddition(String filename, String hash) {
        additionStage.put(filename, hash);
    }

    public void unstageAddition(String filename) {
        additionStage.remove(filename);
    }

    public String getAddition(String filename) {
        return additionStage.get(filename);
    }

    public boolean isStagedAddition(String filename) {
        return additionStage.containsKey(filename);
    }

    public boolean hasHash(String hash) {
        return additionStage.values().contains(hash);
    }

    public Map<String, String> getAdditions() {
        return Collections.unmodifiableMap(additionStage);
    }

    public boolean isAdditionEmpty() {
        return additionStage.isEmpty();
    }

    public void clearAddition() {
        additionStage.clear();
    }

    public void stageRemoval(String filename, String hash) {
        deletionStage.put(filename, hash);
    }

    public void unstageRemoval(String filename) {
        deletionStage.remove(filename);
    }

    public String getRemoval(String filename) {
        return deletionStage.get(filename);
    }

    public boolean isStagedRemoval(String filename) {
        return deletionStage.containsKey(filename);
    }

    public boolean isRemovalEmpty() {
        return deletionStage.isEmpty();
    }

    public Map<String, String> getRemovals() {
        return Collections.unmodifiableMap(deletionStage);
    }

    public void clearRemoval() {
        deletionStage.clear();
    }
}
