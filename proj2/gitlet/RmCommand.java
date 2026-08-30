package gitlet;

import java.io.File;
import static gitlet.Utils.*;
class RmCommand {
    static void rm(String filename, Context context) {
        boolean isStaged = false;
        if (context.stage.isStagedAddition(filename)) {
            String hash = context.stage.getAddition(filename);
            context.stage.unstageAddition(filename);
            if (!context.stage.hasHash(hash)) {
                context.stageStore.delete(hash);
            }
            isStaged = true;
        }

        Commit headCommit = context.headCommit();
        if (headCommit.getTracked().containsKey(filename)) {
            context.stage.stageRemoval(filename, headCommit.getTracked().get(filename));
            
            File fileToRm = Utils.join(context.cwd, filename);
            if (fileToRm.exists()) {
                restrictedDelete(fileToRm);
            }
            return;
        }
        
        if (!isStaged) {
            Utils.message("No reason to remove the file.");
        }
    }

}
