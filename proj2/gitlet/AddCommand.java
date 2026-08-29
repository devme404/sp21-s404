package gitlet;

import java.io.File;
import java.util.*;
import static gitlet.Utils.*;
import gitlet.Context;

class AddCommand {
    static void add(String filename, Context context) {
        if (context.stage.isStagedRemoval(filename)) {
            context.stage.unstageRemoval(filename);
            return;
        }

        File file_to_add = Utils.join(context.cwd, filename);
        if (!file_to_add.exists()) {
            Utils.message("File does not exist.");
            return;
        }

        byte[] content = Utils.readContents(file_to_add);
        String hash = Utils.sha1(content);

        String tracked_hash, old_hash;

        if (context.headCommit().getTracked().containsKey(filename)) {
            tracked_hash = context.headCommit().getTracked().get(filename);
            if (tracked_hash.equals(hash)) {
                if (context.stage.isStagedAddition(filename)) {
                    old_hash = context.stage.getAddition(filename);
                    context.stage_store.delete(old_hash);
                    context.stage.unstageAddition(filename);
                }
                return;
            }
        }

        if (!context.stage.isStagedAddition(filename)) {
            context.stage_store.store(hash, content);
            context.stage.stageAddition(filename, hash);
            return;
        }


        old_hash = context.stage.getAddition(filename);
        if (old_hash.equals(hash)) {
            return;
        }

        context.stage_store.store(hash, content);
        context.stage_store.delete(old_hash);
        context.stage.stageAddition(filename, hash);
    }
}
