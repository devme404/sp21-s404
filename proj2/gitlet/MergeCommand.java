package gitlet;

import java.io.File;
import java.util.*;
import static gitlet.StuUtils.*;
import static gitlet.AddCommand.*;
import static gitlet.CheckoutCommand.*;
class MergeCommand {
    static void resolveConflict(Context context, String filename,
            Commit current, Commit given) {
        File f = Utils.join(context.cwd, filename);
        String currentContent = current.getTracked().containsKey(filename)
                ? context.blobStore.loadString(current.getTracked().get(filename)) : "";
        String givenContent = given.getTracked().containsKey(filename)
                ? context.blobStore.loadString(given.getTracked().get(filename)) : "";
        Utils.writeContents(f, "<<<<<<< HEAD\n" + currentContent
                + "=======\n" + givenContent + ">>>>>>>\n");
        AddCommand.add(filename, context);
    }

    private static boolean willBeModified(String filename, Commit split,
            Commit curr, Commit given) {
        boolean inSplit = split.getTracked().containsKey(filename);
        boolean inCurr = curr.getTracked().containsKey(filename);
        boolean inGiven = given.getTracked().containsKey(filename);
        int state = (inSplit ? 1 : 0) | (inCurr ? 2 : 0) | (inGiven ? 4 : 0);
        switch (state) {
            case 1:
            case 2:
                return false;
            case 3:
                return true;
            case 4:
                return true;
            case 5:
                return !split.getTracked().get(filename)
                        .equals(given.getTracked().get(filename));
            case 6:
                return !curr.getTracked().get(filename)
                        .equals(given.getTracked().get(filename));
            case 7:
                String splitBlob = split.getTracked().get(filename);
                String currBlob = curr.getTracked().get(filename);
                String givenBlob = given.getTracked().get(filename);
                if (Objects.equals(currBlob, givenBlob)) {
                    return false;
                }
                if (Objects.equals(givenBlob, splitBlob)) {
                    return false;
                }
                return true;
            default:
                return false;
        }
    }

    private static boolean handleMergeFiles(Commit split, Commit curr,
            Commit given, Context context) {
        List<String> untrackedFiles = scanUntrackedFiles(context);
        Set<String> filenames = new HashSet<>();
        filenames.addAll(split.getTracked().keySet());
        filenames.addAll(curr.getTracked().keySet());
        filenames.addAll(given.getTracked().keySet());
        Set<String> toBeModified = new HashSet<>();
        for (String filename : filenames) {
            if (willBeModified(filename, split, curr, given)) {
                toBeModified.add(filename);
            }
        }
        if (!Collections.disjoint(toBeModified, untrackedFiles)) {
            throw Utils.error("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
        }
        boolean isConflicted = false;
        for (String filename : filenames) {
            boolean handled = handleOneFile(filename, split, curr, given,
                    context);
            if (handled) {
                isConflicted = true;
            }
        }
        if (isConflicted) {
            Utils.message("Encountered a merge conflict.");
        }
        return true;
    }

    private static boolean handleOneFile(String filename, Commit split,
            Commit curr, Commit given, Context context) {
        boolean inSplit = split.getTracked().containsKey(filename);
        boolean inCurr = curr.getTracked().containsKey(filename);
        boolean inGiven = given.getTracked().containsKey(filename);
        int state = (inSplit ? 1 : 0) | (inCurr ? 2 : 0) | (inGiven ? 4 : 0);
        switch (state) {
            case 1:
            case 2:
                return false;
            case 3:
                return handleState3(filename, split, curr, given, context);
            case 4:
                CheckoutCommand.checkout(given.getID(), filename, context);
                AddCommand.add(filename, context);
                return false;
            case 5:
                return handleState5(filename, split, curr, given, context);
            case 6:
                return handleState6(filename, curr, given, context);
            case 7:
                return handleState7(filename, split, curr, given, context);
            default:
                throw Utils.error("This case should not be reached.");
        }
    }

    private static boolean handleState3(String filename, Commit split,
            Commit curr, Commit given, Context context) {
        if (split.getTracked().get(filename)
                .equals(curr.getTracked().get(filename))) {
            RmCommand.rm(filename, context);
            return false;
        }
        resolveConflict(context, filename, curr, given);
        return true;
    }

    private static boolean handleState5(String filename, Commit split,
            Commit curr, Commit given, Context context) {
        if (split.getTracked().get(filename)
                .equals(given.getTracked().get(filename))) {
            return false;
        }
        resolveConflict(context, filename, curr, given);
        return true;
    }

    private static boolean handleState6(String filename, Commit curr,
            Commit given, Context context) {
        if (curr.getTracked().get(filename)
                .equals(given.getTracked().get(filename))) {
            return false;
        }
        resolveConflict(context, filename, curr, given);
        return true;
    }

    private static boolean handleState7(String filename, Commit split,
            Commit curr, Commit given, Context context) {
        String splitBlob = split.getTracked().get(filename);
        String currBlob = curr.getTracked().get(filename);
        String givenBlob = given.getTracked().get(filename);
        if (Objects.equals(currBlob, givenBlob)) {
            return false;
        }
        if (Objects.equals(currBlob, splitBlob)) {
            CheckoutCommand.checkout(given.getID(), filename, context);
            AddCommand.add(filename, context);
            return false;
        }
        if (Objects.equals(givenBlob, splitBlob)) {
            return false;
        }
        resolveConflict(context, filename, curr, given);
        return true;
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


        Commit given = context.commitTree.getCommit(
                context.references.getPointer(branch));
        Commit curr = context.headCommit();
        Commit split = context.commitTree.getCommit(
                findSplitPoint(curr.getID(), given.getID(), context));

        if (split.getID().equals(given.getID())) {
            Utils.message("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }
        
        if (split.getID().equals(curr.getID())) {
            CheckoutCommand.checkoutBranch(branch, context);
            Utils.message("Current branch fast-forwarded.");
            return;
        }

        if (!handleMergeFiles(split, curr, given, context)) {
            return;
        }
        CommitCommand.commit("Merged " + branch + " into "
                + context.references.getBranch() + ".", given.getID(), context);

    }
}
