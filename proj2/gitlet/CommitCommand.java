package gitlet;

import java.util.*;

class CommitCommand {
    static void commit(String message, String secondParent, Context context) {
        if (message.length() == 0) {
            Utils.message("Please enter a commit message.");
            System.exit(0);
        }
        if (context.stage.isAdditionEmpty() && context.stage.isRemovalEmpty()) {
            Utils.message("No changes added to the commit.");
            return;
        }

        Commit lastCommit = context.headCommit();
        Map<String, String> tracked = new TreeMap<>();
        tracked.putAll(lastCommit.getTracked());

        Map<String, String> additionStage = context.stage.getAdditions();
        Set<String> seen = new HashSet<>();
        for (Map.Entry<String, String> entry : additionStage.entrySet()) {
            String hash = entry.getValue();
            tracked.put(entry.getKey(), entry.getValue());
            if (!seen.contains(hash)) {
                context.blobStore.store(hash, context.stageStore.load(hash));
                seen.add(hash);
            }
        }
        for (String hash : seen) {
            context.stageStore.delete(hash);
        }
        context.stage.clearAddition();

        Map<String, String> deletionStage = context.stage.getRemovals();
        for (Map.Entry<String, String> entry : deletionStage.entrySet()) {
            tracked.remove(entry.getKey());
        }
        context.stage.clearRemoval();

        Commit newCommit = new Commit(message, lastCommit.getID(), secondParent, tracked);
        newCommit.updateID();

        context.commitTree.addCommit(newCommit.getID(), newCommit);

        context.references.updateHEAD(newCommit.getID());
        context.references.updatePointer(context.references.getBranch(), newCommit.getID());
    }

}
