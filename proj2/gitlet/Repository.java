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
    File cwd;
    /** The .gitlet directory. */
    File gitletDir;
    /** The stage directory. */
    File stageDir;
    /** The blob directory. */
    File blobDir;

    File stageFile;
    File commitTreeFile;
    File referencesFile;
    File remotesFile;

    private BlobStore stageStore;
    private BlobStore blobStore;

    private Stage stage;
    private CommitTree commitTree;
    private References references;
    private Remotes remotes;
    private Logger logger;
    private Context context;

    void initialize(File dir) {
        cwd = dir;
        gitletDir = join(cwd, ".gitlet");
        stageDir = join(gitletDir, "stage");
        blobDir = join(gitletDir, "blob");
        stageFile = join(gitletDir, "stage-meta");
        commitTreeFile = join(gitletDir, "commit-tree");
        referencesFile = join(gitletDir, "references");
        remotesFile = join(gitletDir, "remotes");
        stageStore = new BlobStore(stageDir);
        blobStore = new BlobStore(blobDir);
        readState();
        context = new Context(cwd, stage, commitTree, references, blobStore, stageStore);
        logger = new Logger(context);
    }

    Repository() {
        initialize(new File(System.getProperty("user.dir")));
    }

    Repository(File gitletDir) {
        File dir = gitletDir.getParentFile() != null ? gitletDir.getParentFile() : new File(".");
        initialize(dir);
    }

    public void init() {
        if (gitletDir.exists()) {
            Utils.message("A Gitlet version-control system already exists "
                    + "in the current directory.");
            System.exit(0);
        }
        stage = new Stage();

        Map<String, String> tracked = new TreeMap<String, String>();
        Commit initialCommit = new Commit("initial commit", "", "", tracked);
        initialCommit.initialDateTime();
        initialCommit.updateID();
        commitTree = new CommitTree();
        commitTree.addCommit(initialCommit.getID(), initialCommit);
        
        Map<String, Commit> commits = commitTree.getCommitTree();
        String id = "";
        for (Map.Entry<String, Commit> entry : commits.entrySet()) {
            if (entry.getValue().isInitialCommit()) {
                id = entry.getValue().getID();
            }
        }
        references = new References(id);
        context = new Context(cwd, stage, commitTree, references, blobStore, stageStore);
        logger = new Logger(context);
        remotes = new Remotes();

        gitletDir.mkdir();
        stageDir.mkdir();
        blobDir.mkdir();

        Utils.writeObject(stageFile, stage);
        Utils.writeObject(commitTreeFile, commitTree);
        Utils.writeObject(referencesFile, references);
        Utils.writeObject(remotesFile, remotes);
    }

    public void check() {
        if (gitletDir.exists()) {
            return;
        }
        Utils.message("Not in an initialized Gitlet directory.");
        System.exit(0);
    }

    public void log() {
        logger.log();
    }

    public void globalLog() {
        logger.globalLog();
    }

    public void save() {
        Utils.writeObject(stageFile, stage);
        Utils.writeObject(commitTreeFile, commitTree);
        Utils.writeObject(referencesFile, references);
        Utils.writeObject(remotesFile, remotes);
    }

    public void add(String filename) {
        AddCommand.add(filename, context); 
    }

    public void rm(String filename) {
        RmCommand.rm(filename, context);
    }

    public void commit(String message, String secondParent) {
        CommitCommand.commit(message, secondParent, context);
    }

    public void find(String message) {
        List<String> result = commitTree.findCommit(message);
        if (result.isEmpty()) {
            Utils.message("Found no commit with that message.");
        } else {
            for (String id : result) {
                Utils.message(id);
            }
        }
    }

    public void status() {
        logger.status();
    }

    public void checkout(String filename) {
        checkout(references.getHEAD(), filename);
    }

    public void checkout(String commitId, String filename) {
        CheckoutCommand.checkout(commitId, filename, context);
    }

    public void checkoutBranch(String branch) {
        CheckoutCommand.checkoutBranch(branch, context);
    }

    public void branch(String branchName) {
        BranchCommand.branch(branchName, context);
    }

    public void rmBranch(String branchName) {
        BranchCommand.rmBranch(branchName, context);
    }

    public void reset(String commitId) {
        String idToReset = StuUtils.findCommit(commitId, context);
        checkoutCommit(idToReset);
        references.updateHEAD(idToReset);
        references.updatePointer(references.getBranch(), idToReset);
    }

    public void merge(String branch) {
        MergeCommand.merge(branch, context);
    }

    public void addRemote(String remote, String path) {
        RemoteCommand.addRemote(remote, path, remotes);
    }

    public void rmRemote(String remote) {
        RemoteCommand.rmRemote(remote, remotes);
    }

    public String checkRemote(String remote) {
        return RemoteCommand.checkRemote(remote, remotes);
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
        if (stageFile.exists()) {
            stage = Utils.readObject(stageFile, Stage.class);
        }
        if (commitTreeFile.exists()) {
            commitTree = Utils.readObject(commitTreeFile, CommitTree.class);
        }
        if (referencesFile.exists()) {
            references = Utils.readObject(referencesFile, References.class);
        }
        if (remotesFile.exists()) {
            remotes = Utils.readObject(remotesFile, Remotes.class);
        } else {
            remotes = new Remotes();
        }
    }

    private void checkoutCommit(String commitId) {
        CheckoutCommand.checkoutCommit(commitId, context);
    }

    public Context getContext() {
        return context;
    }
}
