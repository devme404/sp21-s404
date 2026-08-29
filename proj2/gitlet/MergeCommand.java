package gitlet;

import java.io.File;
import java.util.*;
import gitlet.BlobStore;
import static gitlet.StuUtils.*;
import static gitlet.AddCommand.*;
import static gitlet.CheckoutCommand.*;
import gitlet.Context;

class MergeCommand {
    static void resolveConflict(Context context, String filename, Commit current, Commit given) {
        File f = Utils.join(context.cwd, filename);
        String current_content = current.getTracked().containsKey(filename) ? context.blob_store.loadString(current.getTracked().get(filename)) : "";
        String given_content = given.getTracked().containsKey(filename) ? context.blob_store.loadString(given.getTracked().get(filename)) : "";
        Utils.writeContents(f, "<<<<<<< HEAD\n" + current_content + "=======\n" + given_content + ">>>>>>>\n");
        AddCommand.add(filename, context);
    }

    static void merge(String branch, Context context) {
        if (!context.references.hasPointer(branch)) {
            Utils.message("A branch with that name does not exist.");
            System.exit(0);
        }

        if (context.references.getBranch().equals(branch)) {
            Utils.message("Cannot merge a branch with itself.");
            System.exit(0);
        }

        if (!context.stage.isAdditionEmpty() || !context.stage.isRemovalEmpty()) {
            Utils.message("You have uncommitted changes.");
            System.exit(0);
        }


        Commit given = context.commit_tree.getCommit(context.references.getPointer(branch));
        Commit curr = context.headCommit();
        Commit split = context.commit_tree.getCommit(findSplitPoint(curr.getID(), given.getID(), context));

        if (split.getID().equals(given.getID())) {
            Utils.message("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        
        if (split.getID().equals(curr.getID())) {
            CheckoutCommand.checkoutBranch(branch, context);
            Utils.message("Current branch fast-forwarded.");
            return;
        }

        List<String> untracked_files = scanUntrackedFiles(context);
        boolean isConflicted = false;
        boolean in_split;
        boolean in_curr;
        boolean in_given;
        int state = 0;

        Set<String> filenames = new HashSet<>();
        filenames.addAll(split.getTracked().keySet());
        filenames.addAll(curr.getTracked().keySet());
        filenames.addAll(given.getTracked().keySet());
        
        if (!Collections.disjoint(filenames, untracked_files)) {
            throw Utils.error("There is an untracked file in the way; delete it, or add and commit it first.");
        }
        for (String filename : filenames) {
            state = 0;
            in_split = split.getTracked().containsKey(filename);
            in_curr = curr.getTracked().containsKey(filename);
            in_given = given.getTracked().containsKey(filename);
            if (in_split) {
                state |= 1;
            }
            if (in_curr) {
                state |= 2;
            }
            if (in_given) {
                state |= 4;
            }

            switch (state) {
                case 1:
                    continue;
                case 2:
                    continue;
                case 3:
                    if (split.getTracked().get(filename).equals(curr.getTracked().get(filename))) {
                        RmCommand.rm(filename, context);
                        continue;
                    }
                    resolveConflict(context, filename, curr, given);
                    isConflicted = true;
                    break;
                case 4:
                    CheckoutCommand.checkout(given.getID(), filename, context);
                    AddCommand.add(filename, context);
                    break;
                case 5:
                    if (split.getTracked().get(filename).equals(given.getTracked().get(filename))) {
                        continue;
                    }
                    resolveConflict(context, filename, curr, given);
                    isConflicted = true;
                    break;
                case 6:
                    if (curr.getTracked().get(filename).equals(given.getTracked().get(filename))) {
                        continue;
                    }
                    resolveConflict(context, filename, curr, given);
                    isConflicted = true;
                    break;
                case 7:
                    String split_blob = split.getTracked().get(filename);
                    String curr_blob = curr.getTracked().get(filename);
                    String given_blob = given.getTracked().get(filename);
                    if (Objects.equals(curr_blob, given_blob)) {
                        continue;
                    }
                    if (Objects.equals(curr_blob, split_blob)) {
                        CheckoutCommand.checkout(given.getID(), filename, context);
                        AddCommand.add(filename, context);
                        continue;
                    }
                    if (Objects.equals(given_blob, split_blob)) {
                        continue;
                    }
                    resolveConflict(context, filename, curr, given);
                    isConflicted = true;
                    break;
                default:
                    throw Utils.error("This case should not be reached.");
            }
        }

        if (isConflicted) {
            Utils.message("Encountered a merge conflict.");
        }


        CommitCommand.commit("Merged " + branch + " into " + context.references.getBranch() + ".", given.getID(), context);

    }
}
