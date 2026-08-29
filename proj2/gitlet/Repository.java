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


/** Represents a gitlet repository. */
class Repository {
    /** The current working directory. */
    public File CWD;
    /** The .gitlet directory. */
    public File GITLET_DIR;
    /** The stage directory. */
    public File STAGE_DIR;
    /** The blob directory. */
    public File BLOB_DIR;

    public File STAGE;
    public File COMMIT_TREE;
    public File REFERENCES;
    public File REMOTES;

    private BlobStore stage_store;
    private BlobStore blob_store;

    private Stage stage_;
    private CommitTree commit_tree_;
    private References references_;
    private Remotes remotes_;
    private Logger logger_;
    private Context context;

    void initialize(File dir) {
        CWD = dir;
        GITLET_DIR = join(CWD, ".gitlet");
        STAGE_DIR = join(GITLET_DIR, "stage");
        BLOB_DIR = join(GITLET_DIR, "blob");
        STAGE = join(GITLET_DIR, "stage-meta");
        COMMIT_TREE = join(GITLET_DIR, "commit-tree");
        REFERENCES = join(GITLET_DIR, "references");
        REMOTES = join(GITLET_DIR, "remotes");
        stage_store = new BlobStore(STAGE_DIR);
        blob_store = new BlobStore(BLOB_DIR);
        readState();
        context = new Context(CWD, stage_, commit_tree_, references_, blob_store, stage_store);
        logger_ = new Logger(context);
    }

    public Repository() {
        initialize(new File(System.getProperty("user.dir")));
    }

    public Repository(File gitletDir) {
        File dir = gitletDir.getParentFile() != null ? gitletDir.getParentFile() : new File(".");
        initialize(dir);
    }

    public void init() {
        if (GITLET_DIR.exists()) {
            Utils.message("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        stage_ = new Stage();

        Map<String, String> tracked = new TreeMap<String, String>();
        Commit initial_commit = new Commit("initial commit", "", "", tracked);
        initial_commit.initialDateTime();
        initial_commit.updateID();
        commit_tree_ = new CommitTree();
        commit_tree_.addCommit(initial_commit.getID(), initial_commit);
        
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
        remotes_ = new Remotes();

        GITLET_DIR.mkdir();
        STAGE_DIR.mkdir();
        BLOB_DIR.mkdir();

        Utils.writeObject(STAGE, stage_);
        Utils.writeObject(COMMIT_TREE, commit_tree_);
        Utils.writeObject(REFERENCES, references_);
        Utils.writeObject(REMOTES, remotes_);
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
        logger_.globalLog();
    }

    public void save() {
        Utils.writeObject(STAGE, stage_);
        Utils.writeObject(COMMIT_TREE, commit_tree_);
        Utils.writeObject(REFERENCES, references_);
        Utils.writeObject(REMOTES, remotes_);
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
        String id_to_reset = StuUtils.findCommit(commit_id, context);
        checkoutCommit(id_to_reset);
        references_.updateHEAD(id_to_reset);
        references_.updatePointer(references_.getBranch(), id_to_reset);
    }

    public void merge(String branch) {
        MergeCommand.merge(branch, context);
    }

    public void addRemote(String remote, String path) {
        RemoteCommand.addRemote(remote, path, remotes_);
    }

    public void rmRemote(String remote) {
        RemoteCommand.rmRemote(remote, remotes_);
    }

    public String checkRemote(String remote) {
        return RemoteCommand.checkRemote(remote, remotes_);
    }

    public void checkRemote(Repository remote) {
        RemoteCommand.checkRemote(remote);
    }

    public void push(Repository remoteRepo, String branch) {
        RemoteCommand.push(context, remoteRepo.getContext(), branch);
    }

    public void fetch(Repository remoteRepo, String remoteBranch, String localBranch) {
        RemoteCommand.fetch(context, remoteRepo.getContext(), remoteBranch, localBranch);
    }

    public void pull(Repository remoteRepo, String remoteBranch, String localBranch) {
        fetch(remoteRepo, remoteBranch, localBranch);
        merge(localBranch);
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
        if (REMOTES.exists()) {
            remotes_ = Utils.readObject(REMOTES, Remotes.class);
        } else {
            remotes_ = new Remotes();
        }
    }

    private void checkoutCommit(String commit_id) {
        CheckoutCommand.checkoutCommit(commit_id, context);
    }

    public Context getContext() {
        return context;
    }
}
