package gitlet;

import java.io.File;
import java.util.*;
import static gitlet.Utils.*;
import static gitlet.StuUtils.*;
public class Logger {
    private final Context context;

    public Logger(Context context) {
        this.context = context;
    }

    public void status() {
        printBranches();
        printStaged();
        printRemoved();
        printModifications();
        printUntracked();
    }

    private void printBranches() {
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
    }

    private void printStaged() {
        Utils.message("=== Staged Files ===");
        List<String> stagedFiles = new ArrayList<>();
        Map<String, String> additionStage = context.stage.getAdditions();
        for (Map.Entry<String, String> entry : additionStage.entrySet()) {
            stagedFiles.add(entry.getKey());
        }
        Collections.sort(stagedFiles);
        for (String file : stagedFiles) {
            Utils.message(file);
        }
        System.out.println();
    }

    private void printRemoved() {
        Utils.message("=== Removed Files ===");
        List<String> removedFiles = new ArrayList<>();
        Map<String, String> deletionStage = context.stage.getRemovals();
        for (Map.Entry<String, String> entry : deletionStage.entrySet()) {
            removedFiles.add(entry.getKey());
        }
        Collections.sort(removedFiles);
        for (String file : removedFiles) {
            Utils.message(file);
        }
        System.out.println();
    }

    private void printModifications() {
        Utils.message("=== Modifications Not Staged For Commit ===");
        Map<String, String> trackedFiles = context.commitTree
                .getCommit(context.references.getHEAD()).getTracked();
        Map<String, String> additionStage = context.stage.getAdditions();
        List<String> modifications = new ArrayList<>();
        for (Map.Entry<String, String> entry : trackedFiles.entrySet()) {
            String filename = entry.getKey();
            if (context.stage.isStagedAddition(filename)) {
                continue;
            }
            
            File file = Utils.join(context.cwd, filename);
            if (file.exists()) {
                byte[] contentOfFileInCwd = readContents(file);
                String hashOfFileInCwd = Utils.sha1(contentOfFileInCwd);
                if (!hashOfFileInCwd.equals(entry.getValue())) {
                    modifications.add(filename + " (modified)");
                }
            } else {
                if (!context.stage.isStagedRemoval(filename)) {
                    modifications.add(filename + " (deleted)");
                }
            }
        }

        for (Map.Entry<String, String> entry : additionStage.entrySet()) {
            String filename = entry.getKey();

            File file = Utils.join(context.cwd, filename);
            if (file.exists()) {
                byte[] contentOfFileInCwd = Utils.readContents(file);
                String hashOfFileInCwd = Utils.sha1(contentOfFileInCwd);
                if (!hashOfFileInCwd.equals(entry.getValue())) {
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
    }

    private void printUntracked() {
        Utils.message("=== Untracked Files ===");
        List<String> files = scanUntrackedFiles(context);
        for (String file : files) {
            Utils.message(file);
        }
        System.out.println();
    }

    public void log() {
        Commit commitPtr = context.headCommit();

        while (!commitPtr.isInitialCommit()) {
            String commitInfo = commitPtr.toString();
            Utils.message(commitInfo);
            commitPtr = context.commitTree.getCommit(commitPtr.getParentID());
        }

        String commitInfo = commitPtr.toString();
        Utils.message(commitInfo);
    }

    public void globalLog() {
        Map<String, Commit> commitTree = context.commitTree.getCommitTree();
        for (Map.Entry<String, Commit> entry : commitTree.entrySet()) {
            Commit commit = entry.getValue();
            String commitInfo = commit.toString();
            Utils.message(commitInfo);
        }
    }
}
