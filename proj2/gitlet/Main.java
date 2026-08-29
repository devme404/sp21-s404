package gitlet;

import java.util.*;
import java.io.File;
import java.util.function.Consumer;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            Utils.message("Please enter a command.");
            System.exit(0);
        }
        Repository repo = new Repository();

        Map<String, Consumer<String[]>> dispatch = new HashMap<>();
        dispatch.put("init", a -> runInit(repo, a));
        dispatch.put("commit", a -> runCommit(repo, a));
        dispatch.put("add", a -> runStateful(repo, a, 1, x -> repo.add(x[1])));
        dispatch.put("rm", a -> runStateful(repo, a, 1, x -> repo.rm(x[1])));
        dispatch.put("log", a -> runReadOnly(repo, a, 0, () -> repo.log()));
        dispatch.put("global-log", a -> runReadOnly(repo, a, 0, () -> repo.globalLog()));
        dispatch.put("status", a -> runReadOnly(repo, a, 0, () -> repo.status()));
        dispatch.put("find", a -> runReadOnly(repo, a, 1, x -> repo.find(x[1])));
        dispatch.put("checkout", a -> runCheckout(repo, a));
        dispatch.put("branch", a -> runStateful(repo, a, 1, x -> repo.branch(x[1])));
        dispatch.put("rm-branch", a -> runStateful(repo, a, 1, x -> repo.rmBranch(x[1])));
        dispatch.put("reset", a -> runStateful(repo, a, 1, x -> repo.reset(x[1])));
        dispatch.put("merge", a -> runStateful(repo, a, 1, x -> repo.merge(x[1])));
        dispatch.put("add-remote", a -> runStateful(repo, a, 2, x -> repo.addRemote(x[1], x[2])));
        dispatch.put("rm-remote", a -> runStateful(repo, a, 1, x -> repo.rmRemote(x[1])));
        dispatch.put("push", a -> runReadOnly(repo, a, 2, x -> {
            Repository remote = setupRemote(repo, x);
            repo.push(remote, x[2]);
            remote.save();
        }));
        dispatch.put("fetch", a -> runStateful(repo, a, 2, x -> {
            Repository remote = setupRemote(repo, x);
            repo.fetch(remote, x[2], x[1] + "/" + x[2]);
        }));
        dispatch.put("pull", a -> runStateful(repo, a, 2, x -> {
            Repository remote = setupRemote(repo, x);
            repo.pull(remote, x[2], x[1] + "/" + x[2]);
        }));

        try {
            String cmd = args[0];
            if (!dispatch.containsKey(cmd)) {
                throw Utils.error("No command with that name exists.");
            }
            dispatch.get(cmd).accept(args);
        } catch (GitletException e) {
            Utils.message(e.getMessage());
            System.exit(0);
        }
    }

    private static void checkArg(String[] args, int operands) {
        if (args.length != operands + 1) {
            throw Utils.error("Incorrect operands.");
        }
    }

    private static void runStateful(Repository repo, String[] args, int operands, Consumer<String[]> action) {
        repo.check();
        checkArg(args, operands);
        action.accept(args);
        repo.save();
    }

    private static void runReadOnly(Repository repo, String[] args, int operands, Runnable action) {
        repo.check();
        checkArg(args, operands);
        action.run();
    }

    private static void runReadOnly(Repository repo, String[] args, int operands, Consumer<String[]> action) {
        repo.check();
        checkArg(args, operands);
        action.accept(args);
    }
    
    private static Repository setupRemote(Repository repo, String[] args) {
        String remote = repo.checkRemote(args[1]);
        Repository remoteRepo = new Repository(new File(remote));
        repo.checkRemote(remoteRepo);
        return remoteRepo;
    }

    private static void runInit(Repository repo, String[] args) {
        checkArg(args, 0);
        repo.init();
        repo.save();
    }

    private static void runCommit(Repository repo, String[] args) {
        if (args.length < 2) {
            throw Utils.error("Please enter a commit message.");
        }
        repo.check();
        checkArg(args, 1);
        repo.commit(args[1], "");
        repo.save();
    }

    private static void runCheckout(Repository repo, String[] args) {
        repo.check();
        if (args.length == 2 && !args[1].equals("--")) {
            repo.checkoutBranch(args[1]);
        } else if (args.length == 3 && args[1].equals("--")) {
            repo.checkout(args[2]);
        } else if (args.length == 4 && args[2].equals("--")) {
            repo.checkout(args[1], args[3]);
        } else {
            throw Utils.error("Incorrect operands.");
        }
        repo.save();
    }
}
