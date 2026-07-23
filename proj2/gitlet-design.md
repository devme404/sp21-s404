# Gitlet Design Document

**Name**: D

## Classes and Data Structures

### Main

This is the entry point to our program. It receives command-line parameters and parses them into different commands. It also validates parameters and throws error messages when they does not meets the format.

#### Fields

This class has no fields.


### Repository

This is the repository state we maintain mainly. It stores paths of different directories and the state of the gitlet repository.

It could initialize the repository which saves the state into the file. And it could check whether the current working directory is a gitlet repository.

All gitlet commands are implemented in this class. Detailed explanation will be in Algorithms part.

#### Fields

1. `public static final File CWD = new File(System.getProperty("user.dir"))` The Current Working Directory. It offers base path for other directories.
2. `public static final File GITLET_DIR = join(CWD, ".gitlet")` The gitlet directory. This directory stores all files related to the program.
3. `public static final File STAGE_DIR = join(GITLET_DIR, "stage")` The stage directory. This directory stores all staged files.
4. `public static final File BLOB_DIR = join(GITLET_DIR, "blob")` The blob directory. This directory stores all committed files.
5. `public static final File STATE_DIR = join(GITLET_DIR, "state")` The state directory. This directory stores the state of gitlet repository which implements the persistence of the program.
6. `public State state_` The state variable. It stores information about gitlet repository with a custom class named `State`. Details will be explained in its section.
7. `private static final String HEAD_POINTER = "HEAD"` The HEAD Pointer. This could be integrated into `State` class, but it works now.

### Commit

This class implements an abstract data structure for a commit. It contains all related data and provides a method to calculate hash value for itself.

#### Fields

1. `public String message_` The commit message.
2. `public String parent_commit_` The hash of the parent commit of this commit.
3. `public String second_parent_commit_` The hash of the other parent commit of this commit. This would be `null` unless this is a merge commit.
4. `public Date date_and_time_`  The date and time of this commit.
5. `public String id_` The hash of this commit.
6. `public HashMap<String, String> tracked_ = new HashMap<>()` Tracked files of this commit. `Key` is the filename and `Value` is the hash value of files.


### State

This class stores all data related to the gitlet repository. The default branch would be `master`. Details are below.

#### Fields

1. `public HashMap<String, Commit> commit_tree = new HashMap<>()` The commit tree. `Key` is the hash value of commits and `Value` is the corresponding `Commit` object.
2. `public HashMap<String, String> addition_stage = new HashMap<>()` Staged files for addition. `Key` is the filename of files and `Value` is the hash value.
3. `public HashMap<String, String> deletion_stage = new HashMap<>()` Staged files for deletion. `Key` is the filename of files and `Value` is the hash value.
4. `public HashMap<String, String> pointers_ = new HashMap<>()` Pointers to latest commits of branches. `Key` is the branch name and `Value` is the hash of the corresponding commit.
5. `public String branch_ = ""` Current branch name.


## Algorithms

### Main

This class contains simple functions for validating command-line arguments.

#### Methods

1. `private static void checkOneArg(String[] argv)` Checks whether there is only one argument for commands. `argv` is the raw command-line arguments. If user does not pass two parameters then the program exits.
2. `private static void checkZeroArg(String[] args)` Checks whether there is not any argument for commands. `argv` is the raw command-line arguments. If user does not pass only one parameter, then the program exits.

### Commit

This class contains simple functions deals with data about the commit.

#### Methods

1. `public Commit(String message, String parent_commit, String second_parent_commit)` Commit Initialization. It directly sets `date_and_time_` as current date and time. 
2. `public void updateID()` Calculate the hash value of the commit. It calculates the hash value by `sha1` the sum of strings of `date_and_time_`, `message_` and `parent_commit_` sequentially.

### State

This class contains repository data and only has one initialization function.

#### Methods

1. `public void init()` Initialization a repository state. It creates a default commit in the commit tree, and setup default branch and `HEAD` pointer.

### Repository

This class implements all main functions of the gitlet program. Basically, `Main` class would call corresponding function here according to the command user input.

#### Methods

1. `public void init()` Initialization function. It firstly checks whether the current working directory is a gitlet repository. If it is, then the program exits. If it is not, it creates related directories and saves current state for persistence.
2. `public void check()` Checks the existence of the gitlet repository. It checks the `.gitlet` directory. When it does not exist, the program exits for safety.
3. `public void log()` Print repository log. It firstly gets the HEAD commit, then reverse the commit tree by `parent_commit_`. Then it print commit information according to whether it is a merge commit. Then it deals with the special case where the default commit does not have a parent commit.
4. `public void globalLog()` Print all log disorderly. It simply iterates the commit tree `HashMap` and print every commit log.
5. `public void save()` Save the current state. It writes current repository state into the state file.
6. `public void add(String filename)` Stages file for addition. The most difficulty part is removing duplicated staged file when the same file is already tracked. Then if the file is not staged, it stages it and returns. If the file is staged, it replaces it.
7. `public void rm(String filename)` Stages file for removal. If the file is already staged for addition, it removes the file and returns. Then if the HEAD commit contains the file, it stages the file for removal. Otherwise, it refuses to change anything.
8. `public void commit(String message, String second_parent_commit)` Makes a commit. It tracks files based on the addition stage and untracks files based on the deletion stage. Then it updates the commit tree, `HEAD` pointer and the branch pointer.
9. `public void find(String message)` Finds the commit with given message. It just iterates the commit tree `HashMap`.
10. `public void status()` Prints current status. It prints current branches, staged files, removed files, modifications and untracked files. The later two parts are a bit complex. For modifications, it firstly iterates tracked files to find whether they are modified or deleted. Then it repeats those steps for addition stage. For untracked files, details are explained in the related utility function.
11. `public void checkout(String commit_id, String filename)` Checkouts certain file from certain commit. It simply replaces the file from corresponding tracked file.
12. `public void checkout(String filename)` Checkouts certain file from latest commit.
13. `public void checkoutBranch(String branch)` Checkouts certain branch. It checkouts from the latest commit of that branch.
14. `public void branch(String branch_name)` Creates a branch with given name. It sets the latest commit as the latest commit of the branch.
15. `public void rmBranch(String branch_name)` Removes a branch with given name.
16. `public void reset(String commit_id)` Resets to certain commit. It checkouts to certain commit, and updates `HEAD` pointer and the branch.
17. `public void merge(String branch)` Merges current branch and given branch. This is the most complex function. Firstly, it finds the split point of these two branches. Then it iterates tracked files of the split point commit and handles different situations. Secondly, it iterates tracked files of given branch and handles different situations. All situations are identified by the existence and the hash value of files.
18. `private void readState()` Loads stored repository state.
19. `private String commitToString(Commit commit)` Converts a commit to a string.
20. `private String mergeCommitToString(Commit commit)` Converts a merge commit to a string.
21. `private List<String> scanUntrackedFiles()` Finds untracked files. It detects deleted files from deletion stage and untracked files from latest commit.
22. `private void checkoutCommit(String commit_id)` Checkouts to certain commit. It checkouts all tracked files from given commit and removes all files the given commit does not track.
23. `private String findCommit(String commit_id)` Finds certain commit with the substring of its hash value. It simply iterates the commit tree.
24. `private HashMap<String, Integer> findParentDistance(String commit)` Calculates distances to other commits. It uses BFS.
25. `private String findSplitPoint(String first_commit, String second_commit)` Finds the split point commit of given commits. It firstly calculates distances of given commits, then finds the shortest parent commit of those two commits.

## Persistence

The directory structure is like this:
```
CWD              -- Current working directory
|___ .gitlet     -- gitlet repository data here
    |___ stage   -- stage files named their hash values
    |___ blob    -- tracked files named their hash values
    |___ state   -- gitlet repository state data
```

The `Repository` class sets up persistence, including:
1. Creates `.gitlet`, `stage` and `blob` folders if they do not exist.
2. Creates a `State` variable storing repository state and writes it into target path, which creates the folder if it does not exist.

`add` and `rm` commands does following things:
1. Creates stage files when they does not exist.
2. Replaces existing but different stage files.
3. Removes existing stage files when `rm` is used.

`commit` command does following things:
1. Creates tracked files in `blob` folder when they does not exist.
2. Modifies repository state and replace existing state file.

`merge` command does similar things as `commit`. It creates a merge commit.

The `State` class handles the serialization of `State` objects by default.
