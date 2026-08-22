package gitlet;

import java.io.File;
import java.util.*;
import static gitlet.Utils.*;
import static gitlet.BlobStore.*;
import static gitlet.Stage.*;
import static gitlet.CommitTree.*;
import static gitlet.Logger.*;
import static gitlet.StuUtils.*;
import static gitlet.AddCommand.*;
import static gitlet.RmCommand.*;
import static gitlet.CommitCommand.*;
import static gitlet.CheckoutCommand.*;
import static gitlet.BranchCommand.*;


/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class does at a high level.
 *
 *  @author D
 */
class Repository {
    /**
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    /** The stage directory. */
    public static final File STAGE_DIR = join(GITLET_DIR, "stage");
    /** The blob directory. */
    public static final File BLOB_DIR = join(GITLET_DIR, "blob");

    public static final File STAGE = join(GITLET_DIR, "stage-meta");

    public static final File COMMIT_TREE = join(GITLET_DIR, "commit-tree");

    public static final File REFERENCES = join(GITLET_DIR, "references");

    private final BlobStore stage_store = new BlobStore(STAGE_DIR);

    private final BlobStore blob_store = new BlobStore(BLOB_DIR);

    private Stage stage_;

    private CommitTree commit_tree_;

    private References references_;
    
    private Logger logger_;

    private Context context;


    public Repository() {
        readState();
        context = new Context(CWD, stage_, commit_tree_, references_, blob_store, stage_store);
        logger_ = new Logger(context);
    }

    public void init() {
        if (GITLET_DIR.exists()) {
            Utils.message("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        stage_ = new Stage();

        // Initialize Commit Tree and Create the default commit
        Map<String, String> tracked = new TreeMap<String, String>();
        Commit initial_commit = new Commit("initial commit", "", "", tracked);
        initial_commit.initialDateTime();
        initial_commit.updateID();
        commit_tree_ = new CommitTree();
        commit_tree_.addCommit(initial_commit.getID(), initial_commit);
        
        // Initializa References with the ID of the initial commit.
        Map<String, Commit> commits = commit_tree_.getCommitTree();
        String id = "";
        for (Map.Entry<String, Commit> entry : commits.entrySet()) {
            if (entry.getValue().isInitialCommit()) {
                id = entry.getValue().getID();
            }
        }
        references_ = new References(id);
        context = new Context(CWD, stage_, commit_tree_, references_, blob_store, stage_store);
        logger_ = new Logger(context);

        GITLET_DIR.mkdir();
        STAGE_DIR.mkdir();
        BLOB_DIR.mkdir();

        Utils.writeObject(STAGE, stage_);
        Utils.writeObject(COMMIT_TREE, commit_tree_);
        Utils.writeObject(REFERENCES, references_);
    }

    public void check() {
        if (GITLET_DIR.exists()) {
            return;
        }

        Utils.message("Not in an initialized Gitlet directory.");
        System.exit(0);
    }

    public void log() {
        logger_.log();
    }

    public void globalLog() {
        logger_.log();
    }

    public void save() {
        Utils.writeObject(STAGE, stage_);
        Utils.writeObject(COMMIT_TREE, commit_tree_);
        Utils.writeObject(REFERENCES, references_);
    }

    public void add(String filename) {
        AddCommand.add(filename, context); 
    }

    public void rm(String filename) {
        RmCommand.rm(filename, context);
    }

    public void commit(String message, String second_parent) {
        CommitCommand.commit(message, second_parent, context);
    }

    public void find(String message) {
        String result = commit_tree_.findCommit(message);
        if (result.equals("")) {
            Utils.message("Found no commit with that message.");
        } else {
            Utils.message(result);
        }
    }

    public void status() {
        logger_.status();
    }

    public void checkout(String filename) {
        checkout(references_.getHEAD(), filename);
    }

    public void checkout(String commit_id, String filename) {
        CheckoutCommand.checkout(commit_id, filename, context);
    }

    public void checkoutBranch(String branch) {
        CheckoutCommand.checkoutBranch(branch, context);
    }

    public void branch(String branch_name) {
        BranchCommand.branch(branch_name, context);
    }

    public void rmBranch(String branch_name) {
        BranchCommand.rmBranch(branch_name, context);
    }

    public void reset(String commit_id) {
        String id_to_reset = findCommit(commit_id, context);
        checkoutCommit(id_to_reset);
        references_.updateHEAD(id_to_reset);
        references_.updatePointer(references_.getBranch(), id_to_reset);
    }

    public void merge(String branch) {
        MergeCommand.merge(branch, context);
    }

    private void readState() {
        if (STAGE.exists()) {
            stage_ = Utils.readObject(STAGE, Stage.class);
        }
        if (COMMIT_TREE.exists()) {
            commit_tree_ = Utils.readObject(COMMIT_TREE, CommitTree.class);
        }
        if (REFERENCES.exists()) {
            references_ = Utils.readObject(REFERENCES, References.class);
        }
    }

    private void checkoutCommit(String commit_id) {
        CheckoutCommand.checkoutCommit(commit_id, context);
    }
}
