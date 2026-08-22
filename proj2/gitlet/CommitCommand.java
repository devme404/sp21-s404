package gitlet;

import java.util.*;

class CommitCommand {
    static void commit(String message, String second_parent, Context context) {
        if (message.length() == 0) {
            Utils.message("Please enter a commit message.");
            System.exit(0);
        }
        if (context.stage.isAdditionEmpty() && context.stage.isRemovalEmpty()) {
            Utils.message("No changes added to the commit.");
            return;
        }

        Commit last_commit = context.headCommit();
        Map<String, String> tracked = new TreeMap<>();
        tracked.putAll(last_commit.getTracked());

        Map<String, String> addition_stage = context.stage.getAdditions();
        for (Map.Entry<String, String> entry : addition_stage.entrySet()) {
            String hash = entry.getValue();

            tracked.put(entry.getKey(), entry.getValue());

            context.blob_store.store(hash, context.stage_store.load(hash));
            context.stage_store.delete(hash);
        }
        context.stage.clearAddition();

        Map<String, String> deletion_stage = context.stage.getRemovals();
        for (Map.Entry<String, String> entry : deletion_stage.entrySet()) {
            tracked.remove(entry.getKey());
        }
        context.stage.clearRemoval();

        Commit new_commit = new Commit(message, last_commit.getID(), second_parent, tracked);
        new_commit.updateID();

        context.commit_tree.addCommit(new_commit.getID(), new_commit);

        context.references.updateHEAD(new_commit.getID());
        context.references.updatePointer(context.references.getBranch(), new_commit.getID());
    }

}
