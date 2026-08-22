package gitlet;

import java.util.*;
import java.io.File;
import static gitlet.Utils.*;
import static gitlet.StuUtils.*;
import gitlet.Context;
class CheckoutCommand {
    static void checkout(String commit_id, String filename, Context context) {
        String id = findCommit(commit_id, context);
        Map<String, String> tracked_files = context.commit_tree.getCommit(id).getTracked();

        if (!tracked_files.containsKey(filename)) {
            Utils.message("File does not exist in that commit.");
            System.exit(0);
        }

        File file = Utils.join(context.cwd, filename);
        Utils.writeContents(file, context.blob_store.load(tracked_files.get(filename)));
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
        Map<String, String> target_files = context.commit_tree.getCommit(id).getTracked();

        List<String> untracked_files = scanUntrackedFiles(context);

        for (String file : untracked_files) {
            if (target_files.containsKey(file)) {
                Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }

        Map<String, String> addition_stage = context.stage.getAdditions();
        for (Map.Entry<String, String> entry : addition_stage.entrySet()) {
            context.stage_store.delete(entry.getValue());
        }

        context.stage.clearAddition();
        context.stage.clearRemoval();

        for (Map.Entry<String, String> entry : target_files.entrySet()) {
            String filename = entry.getKey();
            File file_in_cwd = Utils.join(context.cwd, filename);
            Utils.writeContents(file_in_cwd, context.blob_store.load(entry.getValue()));
        }

        Map<String, String> current_tracked_files = context.headCommit().getTracked();

        for (Map.Entry<String, String> entry : current_tracked_files.entrySet()) {
            String filename = entry.getKey();
            if (!target_files.containsKey(filename)) {
                File file = Utils.join(context.cwd, filename);
                if (file.exists()) {
                    restrictedDelete(file);
                }
            }
        }
    }
}
