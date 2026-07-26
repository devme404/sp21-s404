package gitlet;

import java.io.Serializable;
import java.util.*;
import gitlet.Commit.*;

public class CommitTree implements Serializable {
    public HashMap<String, Commit> commit_tree_ = new HashMap<String, Commit>();

    public CommitTree() {
        Map<String, String> tracked = new TreeMap<String, String>();
        Commit initial_commit = new Commit("initial commit", "", "", tracked);
        initial_commit.initialDateTime();
        initial_commit.updateID();
        commit_tree_.put(initial_commit.getID(), initial_commit);
    }

    public Commit getCommit(String id) {
        return commit_tree_.get(id);
    }

    public void addCommit(String id, Commit commit) {
        commit_tree_.put(id, commit);
    }

    public String findCommit(String text) {
        for (Map.Entry<String, Commit> entry : commit_tree_.entrySet()) {
            Commit commit = entry.getValue();
            if (commit.getMessage().equals(text)) {
                return commit.getID();
            }
        }
        return "";
    }

    public String matchCommit(String text) {
        String id = text;
        for (Map.Entry<String, Commit> entry : commit_tree_.entrySet()) {
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
        return Collections.unmodifiableMap(commit_tree_);
    }
}
