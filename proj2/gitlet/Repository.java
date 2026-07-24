package gitlet;

import java.text.SimpleDateFormat;
import java.io.File;
import java.util.*;
import static gitlet.Utils.*;
import static gitlet.BlobStore.*;


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

    /** The state file. */
    public static final File STATE_DIR = join(GITLET_DIR, "state");

    private static final BlobStore stage_store = new BlobStore(STAGE_DIR);

    private static final BlobStore blob_store = new BlobStore(BLOB_DIR);

    /** The state of gitlet. */
    public State state_;

    /** The name of HEAD commit pointer. */
    private static final String HEAD_POINTER = "HEAD";


    public Repository() {
        readState();
    }

    public void init() {
        if (GITLET_DIR.exists()) {
            Utils.message("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        state_ = new State();

        GITLET_DIR.mkdir();
        STAGE_DIR.mkdir();
        BLOB_DIR.mkdir();

        state_.init();
        Utils.writeObject(STATE_DIR, state_);

    }

    public void check() {
        if (GITLET_DIR.exists()) {
            return;
        }

        Utils.message("Not in an initialized Gitlet directory.");
        System.exit(0);
    }

    public void log() {
        Commit commit_ptr = state_.commit_tree.get(state_.pointers_.get(HEAD_POINTER));

        while (!commit_ptr.parent_commit_.equals("")) {
            String commit_info;
            if (commit_ptr.second_parent_commit_.isEmpty()) {
                commit_info = commitToString(commit_ptr);
            } else {
                commit_info = mergeCommitToString(commit_ptr);
            }
            Utils.message(commit_info);
            commit_ptr = state_.commit_tree.get(commit_ptr.parent_commit_);
        }

        String commit_info = commitToString(commit_ptr);
        Utils.message(commit_info);
    }

    public void globalLog() {
        for (Map.Entry<String, Commit> entry : state_.commit_tree.entrySet()) {
            Commit commit = entry.getValue();
            String commit_info;
            if (commit.second_parent_commit_.isEmpty()) {
                commit_info = commitToString(commit);
            } else {
                commit_info = mergeCommitToString(commit);
            }
            Utils.message(commit_info);
        }
    }

    public void save() {
        Utils.writeObject(STATE_DIR, state_);
    }

    public void add(String filename) {
        if (state_.deletion_stage.containsKey(filename)) {
            state_.deletion_stage.remove(filename);
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

        if (state_.commit_tree.get(state_.pointers_.get(HEAD_POINTER)).tracked_.containsKey(filename)) {
            tracked_hash = state_.commit_tree.get(state_.pointers_.get(HEAD_POINTER)).tracked_.get(filename);
            if (tracked_hash.equals(hash)) {
                if (state_.addition_stage.containsKey(filename)) {
                    old_hash = state_.addition_stage.get(filename);
                    stage_store.delete(old_hash);
                    state_.addition_stage.remove(filename);
                }
                return;
            }
        }

        if (!state_.addition_stage.containsKey(filename)) {
            stage_store.store(hash, content);
            state_.addition_stage.put(filename, hash);
            return;
        }


        old_hash = state_.addition_stage.get(filename);
        if (old_hash.equals(hash)) {
            return;
        }

        stage_store.store(hash, content);
        stage_store.delete(old_hash);
        state_.addition_stage.put(filename, hash);
    }

    public void rm(String filename) {
        boolean is_staged = false;
        if (state_.addition_stage.containsKey(filename)) {
            String hash = state_.addition_stage.get(filename);
            state_.addition_stage.remove(filename);
            stage_store.delete(hash);
            is_staged = true;
        }

        Commit head_commit = state_.commit_tree.get(state_.pointers_.get(HEAD_POINTER));
        if (head_commit.tracked_.containsKey(filename)) {
            state_.deletion_stage.put(filename, head_commit.tracked_.get(filename));
            
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
        if (state_.addition_stage.isEmpty() && state_.deletion_stage.isEmpty()) {
            Utils.message("No changes added to the commit.");
            return;
        }

        Commit last_commit = state_.commit_tree.get(state_.pointers_.get(HEAD_POINTER));
        Commit new_commit = new Commit(message, last_commit.id_, second_parent_commit);
        new_commit.tracked_ = new TreeMap<>();
        new_commit.tracked_.putAll(last_commit.tracked_);

        for (Map.Entry<String, String> entry : state_.addition_stage.entrySet()) {
            String hash = entry.getValue();

            new_commit.tracked_.put(entry.getKey(), entry.getValue());

            blob_store.store(hash, stage_store.load(hash));
            stage_store.delete(hash);
        }
        state_.addition_stage.clear();

        for (Map.Entry<String, String> entry : state_.deletion_stage.entrySet()) {
            new_commit.tracked_.remove(entry.getKey());
        }
        state_.deletion_stage.clear();

        new_commit.updateID();

        state_.commit_tree.put(new_commit.id_, new_commit);

        state_.pointers_.put(HEAD_POINTER, new_commit.id_);
        state_.pointers_.put(state_.branch_, new_commit.id_);
    }

    public void find(String message) {
        boolean found = false;
        for (Map.Entry<String, Commit> entry : state_.commit_tree.entrySet()) {
            Commit commit = entry.getValue();
            if (commit.message_.equals(message)) {
                Utils.message(commit.id_);
                found = true;
            }
        }
        if (!found) {
            Utils.message("Found no commit with that message.");
        }
    }

    public void status() {
        Utils.message("=== Branches ===");
        List<String> branches = new ArrayList<>();
        for (Map.Entry<String, String> entry : state_.pointers_.entrySet()) {
            String branch = entry.getKey();
            if (branch.equals(HEAD_POINTER)) {
                continue;
            }
            branches.add(branch);
        }
        Collections.sort(branches);
        for (String branch : branches) {
            if (branch.equals(state_.branch_)) {
                Utils.message("*" + branch);
                continue;
            }
            Utils.message(branch);
        }
        System.out.println();

        Utils.message("=== Staged Files ===");
        List<String> staged_files = new ArrayList<>();
        for (Map.Entry<String, String> entry : state_.addition_stage.entrySet()) {
            staged_files.add(entry.getKey());
        }
        Collections.sort(staged_files);
        for (String file : staged_files) {
            Utils.message(file);
        }
        System.out.println();

        Utils.message("=== Removed Files ===");
        List<String> removed_files = new ArrayList<>();
        for (Map.Entry<String, String> entry : state_.deletion_stage.entrySet()) {
            removed_files.add(entry.getKey());
        }
        Collections.sort(removed_files);
        for (String file : removed_files) {
            Utils.message(file);
        }
        System.out.println();

        Utils.message("=== Modifications Not Staged For Commit ===");
        Map<String, String> tracked_files = state_.commit_tree.get(state_.pointers_.get(HEAD_POINTER)).tracked_;
        List<String> modifications = new ArrayList<>();
        for (Map.Entry<String, String> entry : tracked_files.entrySet()) {
            String filename = entry.getKey();
            if (state_.addition_stage.containsKey(filename)) {
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
                if (!state_.deletion_stage.containsKey(filename)) {
                    modifications.add(filename + " (deleted)");
                }
            }
        }

        for (Map.Entry<String, String> entry : state_.addition_stage.entrySet()) {
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
        checkout(state_.pointers_.get(HEAD_POINTER), filename);
    }

    public void checkout(String commit_id, String filename) {
        String id_to_checkout = findCommit(commit_id);
        Map<String, String> tracked_files = state_.commit_tree.get(id_to_checkout).tracked_;

        if (!tracked_files.containsKey(filename)) {
            Utils.message("File does not exist in that commit.");
            System.exit(0);
        }

        File file_to_checkout_in_cwd = Utils.join(CWD, filename);
        Utils.writeContents(file_to_checkout_in_cwd, blob_store.load(tracked_files.get(filename)));
    }

    public void checkoutBranch(String branch) {
        if (!state_.pointers_.containsKey(branch)) {
            Utils.message("No such branch exists.");
            System.exit(0);
        }
        
        if (state_.branch_.equals(branch)) {
            Utils.message("No need to checkout the current branch.");
            System.exit(0);
        }

        checkoutCommit(state_.pointers_.get(branch));

        state_.branch_ = branch;
        state_.pointers_.put(HEAD_POINTER, state_.pointers_.get(branch));
    }

    public void branch(String branch_name) {
        if (state_.pointers_.containsKey(branch_name)) {
            Utils.message("A branch with that name already exists.");
            System.exit(0);
        }

        state_.pointers_.put(branch_name, state_.pointers_.get(HEAD_POINTER));
    }

    public void rmBranch(String branch_name) {
        if (!state_.pointers_.containsKey(branch_name)) {
            Utils.message("A branch with that name does not exist.");
            System.exit(0);
        }
        
        if (state_.branch_.equals(branch_name)) {
            Utils.message("Cannot remove the current branch.");
            System.exit(0);
        }

        state_.pointers_.remove(branch_name);
    }

    public void reset(String commit_id) {
        String id_to_reset = findCommit(commit_id);
        checkoutCommit(id_to_reset);
        state_.pointers_.put(HEAD_POINTER, id_to_reset);
        state_.pointers_.put(state_.branch_, id_to_reset);
    }

    public void merge(String branch) {
        if (state_.branch_.equals(branch)) {
            Utils.message("Cannot merge a branch with itself.");
            System.exit(0);
        }

        if (!state_.pointers_.containsKey(branch)) {
            Utils.message("A branch with that name does not exist.");
            System.exit(0);
        }


        if (!state_.addition_stage.isEmpty() || !state_.deletion_stage.isEmpty()) {
            Utils.message("You have uncommitted changes.");
            System.exit(0);
        }


        Commit given_branch_head = state_.commit_tree.get(state_.pointers_.get(branch));
        Commit current_branch_head = state_.commit_tree.get(state_.pointers_.get(HEAD_POINTER));

        Commit split_point = state_.commit_tree.get(findSplitPoint(current_branch_head.id_, given_branch_head.id_));

        if (split_point.id_.equals(given_branch_head.id_)) {
            Utils.message("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        
        if (split_point.id_.equals(current_branch_head.id_)) {
            checkoutBranch(branch);
            Utils.message("Current branch fast-forwarded.");
            return;
        }

        List<String> untracked_files = scanUntrackedFiles();

        boolean isConflicted = false;

        for (Map.Entry<String, String> entry : split_point.tracked_.entrySet()) {
            String split_point_filename = entry.getKey();
            String split_point_hash = entry.getValue();

            if (!given_branch_head.tracked_.containsKey(split_point_filename) && !current_branch_head.tracked_.containsKey(split_point_filename)) {
                continue;
            }

            if (given_branch_head.tracked_.containsKey(split_point_filename) && given_branch_head.tracked_.get(split_point_filename).equals(split_point_hash)) {
                continue;
            }


            if (current_branch_head.tracked_.containsKey(split_point_filename) && current_branch_head.tracked_.get(split_point_filename).equals(split_point_hash)) {
                if (untracked_files.contains(split_point_filename)) {
                    Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }

                if (!given_branch_head.tracked_.containsKey(split_point_filename)) {
                    rm(split_point_filename);
                    continue;
                }

                checkout(given_branch_head.id_, split_point_filename);
                add(split_point_filename);
                continue;
            }

            if (!current_branch_head.tracked_.containsKey(split_point_filename)) {
                if (untracked_files.contains(split_point_filename)) {
                    Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }

                isConflicted = true;
                File cwd_file = Utils.join(CWD, split_point_filename);
                Utils.writeContents(cwd_file, "<<<<<<< HEAD\n=======\n" + blob_store.load(given_branch_head.tracked_.get(split_point_filename)) + "\n>>>>>>>\n");
                add(split_point_filename);
            }

            if (!given_branch_head.tracked_.containsKey(split_point_filename)) {
                if (untracked_files.contains(split_point_filename)) {
                    Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }

                isConflicted = true;
                File cwd_file = Utils.join(CWD, split_point_filename);
                Utils.writeContents(cwd_file, "<<<<<<< HEAD\n" + blob_store.load(current_branch_head.tracked_.get(split_point_filename)) + "\n=======\n>>>>>>>\n");
                add(split_point_filename);
            }
            if (current_branch_head.tracked_.containsKey(split_point_filename) && given_branch_head.tracked_.containsKey(split_point_filename)) {
                if (current_branch_head.tracked_.get(split_point_filename).equals(given_branch_head.tracked_.get(split_point_filename))) {
                    continue;
                }

                if (untracked_files.contains(split_point_filename)) {
                    Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }


                isConflicted = true;
                File cwd_file = Utils.join(CWD, split_point_filename);
                Utils.writeContents(cwd_file, "<<<<<<< HEAD\n" + blob_store.load(current_branch_head.tracked_.get(split_point_filename)) + "=======\n" + blob_store.load(given_branch_head.tracked_.get(split_point_filename)) + ">>>>>>>\n");
                add(split_point_filename);
            }
        }

        for (Map.Entry<String, String> entry : given_branch_head.tracked_.entrySet()) {
            String filename = entry.getKey();
            if (!split_point.tracked_.containsKey(filename)) {
                if (current_branch_head.tracked_.containsKey(filename) && current_branch_head.tracked_.get(filename).equals(entry.getValue())) {
                    continue;
                }

                if (untracked_files.contains(filename)) {
                    Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }

                if (!current_branch_head.tracked_.containsKey(filename)) {
                    checkout(given_branch_head.id_, filename);
                    add(filename);
                    continue;
                }
                
                File cwd_file = Utils.join(CWD, filename);
                Utils.writeContents(cwd_file, "<<<<<<< HEAD\n" + blob_store.load(current_branch_head.tracked_.get(filename)) + "\n=======\n" + blob_store.load(entry.getValue()) + "\n>>>>>>>");
                add(filename);
                isConflicted = true;
            }
        }
        if (isConflicted) {
            Utils.message("Encountered a merge conflict.");
        }


        commit("Merged " + branch + " into " + state_.branch_ + ".", given_branch_head.id_);

    }

    private void readState() {
        if (STATE_DIR.exists()) {
            state_ = Utils.readObject(STATE_DIR, State.class); 
        }
    }

    private String commitToString(Commit commit) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        return String.format("===\ncommit %s\nDate: %s\n%s\n", commit.id_, sdf.format(commit.date_and_time_), commit.message_);

    }

    private String mergeCommitToString(Commit commit) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        return String.format("===\ncommit %s\nMerge: %.7s %.7s\nDate: %s\n%s\n", commit.id_, commit.parent_commit_, commit.second_parent_commit_, sdf.format(commit.date_and_time_), commit.message_);
    }

    private List<String> scanUntrackedFiles() {
        List<String> files = new ArrayList<>();
        for (Map.Entry<String, String> entry : state_.deletion_stage.entrySet()) {
            String filename = entry.getKey();
            File file = Utils.join(CWD, filename);
            if (file.exists()) {
                files.add(filename);
            }
        }
        
        List<String> files_in_cwd = Utils.plainFilenamesIn(CWD);
        for (String filename : files_in_cwd) {
            if (!state_.commit_tree.get(state_.pointers_.get(HEAD_POINTER)).tracked_.containsKey(filename) && !state_.addition_stage.containsKey(filename)) {
                files.add(filename);
            }
        }
        Collections.sort(files);

        return files;
    }

    private void checkoutCommit(String commit_id) {
        Map<String, String> target_files = state_.commit_tree.get(commit_id).tracked_;

        List<String> untracked_files = scanUntrackedFiles();

        for (String file : untracked_files) {
            if (target_files.containsKey(file)) {
                Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }

        for (Map.Entry<String, String> entry : state_.addition_stage.entrySet()) {
            stage_store.delete(entry.getValue());
        }

        state_.addition_stage.clear();
        state_.deletion_stage.clear();

        for (Map.Entry<String, String> entry : target_files.entrySet()) {
            String filename = entry.getKey();
            File file_in_cwd = Utils.join(CWD, filename);
            Utils.writeContents(file_in_cwd, blob_store.load(entry.getValue()));
        }

        Map<String, String> current_tracked_files = state_.commit_tree.get(state_.pointers_.get(HEAD_POINTER)).tracked_;

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

    private String findCommit(String commit_id) {
        boolean found = false;
        String id_to_checkout = commit_id;
        for (Map.Entry<String, Commit> entry : state_.commit_tree.entrySet()) {
            if (entry.getKey().startsWith(commit_id)) {
                id_to_checkout = entry.getValue().id_;
                found = true;
                break;
            } 

            if (entry.getKey().equals(commit_id)) {
                found = true;
                break;
            }
        }
        if (!found) {
            Utils.message("No commit with that id exists.");
            System.exit(0);
        }
        return id_to_checkout;
    }


    private HashMap<String, Integer> findParentsDistance(String commit) {
        Queue<String> queue = new LinkedList<>();
        HashMap<String, Integer> distances = new HashMap<>();

        queue.add(commit);
        distances.put(commit, 0);

        while (!queue.isEmpty()) {
            String id = queue.remove();
            Integer parent_distance = distances.get(id) + 1;
            Commit curr_commit = state_.commit_tree.get(id);
            String first_parent = curr_commit.parent_commit_;
            String second_parent = curr_commit.second_parent_commit_;

            if (!first_parent.equals("") && !distances.containsKey(first_parent)) {
                queue.add(first_parent);
                distances.put(first_parent, parent_distance);
            }
            if (!second_parent.equals("") && !distances.containsKey(second_parent)) {
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
