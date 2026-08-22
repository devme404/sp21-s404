package gitlet;

import java.io.File;
import static gitlet.Utils.*;
import gitlet.Context;

class RmCommand {
    static void rm(String filename, Context context) {
        boolean is_staged = false;
        if (context.stage.isStagedAddition(filename)) {
            String hash = context.stage.getAddition(filename);
            context.stage.unstageAddition(filename);
            context.stage_store.delete(hash);
            is_staged = true;
        }

        Commit head_commit = context.headCommit();
        if (head_commit.getTracked().containsKey(filename)) {
            context.stage.stageRemoval(filename, head_commit.getTracked().get(filename));
            
            File file_to_rm = Utils.join(context.cwd, filename);
            if (file_to_rm.exists()) {
                restrictedDelete(file_to_rm);
            }
            return;
        }
        
        if (!is_staged) {
            Utils.message("No reason to remove the file.");
        }
    }

}
