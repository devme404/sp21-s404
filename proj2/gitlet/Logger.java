package gitlet;

import java.io.File;
import java.util.*;
import gitlet.References.*;
import gitlet.Stage.*;
import gitlet.CommitTree.*;
import static gitlet.Utils.*;
import static gitlet.StuUtils.*;
import gitlet.Context;

public class Logger {
    private final Context context;

    public Logger(Context context) {
        this.context = context;
    }

    public void status() {
        Utils.message("=== Branches ===");
        List<String> branches = context.references.getBranches();
        for (String branch : branches) {
            if (branch.equals(context.references.getBranch())) {
                Utils.message("*" + branch);
                continue;
            }
            Utils.message(branch);
        }
        System.out.println();

        Utils.message("=== Staged Files ===");
        List<String> staged_files = new ArrayList<>();
        Map<String, String> addition_stage = context.stage.getAdditions();
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
        Map<String, String> deletion_stage = context.stage.getRemovals();
        for (Map.Entry<String, String> entry : deletion_stage.entrySet()) {
            removed_files.add(entry.getKey());
        }
        Collections.sort(removed_files);
        for (String file : removed_files) {
            Utils.message(file);
        }
        System.out.println();

        Utils.message("=== Modifications Not Staged For Commit ===");
        Map<String, String> tracked_files = context.commit_tree.getCommit(context.references.getHEAD()).getTracked();
        List<String> modifications = new ArrayList<>();
        for (Map.Entry<String, String> entry : tracked_files.entrySet()) {
            String filename = entry.getKey();
            if (context.stage.isStagedAddition(filename)) {
                continue;
            }
            
            File file = Utils.join(context.cwd, filename);
            if (file.exists()) {
                byte[] content_of_file_in_cwd = readContents(file);
                String hash_of_file_in_cwd = Utils.sha1(filename, content_of_file_in_cwd);
                if (!hash_of_file_in_cwd.equals(entry.getValue())) {
                    modifications.add(filename + " (modified)");
                }
            } else {
                if (!context.stage.isStagedRemoval(filename)) {
                    modifications.add(filename + " (deleted)");
                }
            }
        }

        for (Map.Entry<String, String> entry : addition_stage.entrySet()) {
            String filename = entry.getKey();

            File file = Utils.join(context.cwd, filename);
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
        List<String> files = scanUntrackedFiles(context);
        for (String file : files) {
            Utils.message(file);
        }
        System.out.println();

    }

    public void log() {
        Commit commit_ptr = context.headCommit();

        while (!commit_ptr.isInitialCommit()) {
            String commit_info = commit_ptr.toString();
            Utils.message(commit_info);
            commit_ptr = context.commit_tree.getCommit(commit_ptr.getParentID());
        }

        String commit_info = commit_ptr.toString();
        Utils.message(commit_info);
    }

    public void globalLog() {
        Map<String, Commit> commit_tree = context.commit_tree.getCommitTree();
        for (Map.Entry<String, Commit> entry : commit_tree.entrySet()) {
            Commit commit = entry.getValue();
            String commit_info = commit.toString();
            Utils.message(commit_info);
        }
    }
}
