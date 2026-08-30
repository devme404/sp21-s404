package gitlet;

import java.util.*;
import java.io.File;
import static gitlet.Utils.*;
import static gitlet.StuUtils.*;
class CheckoutCommand {
    static void checkout(String commitId, String filename, Context context) {
        String id = findCommit(commitId, context);
        Map<String, String> trackedFiles = context.commitTree.getCommit(id).getTracked();

        if (!trackedFiles.containsKey(filename)) {
            Utils.message("File does not exist in that commit.");
            System.exit(0);
        }

        File file = Utils.join(context.cwd, filename);
        Utils.writeContents(file, context.blobStore.load(trackedFiles.get(filename)));
    }

    static void checkoutBranch(String branch, Context context) {
        if (!context.references.hasPointer(branch)) {
            Utils.message("No such branch exists.");
            System.exit(0);
        }
        
        if (context.references.getBranch().equals(branch)) {
            Utils.message("No need to checkout the current branch.");
            System.exit(0);
        }

        checkoutCommit(context.references.getPointer(branch), context);

        context.references.updateBranch(branch);
        context.references.updateHEAD(context.references.getPointer(branch));
    }

    static void checkoutCommit(String id, Context context) {
        Map<String, String> targetFiles = context.commitTree.getCommit(id).getTracked();

        List<String> untrackedFiles = scanUntrackedFiles(context);

        for (String file : untrackedFiles) {
            if (targetFiles.containsKey(file)) {
                Utils.message("There is an untracked file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
        }

        Map<String, String> additionStage = context.stage.getAdditions();
        for (Map.Entry<String, String> entry : additionStage.entrySet()) {
            context.stageStore.delete(entry.getValue());
        }

        context.stage.clearAddition();
        context.stage.clearRemoval();

        for (Map.Entry<String, String> entry : targetFiles.entrySet()) {
            String filename = entry.getKey();
            File fileInCwd = Utils.join(context.cwd, filename);
            Utils.writeContents(fileInCwd, context.blobStore.load(entry.getValue()));
        }

        Map<String, String> currentTrackedFiles = context.headCommit().getTracked();

        for (Map.Entry<String, String> entry : currentTrackedFiles.entrySet()) {
            String filename = entry.getKey();
            if (!targetFiles.containsKey(filename)) {
                File file = Utils.join(context.cwd, filename);
                if (file.exists()) {
                    restrictedDelete(file);
                }
            }
        }
    }
}
