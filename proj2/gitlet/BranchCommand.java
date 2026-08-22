package gitlet;

import gitlet.Context;

class BranchCommand {
    static void branch(String branch_name, Context context) {
        if (context.references.hasPointer(branch_name)) {
            Utils.message("A branch with that name already exists.");
            System.exit(0);
        }
        context.references.updatePointer(branch_name, context.references.getHEAD());
    }

    static void rmBranch(String branch_name, Context context) {
        if (!context.references.hasPointer(branch_name)) {
            Utils.message("A branch with that name does not exist.");
            System.exit(0);
        }
        if (context.references.getBranch().equals(branch_name)) {
            Utils.message("Cannot remove the current branch.");
            System.exit(0);
        }
        context.references.removePointer(branch_name);
    }
}
