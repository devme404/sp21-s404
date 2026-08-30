package gitlet;

class RemoteCommand {
    static void addRemote(String remote, String path, Remotes remotes) {
        remotes.add(remote, path);
    }

    static void rmRemote(String remote, Remotes remotes) {
        remotes.remove(remote);
    }

    static String checkRemote(String remote, Remotes remotes) {
        return remotes.get(remote);
    }

    static void checkRemote(Repository remote) {
        if (!remote.gitletDir.exists()) {
            throw Utils.error("Remote directory not found.");
        }
    }

    static void push(Context local, Context remote, String branch) {
        String localHead = local.references.getPointer(branch);
        if (remote.references.hasPointer(branch)) {
            String remoteHead = remote.references.getPointer(branch);
            if (!isAncestor(remoteHead, localHead, local)) {
                throw Utils.error("Please pull down remote changes before pushing.");
            }
        }
        StuUtils.transfer(local, remote, localHead);
        remote.references.updatePointer(branch, localHead);
        if (remote.references.getBranch().equals(branch)) {
            CheckoutCommand.checkoutCommit(localHead, remote);
            remote.references.updateHEAD(localHead);
        }
    }
    
    static void fetch(Context local, Context remote, String remoteBranch, String localBranch) {
        if (!remote.references.hasPointer(remoteBranch)) {
            throw Utils.error("That remote does not have that branch.");
        }
        String remoteHead = remote.references.getPointer(remoteBranch);
        StuUtils.transfer(remote, local, remoteHead);
        local.references.updatePointer(localBranch, remoteHead);
    }

    private static boolean isAncestor(String ancestor, String descendant, Context ctx) {
        if (ancestor.equals(descendant)) {
            return true;
        }
        // If ancestor not in local commit tree, it's definitely not an ancestor -> reject push
        if (!ctx.commitTree.hasCommit(ancestor)) {
            return false;
        }
        java.util.Queue<String> q = new java.util.LinkedList<>();
        java.util.Set<String> visited = new java.util.HashSet<>();
        q.add(descendant);
        visited.add(descendant);
        while (!q.isEmpty()) {
            String cur = q.remove();
            Commit c = ctx.commitTree.getCommit(cur);
            if (c == null) {
                continue;
            }
            String p1 = c.getParentID();
            String p2 = c.getSecondParentID();
            if (!p1.isEmpty()) {
                if (p1.equals(ancestor)) {
                    return true;
                }
                if (!visited.contains(p1)) {
                    visited.add(p1);
                    q.add(p1);
                }
            }
            if (!p2.isEmpty()) {
                if (p2.equals(ancestor)) {
                    return true;
                }
                if (!visited.contains(p2)) {
                    visited.add(p2);
                    q.add(p2);
                }
            }
        }
        return false;
    }
}
