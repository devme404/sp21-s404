package gitlet;

import gitlet.Remotes.*;
import gitlet.Repository.*;
import gitlet.Context.*;
import gitlet.Commit.*;
import gitlet.StuUtils.*;
import gitlet.CheckoutCommand.*;

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
        if (!remote.GITLET_DIR.exists()) {
            throw Utils.error("Remote directory not found.");
        }
    }

    static void push(Context local, Context remote, String branch) {
        String local_head = local.references.getPointer(branch);
        if (remote.references.hasPointer(branch)) {
            String remote_head = remote.references.getPointer(branch);
            if (!isAncestor(remote_head, local_head, local)) {
                throw Utils.error("Please pull down remote changes before pushing.");
            }
        }
        StuUtils.transfer(local, remote, local_head);
        remote.references.updatePointer(branch, local_head);
        if (remote.references.getBranch().equals(branch)) {
            CheckoutCommand.checkoutCommit(local_head, remote);
            remote.references.updateHEAD(local_head);
        }
    }
    
    static void fetch(Context local, Context remote, String remoteBranch, String localBranch) {
        if (!remote.references.hasPointer(remoteBranch)) {
            throw Utils.error("That remote does not have that branch.");
        }
        String remote_head = remote.references.getPointer(remoteBranch);
        StuUtils.transfer(remote, local, remote_head);
        local.references.updatePointer(localBranch, remote_head);
    }

    private static boolean isAncestor(String ancestor, String descendant, Context ctx) {
        if (ancestor.equals(descendant)) {
            return true;
        }
        // If ancestor not in local commit tree, it's definitely not an ancestor -> reject push
        if (!ctx.commit_tree.hasCommit(ancestor)) {
            return false;
        }
        java.util.Queue<String> q = new java.util.LinkedList<>();
        java.util.Set<String> visited = new java.util.HashSet<>();
        q.add(descendant);
        visited.add(descendant);
        while (!q.isEmpty()) {
            String cur = q.remove();
            Commit c = ctx.commit_tree.getCommit(cur);
            if (c == null) continue;
            String p1 = c.getParentID();
            String p2 = c.getSecondParentID();
            if (!p1.isEmpty()) {
                if (p1.equals(ancestor)) return true;
                if (!visited.contains(p1)) {
                    visited.add(p1);
                    q.add(p1);
                }
            }
            if (!p2.isEmpty()) {
                if (p2.equals(ancestor)) return true;
                if (!visited.contains(p2)) {
                    visited.add(p2);
                    q.add(p2);
                }
            }
        }
        return false;
    }
}
