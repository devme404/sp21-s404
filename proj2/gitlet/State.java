package gitlet;

import java.util.*;
import java.io.Serializable;

/** Represents the state of gitlet program.
 *
 *
 * @author D
 */
public class State implements Serializable {
    /** Node tree of commits. */
    public HashMap<String, Commit> commit_tree = new HashMap<>();
    /** Addition stage. */
    public HashMap<String, String> addition_stage = new HashMap<>();
    /** Deletion stage. */
    public HashMap<String, String> deletion_stage = new HashMap<>();

    /** HEAD and branch pointers. */
    public HashMap<String, String> pointers_ = new HashMap<>();
    /** Branch state. */
    public String branch_ = "";

    public void init() {
        Commit initial_commit = new Commit("initial commit", "", "");
        initial_commit.initialDateTime();
        initial_commit.updateID();
        commit_tree.put(initial_commit.id_, initial_commit);

        pointers_.put("HEAD", initial_commit.id_); 
        pointers_.put("master", initial_commit.id_);
        branch_ = "master";
    }
}
