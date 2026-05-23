package gitlet;

import java.text.SimpleDateFormat;
import java.io.File;
import java.util.*;
import static gitlet.Utils.*;


/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class does at a high level.
 *
 *  @author D
 */
public class Repository {
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
    public static final File STATE = join(GITLET_DIR, "state");

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
        Utils.writeObject(STATE, state_);

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
        Utils.writeObject(STATE, state_);
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

        byte[] content_of_file_to_add = Utils.readContents(file_to_add);
        String hash_of_file_to_add = Utils.sha1(filename, content_of_file_to_add);

        File stage_of_file_to_add = Utils.join(STAGE_DIR, hash_of_file_to_add);

        if (!state_.addition_stage.containsKey(filename)) {
            Utils.writeContents(stage_of_file_to_add, content_of_file_to_add);
            state_.addition_stage.put(filename, hash_of_file_to_add);
            return;
        }

        String old_hash = state_.addition_stage.get(filename);

        if (old_hash.equals(hash_of_file_to_add)) {
            stage_of_file_to_add.delete();
            state_.addition_stage.remove(filename);
            return;
        }

        Utils.writeContents(stage_of_file_to_add, content_of_file_to_add);
        File old_stage_file = Utils.join(STAGE_DIR, old_hash);
        old_stage_file.delete();
        state_.addition_stage.put(filename, hash_of_file_to_add);
    }

    public void rm(String filename) {
        if (state_.addition_stage.containsKey(filename)) {
            String hash_of_file_to_rm = state_.addition_stage.get(filename);
            state_.addition_stage.remove(filename);
            File stage_of_file_to_rm = Utils.join(STAGE_DIR, hash_of_file_to_rm);
            stage_of_file_to_rm.delete();
            return;
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

        Utils.message("No reason to remove the file.");
    }

    public void commit(String message, String second_parent_commit) {
        if (state_.addition_stage.isEmpty() && state_.deletion_stage.isEmpty()) {
            Utils.message("No changes added to the commit.");
            return;
        }

        Commit last_commit = state_.commit_tree.get(state_.pointers_.get(HEAD_POINTER));
        Commit new_commit = new Commit(message, last_commit.id_, second_parent_commit);
        new_commit.tracked_ = new HashMap<>();
        if (state_.commit_tree.size() > 1) {
            new_commit.tracked_.putAll(last_commit.tracked_);
        }

        for (Map.Entry<String, String> entry : state_.addition_stage.entrySet()) {
            File file_to_track = Utils.join(BLOB_DIR, entry.getValue());
            File stage_of_file_to_track = Utils.join(STAGE_DIR, entry.getValue());

            new_commit.tracked_.put(entry.getKey(), entry.getValue());

            Utils.writeContents(file_to_track, Utils.readContents(stage_of_file_to_track));
            stage_of_file_to_track.delete();
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

            if (branch.equals(state_.branch_)) {
                Utils.message("*" + branch);
                continue;
            }
            
            branches.add(branch);
        }
        Collections.sort(branches);
        for (String branch : branches) {
            Utils.message(branch);
        }
        System.out.println();

        Utils.message("=== Staged Files ===");
        for (Map.Entry<String, String> entry : state_.addition_stage.entrySet()) {
            Utils.message(entry.getKey());
        }
        System.out.println();

        Utils.message("=== Removed Files ===");
        for (Map.Entry<String, String> entry : state_.deletion_stage.entrySet()) {
            Utils.message(entry.getKey());
        }
        System.out.println();

        Utils.message("=== Modifications Not Staged For Commit ===");
        HashMap<String, String> tracked_files = state_.commit_tree.get(state_.pointers_.get(HEAD_POINTER)).tracked_;
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
                    Utils.message(filename + " (modified)");
                }
            } else {
                if (!state_.deletion_stage.containsKey(filename)) {
                    Utils.message(filename + " (deleted)");
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
                    Utils.message(filename + " (modified)");
                }
            } else {
                Utils.message(filename + " (deleted)");
            }
        }
        System.out.println();

        Utils.message("=== Untracked Files ===");
        HashSet<String> files = scanUntrackedFiles();
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
        HashMap<String, String> tracked_files = state_.commit_tree.get(id_to_checkout).tracked_;

        if (!tracked_files.containsKey(filename)) {
            Utils.message("File does not exist in that commit.");
            System.exit(0);
        }

        File file_to_checkout = Utils.join(BLOB_DIR, tracked_files.get(filename));
        File file_to_checkout_in_cwd = Utils.join(CWD, filename);
        Utils.writeContents(file_to_checkout_in_cwd, Utils.readContents(file_to_checkout));
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
        state_.pointers_.put(HEAD_POINTER, id_to_reset);
        state_.pointers_.put(state_.branch_, id_to_reset);
    }

    public void merge(String branch) {
        if (!state_.addition_stage.isEmpty() || !state_.deletion_stage.isEmpty()) {
            Utils.message("You have uncommitted changes.");
            System.exit(0);
        }
        
        if (!state_.pointers_.containsKey(branch)) {
            Utils.message("A branch with that name does not exist.");
            System.exit(0);
        }

        if (state_.branch_.equals(branch)) {
            Utils.message("Cannot merge a branch with itself.");
        }

        Commit given_branch_head = state_.commit_tree.get(state_.pointers_.get(branch));
        Commit current_branch_head = state_.commit_tree.get(state_.pointers_.get(HEAD_POINTER));

        Commit split_point = state_.commit_tree.get(findSplitPoint(current_branch_head.id_, given_branch_head.id_));

        if (split_point.id_.equals(given_branch_head.id_)) {
            Utils.message("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        
        if (split_point.id_.equals(current_branch_head.id_)) {
            checkoutCommit(given_branch_head.id_);
            Utils.message("Current branch fast-forwarded.");
            return;
        }

        HashSet<String> untracked_files = scanUntrackedFiles();

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
                }

                checkout(given_branch_head.id_, split_point_filename);
            }

            if (!current_branch_head.tracked_.containsKey(split_point_filename)) {
                if (untracked_files.contains(split_point_filename)) {
                    Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }

                isConflicted = true;
                File cwd_file = Utils.join(CWD, split_point_filename);
                File given_file = Utils.join(BLOB_DIR, given_branch_head.tracked_.get(split_point_filename));
                Utils.writeContents(cwd_file, "<<<<<<< HEAD\n=======\n" + Utils.readContentsAsString(given_file) + "\n>>>>>>>\n");
            }

            if (!given_branch_head.tracked_.containsKey(split_point_filename)) {
                if (untracked_files.contains(split_point_filename)) {
                    Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }

                isConflicted = true;
                File cwd_file = Utils.join(CWD, split_point_filename);
                File current_file = Utils.join(BLOB_DIR, current_branch_head.tracked_.get(split_point_filename));
                Utils.writeContents(cwd_file, "<<<<<<< HEAD\n" + Utils.readContentsAsString(current_file) + "\n=======\n>>>>>>>\n");
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
                File current_file = Utils.join(BLOB_DIR, current_branch_head.tracked_.get(split_point_filename));
                File given_file = Utils.join(BLOB_DIR, given_branch_head.tracked_.get(split_point_filename));
                Utils.writeContents(cwd_file, "<<<<<<< HEAD\n" + Utils.readContentsAsString(current_file) + "\n=======\n" + Utils.readContentsAsString(given_file) + "\n>>>>>>>\n");
            }

            if (isConflicted) {
                add(split_point_filename); 
                Utils.message("Encountered a merge conflict.");
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
                File current_file = Utils.join(BLOB_DIR, current_branch_head.tracked_.get(filename));
                File given_file = Utils.join(BLOB_DIR, entry.getValue());
                Utils.writeContents(cwd_file, "<<<<<<< HEAD\n" + Utils.readContentsAsString(current_file) + "\n=======\n" + Utils.readContentsAsString(given_file) + "\n>>>>>>>");
                add(filename);
                Utils.message("Encountered a merge conflict.");
            }
        }

        commit("Merged " + branch + " into " + state_.branch_ + ".", given_branch_head.id_);

    }

    private void readState() {
        if (STATE.exists()) {
            state_ = Utils.readObject(STATE, State.class); 
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

    private HashSet<String> scanUntrackedFiles() {
        HashSet<String> files = new HashSet<>();
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

        return files;
    }

    private void checkoutCommit(String commit_id) {
        HashMap<String, String> target_files = state_.commit_tree.get(commit_id).tracked_;

        HashSet<String> untracked_files = scanUntrackedFiles();

        for (String file : untracked_files) {
            if (target_files.containsKey(file)) {
                Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }

        state_.addition_stage.clear();
        state_.deletion_stage.clear();

        for (Map.Entry<String, String> entry : target_files.entrySet()) {
            String filename = entry.getKey();
            File file = Utils.join(BLOB_DIR, entry.getValue());
            File file_in_cwd = Utils.join(CWD, filename);
            Utils.writeContents(file_in_cwd, Utils.readContents(file));
        }

        HashMap<String, String> current_tracked_files = state_.commit_tree.get(state_.pointers_.get(HEAD_POINTER)).tracked_;

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

    private String findSplitPoint(String first_commit, String second_commit) {
        HashSet<String> second_commit_history = new HashSet<>();
        while (!second_commit.equals("")) {
            second_commit_history.add(second_commit);
            second_commit = state_.commit_tree.get(second_commit).parent_commit_;
        }

        while (!first_commit.equals("")) {
            if (second_commit_history.contains(first_commit)) {
                return first_commit;
            }
            first_commit = state_.commit_tree.get(first_commit).parent_commit_;
        }

        Utils.error("There should be a split point between these two commits.");
        return "";
    }

}
