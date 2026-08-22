package gitlet;

import java.io.File;
import java.util.*;
import gitlet.BlobStore;
import static gitlet.StuUtils.*;
import static gitlet.AddCommand.*;
import static gitlet.CheckoutCommand.*;
import gitlet.Context;

class MergeCommand {
    static void merge(String branch, Context context) {
        if (context.references.getBranch().equals(branch)) {
            Utils.message("Cannot merge a branch with itself.");
            System.exit(0);
        }

        if (!context.references.hasPointer(branch)) {
            Utils.message("A branch with that name does not exist.");
            System.exit(0);
        }


        if (!context.stage.isAdditionEmpty() || !context.stage.isRemovalEmpty()) {
            Utils.message("You have uncommitted changes.");
            System.exit(0);
        }


        Commit given_branch_head = context.commit_tree.getCommit(context.references.getPointer(branch));
        Commit current_branch_head = context.headCommit();

        Commit split_point = context.commit_tree.getCommit(findSplitPoint(current_branch_head.getID(), given_branch_head.getID(), context));

        if (split_point.getID().equals(given_branch_head.getID())) {
            Utils.message("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        
        if (split_point.getID().equals(current_branch_head.getID())) {
            CheckoutCommand.checkoutBranch(branch, context);
            Utils.message("Current branch fast-forwarded.");
            return;
        }

        List<String> untracked_files = scanUntrackedFiles(context);

        boolean isConflicted = false;

        for (Map.Entry<String, String> entry : split_point.getTracked().entrySet()) {
            String split_point_filename = entry.getKey();
            String split_point_hash = entry.getValue();

            if (!given_branch_head.getTracked().containsKey(split_point_filename) && !current_branch_head.getTracked().containsKey(split_point_filename)) {
                continue;
            }

            if (given_branch_head.getTracked().containsKey(split_point_filename) && given_branch_head.getTracked().get(split_point_filename).equals(split_point_hash)) {
                continue;
            }


            if (current_branch_head.getTracked().containsKey(split_point_filename) && current_branch_head.getTracked().get(split_point_filename).equals(split_point_hash)) {
                if (untracked_files.contains(split_point_filename)) {
                    Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }

                if (!given_branch_head.getTracked().containsKey(split_point_filename)) {
                    RmCommand.rm(split_point_filename, context);
                    continue;
                }

                CheckoutCommand.checkout(given_branch_head.getID(), split_point_filename, context);
                AddCommand.add(split_point_filename, context);
                continue;
            }

            if (!current_branch_head.getTracked().containsKey(split_point_filename)) {
                if (untracked_files.contains(split_point_filename)) {
                    Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }

                isConflicted = true;
                File cwd_file = Utils.join(context.cwd, split_point_filename);
                Utils.writeContents(cwd_file, "<<<<<<< HEAD\n=======\n" + context.blob_store.loadString(given_branch_head.getTracked().get(split_point_filename)) + "\n>>>>>>>\n");
                AddCommand.add(split_point_filename, context);
            }

            if (!given_branch_head.getTracked().containsKey(split_point_filename)) {
                if (untracked_files.contains(split_point_filename)) {
                    Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }

                isConflicted = true;
                File cwd_file = Utils.join(context.cwd, split_point_filename);
                Utils.writeContents(cwd_file, "<<<<<<< HEAD\n" + context.blob_store.loadString(current_branch_head.getTracked().get(split_point_filename)) + "\n=======\n>>>>>>>\n");
                AddCommand.add(split_point_filename, context);
            }
            if (current_branch_head.getTracked().containsKey(split_point_filename) && given_branch_head.getTracked().containsKey(split_point_filename)) {
                if (current_branch_head.getTracked().get(split_point_filename).equals(given_branch_head.getTracked().get(split_point_filename))) {
                    continue;
                }

                if (untracked_files.contains(split_point_filename)) {
                    Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }


                isConflicted = true;
                File cwd_file = Utils.join(context.cwd, split_point_filename);
                Utils.writeContents(cwd_file, "<<<<<<< HEAD\n" + context.blob_store.loadString(current_branch_head.getTracked().get(split_point_filename)) + "=======\n" + context.blob_store.loadString(given_branch_head.getTracked().get(split_point_filename)) + ">>>>>>>\n");
                AddCommand.add(split_point_filename, context);
            }
        }

        for (Map.Entry<String, String> entry : given_branch_head.getTracked().entrySet()) {
            String filename = entry.getKey();
            if (!split_point.getTracked().containsKey(filename)) {
                if (current_branch_head.getTracked().containsKey(filename) && current_branch_head.getTracked().get(filename).equals(entry.getValue())) {
                    continue;
                }

                if (untracked_files.contains(filename)) {
                    Utils.message("There is an untracked file in the way; delete it, or add and commit it first.");
                    System.exit(0);
                }

                if (!current_branch_head.getTracked().containsKey(filename)) {
                    CheckoutCommand.checkout(given_branch_head.getID(), filename, context);
                    AddCommand.add(filename, context);
                    continue;
                }
                
                File cwd_file = Utils.join(context.cwd, filename);
                Utils.writeContents(cwd_file, "<<<<<<< HEAD\n" + context.blob_store.loadString(current_branch_head.getTracked().get(filename)) + "\n=======\n" + context.blob_store.loadString(entry.getValue()) + "\n>>>>>>>");
                AddCommand.add(filename, context);
                isConflicted = true;
            }
        }
        if (isConflicted) {
            Utils.message("Encountered a merge conflict.");
        }


        CommitCommand.commit("Merged " + branch + " into " + context.references.getBranch() + ".", given_branch_head.getID(), context);

    }
}
