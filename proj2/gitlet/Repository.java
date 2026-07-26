package gitlet;

import java.io.File;
import java.util.*;
import static gitlet.Utils.*;
import static gitlet.BlobStore.*;
import static gitlet.Stage.*;
import static gitlet.CommitTree.*;


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

    private static final BlobStore stage_store = new BlobStore(STAGE_DIR);

    private static final BlobStore blob_store = new BlobStore(BLOB_DIR);

    private static Stage stage_;

    private static CommitTree commit_tree_;

    private static References references_;


    public Repository() {
        readState();
    }

    public void init() {
        if (GITLET_DIR.exists()) {
            Utils.message("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        stage_ = new Stage();
        commit_tree_ = new CommitTree();
        references_ = new References(commit_tree_);

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
        Commit commit_ptr = commit_tree_.getCommit(references_.getHEAD());

        while (!commit_ptr.isInitialCommit()) {
            String commit_info;
            if (!commit_ptr.isMergeCommit()) {
                commit_info = commit_ptr.toString();
            } else {
                commit_info = commit_ptr.toString();
            }
            Utils.message(commit_info);
            commit_ptr = commit_tree_.getCommit(commit_ptr.getParentID());
        }

        String commit_info = commit_ptr.toString();
        Utils.message(commit_info);
    }

    public void globalLog() {
        Map<String, Commit> commit_tree = commit_tree_.getCommitTree();
        for (Map.Entry<String, Commit> entry : commit_tree.entrySet()) {
            Commit commit = entry.getValue();
            String commit_info;
            if (!commit.isMergeCommit()) {
                commit_info = commit.toString();
            } else {
                commit_info = commit.toString();
            }
            Utils.message(commit_info);
        }
    }

    public void save() {
        Utils.writeObject(STAGE, stage_);
        Utils.writeObject(COMMIT_TREE, commit_tree_);
        Utils.writeObject(REFERENCES, references_);
    }

    public void add(String filename) {
        if (stage_.isStagedRemoval(filename)) {
            stage_.unstageRemoval(filename);
            return;
        }

        File file_to_add = Utils.join(CWD, filename);
        if (!file_to_add.exists()) {
            Utils.message("File does not exist.");
            return;
        }

        byte[] content = Utils.readContents(file_to_add);
        String hash = Utils.sha1(filename, content);

        String tracked_hash, old_hash;

        if (commit_tree_.getCommit(references_.getHEAD()).getTracked().containsKey(filename)) {
            tracked_hash = commit_tree_.getCommit(references_.getHEAD()).getTracked().get(filename);
            if (tracked_hash.equals(hash)) {
                if (stage_.isStagedAddition(filename)) {
                    old_hash = stage_.getAddition(filename);
                    stage_store.delete(old_hash);
                    stage_.unstageAddition(filename);
                }
                return;
            }
        }

        if (!stage_.isStagedAddition(filename)) {
            stage_store.store(hash, content);
            stage_.stageAddition(filename, hash);
            return;
        }


        old_hash = stage_.getAddition(filename);
        if (old_hash.equals(hash)) {
            return;
        }

        stage_store.store(hash, content);
        stage_store.delete(old_hash);
        stage_.stageAddition(filename, hash);
    }

    public void rm(String filename) {
        boolean is_staged = false;
        if (stage_.isStagedAddition(filename)) {
            String hash = stage_.getAddition(filename);
            stage_.unstageAddition(filename);
            stage_store.delete(hash);
            is_staged = true;
        }

        Commit head_commit = commit_tree_.getCommit(references_.getHEAD());
        if (head_commit.getTracked().containsKey(filename)) {
            stage_.stageRemoval(filename, head_commit.getTracked().get(filename));
            
            File file_to_rm = Utils.join(CWD, filename);
            if (file_to_rm.exists()) {
                restrictedDelete(file_to_rm);
            }
            return;
        }
        
        if (!is_staged) {
            Utils.message("No reason to remove the file.");
        }
    }

    public void commit(String message, String second_parent_commit) {
        if (message.length() == 0) {
            Utils.message("Please enter a commit message.");
            System.exit(0);
        }
        if (stage_.isAdditionEmpty() && stage_.isRemovalEmpty()) {
            Utils.message("No changes added to the commit.");
            return;
        }

        Commit last_commit = commit_tree_.getCommit(references_.getHEAD());
        Map<String, String> tracked = new TreeMap<String, String>();
        tracked.putAll(last_commit.getTracked());

        Map<String, String> addition_stage = stage_.getAdditions();
        for (Map.Entry<String, String> entry : addition_stage.entrySet()) {
            String hash = entry.getValue();

            tracked.put(entry.getKey(), entry.getValue());

            blob_store.store(hash, stage_store.load(hash));
            stage_store.delete(hash);
        }
        stage_.clearAddition();

        Map<String, String> deletion_stage = stage_.getRemovals();
        for (Map.Entry<String, String> entry : deletion_stage.entrySet()) {
            tracked.remove(entry.getKey());
        }
        stage_.clearRemoval();

        Commit new_commit = new Commit(message, last_commit.getID(), second_parent_commit, tracked);
        new_commit.updateID();

        commit_tree_.addCommit(new_commit.getID(), new_commit);

        references_.updateHEAD(new_commit.getID());
        references_.updatePointer(references_.getBranch(), new_commit.getID());
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
        Utils.message("=== Branches ===");
        List<String> branches = references_.getBranches();
        for (String branch : branches) {
            if (branch.equals(references_.getBranch())) {
                Utils.message("*" + branch);
                continue;
            }
            Utils.message(branch);
        }
        System.out.println();

        Utils.message("=== Staged Files ===");
        List<String> staged_files = new ArrayList<>();
        Map<String, String> addition_stage = stage_.getAdditions();
        for (Map.Entry<String, String> entry : addition_stage.entrySet()) {
            staged_files.add(entry.getKey());
        }
        Collections.sort(staged_files);
        for (String file : staged_files) {
            Utils.message(file);
        }
        System.out.println();

        Utils.message("=== Removed Files ===");
        List<String> removed_files = new ArrayList<>();
        Map<String, String> deletion_stage = stage_.getRemovals();
        for (Map.Entry<String, String> entry : deletion_stage.entrySet()) {
            removed_files.add(entry.getKey());
        }
        Collections.sort(removed_files);
        for (String file : removed_files) {
            Utils.message(file);
        }
        System.out.println();

        Utils.message("=== Modifications Not Staged For Commit ===");
        Map<String, String> tracked_files = commit_tree_.getCommit(references_.getHEAD()).getTracked();
        List<String> modifications = new ArrayList<>();
        for (Map.Entry<String, String> entry : tracked_files.entrySet()) {
            String filename = entry.getKey();
            if (stage_.isStagedAddition(filename)) {
                continue;
            }
            
            File file = Utils.join(CWD, filename);
            if (file.exists()) {
                byte[] content_of_file_in_cwd = readContents(file);
                String hash_of_file_in_cwd = Utils.sha1(filename, content_of_file_in_cwd);
                if (!hash_of_file_in_cwd.equals(entry.getValue())) {
                    modifications.add(filename + " (modified)");
                }
            } else {
                if (!stage_.isStagedRemoval(filename)) {
                    modifications.add(filename + " (deleted)");
                }
            }
        }

        for (Map.Entry<String, String> entry : addition_stage.entrySet()) {
            String filename = entry.getKey();

            File file = Utils.join(CWD, filename);
            if (file.exists()) {
                byte[] content_of_file_in_cwd = Utils.readContents(file);
                String hash_of_file_in_cwd = Utils.sha1(filename, content_of_file_in_cwd);
                if (!hash_of_file_in_cwd.equals(entry.getValue())) {
                    modifications.add(filename + " (modified)");
                }
            } else {
                modifications.add(filename + " (deleted)");
            }
        }

        Collections.sort(modifications);
        for (String file : modifications) {
            Utils.message(file);
        }

        System.out.println();

        Utils.message("=== Untracked Files ===");
        List<String> files = scanUntrackedFiles();
        for (String file : files) {
            Utils.message(file);
        }
        System.out.println();
    }

    public void checkout(String filename) {
        checkout(references_.getHEAD(), filename);
    }

    public void checkout(String commit_id, String filename) {
        String id_to_checkout = findCommit(commit_id);
        Map<String, String> tracked_files = commit_tree_.getCommit(id_to_checkout).getTracked();

        if (!tracked_files.containsKey(filename)) {
            Utils.message("File does not exist in that commit.");
            System.exit(0);
        }

        File file_to_checkout_in_cwd = Utils.join(CWD, filename);
        Utils.writeContents(file_to_checkout_in_cwd, blob_store.load(tracked_files.get(filename)));
    }

    public void checkoutBranch(String branch) {
        if (!references_.hasPointer(branch)) {
            Utils.message("No such branch exists.");
            System.exit(0);
        }
        
        if (references_.getBranch().equals(branch)) {
            Utils.message("No need to checkout the current branch.");
            System.exit(0);
        }

        checkoutCommit(references_.getPointer(branch));

        references_.updateBranch(branch);
        references_.updateHEAD(references_.getPointer(branch));
    }

    public void branch(String branch_name) {
        if (references_.hasPointer(branch_name)) {
            Utils.message("A branch with that name already exists.");
            System.exit(0);
        }

        references_.updatePointer(branch_name, references_.getHEAD());
    }

    public void rmBranch(String branch_name) {
        if (!references_.hasPointer(branch_name)) {
            Utils.message("A branch with that name does not exist.");
            System.exit(0);
        }
        
        if (references_.getBranch().equals(branch_name)) {
            Utils.message("Cannot remove the current branch.");
            System.exit(0);
        }

        references_.removePointer(branch_name);
    }

    public void reset(String commit_id) {
        String id_to_reset = findCommit(commit_id);
        checkoutCommit(id_to_reset);
        references_.updateHEAD(id_to_reset);
        references_.updatePointer(references_.getBranch(), id_to_reset);
    }

    public void merge(String branch) {
        if (references_.getBranch().equals(branch)) {
            Utils.message("Cannot merge a branch with itself.");
            System.exit(0);
        }

        if (!references_.hasPointer(branch)) {
            Utils.message("A branch with that name does not exist.");
            System.exit(0);
        }


        if (!stage_.isAdditionEmpty() || !stage_.isRemovalEmpty()) {
            Utils.message("You have uncommitted changes.");
            System.exit(0);
        }


        Commit given_branch_head = commit_tree_.getCommit(references_.getPointer(branch));
        Commit current_branch_head = commit_tree_.getCommit(references_.getHEAD());

        Commit split_point = commit_tree_.getCommit(findSplitPoint(current_branch_head.getID(), given_branch_head.getID()));

        if (split_point.getID().equals(given_branch_head.getID())) {
            Utils.message("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        
        if (split_point.getID().equals(current_branch_head.getID())) {
            checkoutBranch(branch);
            Utils.message("Current branch fast-forwarded.");
            return;
        }

        List<String> untracked_files = scanUntrackedFiles();

        boolean isConflicted = false;

        for (Map.Entry<String, String> entry : split_point.getTracked().entrySet()) {
            String split_point_filename = entry.getKey();
            String split_point_hash = entry.getValue();

            if (!given_branch_head.getTracked().containsKey(split_point_filename) && !current_branch_head.getTracked().containsKey(split_point_filename)) {
                continue;
            }

            if (given_branch_head.getTracked().containsKey(split_point_filename) && given_branch_head.getTracked().get(split_point_filename).equals(split_point_hash)) {
                continue;
            }


            if (current_branch_head.getTracked().containsKey(split_point_filename) && current_branch_head.getTracked().get(split_point_filename).equals(split_point_hash)) {
                if (untracked_files.contains(split_point_filename)) {
                    Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }

                if (!given_branch_head.getTracked().containsKey(split_point_filename)) {
                    rm(split_point_filename);
                    continue;
                }

                checkout(given_branch_head.getID(), split_point_filename);
                add(split_point_filename);
                continue;
            }

            if (!current_branch_head.getTracked().containsKey(split_point_filename)) {
                if (untracked_files.contains(split_point_filename)) {
                    Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }

                isConflicted = true;
                File cwd_file = Utils.join(CWD, split_point_filename);
                Utils.writeContents(cwd_file, "<<<<<<< HEAD\n=======\n" + blob_store.loadString(given_branch_head.getTracked().get(split_point_filename)) + "\n>>>>>>>\n");
                add(split_point_filename);
            }

            if (!given_branch_head.getTracked().containsKey(split_point_filename)) {
                if (untracked_files.contains(split_point_filename)) {
                    Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }

                isConflicted = true;
                File cwd_file = Utils.join(CWD, split_point_filename);
                Utils.writeContents(cwd_file, "<<<<<<< HEAD\n" + blob_store.loadString(current_branch_head.getTracked().get(split_point_filename)) + "\n=======\n>>>>>>>\n");
                add(split_point_filename);
            }
            if (current_branch_head.getTracked().containsKey(split_point_filename) && given_branch_head.getTracked().containsKey(split_point_filename)) {
                if (current_branch_head.getTracked().get(split_point_filename).equals(given_branch_head.getTracked().get(split_point_filename))) {
                    continue;
                }

                if (untracked_files.contains(split_point_filename)) {
                    Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }


                isConflicted = true;
                File cwd_file = Utils.join(CWD, split_point_filename);
                Utils.writeContents(cwd_file, "<<<<<<< HEAD\n" + blob_store.loadString(current_branch_head.getTracked().get(split_point_filename)) + "=======\n" + blob_store.loadString(given_branch_head.getTracked().get(split_point_filename)) + ">>>>>>>\n");
                add(split_point_filename);
            }
        }

        for (Map.Entry<String, String> entry : given_branch_head.getTracked().entrySet()) {
            String filename = entry.getKey();
            if (!split_point.getTracked().containsKey(filename)) {
                if (current_branch_head.getTracked().containsKey(filename) && current_branch_head.getTracked().get(filename).equals(entry.getValue())) {
                    continue;
                }

                if (untracked_files.contains(filename)) {
                    Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }

                if (!current_branch_head.getTracked().containsKey(filename)) {
                    checkout(given_branch_head.getID(), filename);
                    add(filename);
                    continue;
                }
                
                File cwd_file = Utils.join(CWD, filename);
                Utils.writeContents(cwd_file, "<<<<<<< HEAD\n" + blob_store.loadString(current_branch_head.getTracked().get(filename)) + "\n=======\n" + blob_store.loadString(entry.getValue()) + "\n>>>>>>>");
                add(filename);
                isConflicted = true;
            }
        }
        if (isConflicted) {
            Utils.message("Encountered a merge conflict.");
        }


        commit("Merged " + branch + " into " + references_.getBranch() + ".", given_branch_head.getID());

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

    private List<String> scanUntrackedFiles() {
        List<String> files = new ArrayList<>();
        Map<String, String> deletion_stage = stage_.getRemovals();
        for (Map.Entry<String, String> entry : deletion_stage.entrySet()) {
            String filename = entry.getKey();
            File file = Utils.join(CWD, filename);
            if (file.exists()) {
                files.add(filename);
            }
        }
        
        List<String> files_in_cwd = Utils.plainFilenamesIn(CWD);
        for (String filename : files_in_cwd) {
            if (!commit_tree_.getCommit(references_.getHEAD()).getTracked().containsKey(filename) && !stage_.isStagedAddition(filename)) {
                files.add(filename);
            }
        }
        Collections.sort(files);

        return files;
    }

    private void checkoutCommit(String commit_id) {
        Map<String, String> target_files = commit_tree_.getCommit(commit_id).getTracked();

        List<String> untracked_files = scanUntrackedFiles();

        for (String file : untracked_files) {
            if (target_files.containsKey(file)) {
                Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }

        Map<String, String> addition_stage = stage_.getAdditions();
        for (Map.Entry<String, String> entry : addition_stage.entrySet()) {
            stage_store.delete(entry.getValue());
        }

        stage_.clearAddition();
        stage_.clearRemoval();

        for (Map.Entry<String, String> entry : target_files.entrySet()) {
            String filename = entry.getKey();
            File file_in_cwd = Utils.join(CWD, filename);
            Utils.writeContents(file_in_cwd, blob_store.load(entry.getValue()));
        }

        Map<String, String> current_tracked_files = commit_tree_.getCommit(references_.getHEAD()).getTracked();

        for (Map.Entry<String, String> entry : current_tracked_files.entrySet()) {
            String filename = entry.getKey();
            if (!target_files.containsKey(filename)) {
                File file = Utils.join(CWD, filename);
                if (file.exists()) {
                    restrictedDelete(file);
                }
            }
        }
    }

    private String findCommit(String text) {
        String result = commit_tree_.matchCommit(text);
        if (result.equals("")) {
            Utils.message("No commit with that id exists.");
            System.exit(0);
        }
        return result;
    }


    private HashMap<String, Integer> findParentsDistance(String commit) {
        Queue<String> queue = new LinkedList<>();
        HashMap<String, Integer> distances = new HashMap<>();

        queue.add(commit);
        distances.put(commit, 0);

        while (!queue.isEmpty()) {
            String id = queue.remove();
            Integer parent_distance = distances.get(id) + 1;
            Commit curr_commit = commit_tree_.getCommit(id);
            String first_parent = curr_commit.getParentID();
            String second_parent = curr_commit.getSecondParentID();

            if (!first_parent.isEmpty() && !distances.containsKey(first_parent)) {
                queue.add(first_parent);
                distances.put(first_parent, parent_distance);
            }
            if (!second_parent.isEmpty() && !distances.containsKey(second_parent)) {
                queue.add(second_parent);
                distances.put(second_parent, parent_distance);
            }
        }

        return distances;
    }

    private String findSplitPoint(String first_commit, String second_commit) {
        HashMap<String, Integer> first_parent_distances = findParentsDistance(first_commit);
        HashMap<String, Integer> second_parent_distances = findParentsDistance(second_commit);

        String best_parent = null;
        Integer best_distance = Integer.MAX_VALUE;

        for (Map.Entry<String, Integer> entry : first_parent_distances.entrySet()) {
            String curr_parent = entry.getKey();
            if (second_parent_distances.containsKey(curr_parent)) {
                Integer sum_distance = entry.getValue() + second_parent_distances.get(curr_parent);
                if (sum_distance < best_distance) {
                    best_parent = curr_parent;
                    best_distance = sum_distance;
                }
            }
        }

        if (best_parent != null) {
            return best_parent;
        }
        
        throw Utils.error("There should be a split point between these two commits.");
    }

}
