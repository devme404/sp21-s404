package gitlet;

class BranchCommand {
    static void branch(String branchName, Context context) {
        if (context.references.hasPointer(branchName)) {
            Utils.message("A branch with that name already exists.");
            System.exit(0);
        }
        context.references.updatePointer(branchName, context.references.getHEAD());
    }

    static void rmBranch(String branchName, Context context) {
        if (!context.references.hasPointer(branchName)) {
            Utils.message("A branch with that name does not exist.");
            System.exit(0);
        }
        if (context.references.getBranch().equals(branchName)) {
            Utils.message("Cannot remove the current branch.");
            System.exit(0);
        }
        context.references.removePointer(branchName);
    }
}
