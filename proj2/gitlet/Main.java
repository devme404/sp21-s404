package gitlet;


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
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                repo.init();
                repo.save();
                break;
            case "add":
                repo.check();
                checkArgsNum(args);
                repo.add(args[1]);
                repo.save();
                break;
            case "rm":
                repo.check();
                checkArgsNum(args);
                repo.rm(args[1]);
                repo.save();
                break;
            case "commit":
                repo.check();
                if (args.length < 2) {
                    Utils.message("Please enter a commit message.");
                    System.exit(0);
                }
                repo.commit(args[1], "");
                repo.save();
                break;
            case "log":
                repo.check();
                repo.log();
                break;
            case "global-log":
                repo.check();
                repo.globalLog();
                break;
            case "find":
                repo.check();
                checkArgsNum(args);
                repo.find(args[1]);
                break;
            case "status":
                repo.check();
                repo.status();
                break;
            case "checkout":
                repo.check();
                if (args.length == 2) {
                    repo.checkoutBranch(args[1]);
                } else if (args.length == 3) {
                    repo.checkout(args[2]);
                } else if (args.length == 4) {
                    repo.checkout(args[1], args[3]);
                } else {
                    Utils.message("Incorrect operands.");
                    System.exit(0);
                }
                repo.save();
                break;
            case "branch":
                repo.check();
                checkArgsNum(args);
                repo.branch(args[1]);
                repo.save();
                break;
            case "rm-branch":
                repo.check();
                checkArgsNum(args);
                repo.rmBranch(args[1]);
                repo.save();
                break;
            case "reset":
                repo.check();
                checkArgsNum(args);
                repo.reset(args[1]);
                repo.save();
                break;
            case "merge":
                repo.check();
                checkArgsNum(args);
                repo.merge(args[1]);
                repo.save();
                break;
            default:
                Utils.message("No command with that name exists.");
                System.exit(0);
                break;
        }
    }

    private static void checkArgsNum(String[] argv) {
        if (argv.length < 2 || argv.length > 2) {
            Utils.message("Incorrect operands.");
            System.exit(0);
        }
    }
}
