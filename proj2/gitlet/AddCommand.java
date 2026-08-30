package gitlet;

import java.io.File;
import java.util.*;
import static gitlet.Utils.*;
class AddCommand {
    static void add(String filename, Context context) {
        if (context.stage.isStagedRemoval(filename)) {
            context.stage.unstageRemoval(filename);
            return;
        }

        File fileToAdd = Utils.join(context.cwd, filename);
        if (!fileToAdd.exists()) {
            Utils.message("File does not exist.");
            return;
        }

        byte[] content = Utils.readContents(fileToAdd);
        String hash = Utils.sha1(content);

        String trackedHash, oldHash;

        if (context.headCommit().getTracked().containsKey(filename)) {
            trackedHash = context.headCommit().getTracked().get(filename);
            if (trackedHash.equals(hash)) {
                if (context.stage.isStagedAddition(filename)) {
                    oldHash = context.stage.getAddition(filename);
                    context.stageStore.delete(oldHash);
                    context.stage.unstageAddition(filename);
                }
                return;
            }
        }

        if (!context.stage.isStagedAddition(filename)) {
            context.stageStore.store(hash, content);
            context.stage.stageAddition(filename, hash);
            return;
        }


        oldHash = context.stage.getAddition(filename);
        if (oldHash.equals(hash)) {
            return;
        }

        context.stageStore.store(hash, content);
        context.stageStore.delete(oldHash);
        context.stage.stageAddition(filename, hash);
    }
}
