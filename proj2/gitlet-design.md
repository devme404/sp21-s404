# Gitlet Design Document

**Name**: D

## Classes and Data Structures

### Main
Entry point. Parses `args`, validates operand count via a dispatch table, delegates to `Repository`, and centralizes error handling (`GitletException` → `message` + `System.exit(0)`).

#### Fields
This class has no fields.

### Repository
Abstraction of a Gitlet repository. Owns persistence layout, in-memory state, and delegates commands to handler classes. Holds a `Context` to pass shared state to handlers.

#### Fields
1. `public File CWD` — working directory (default `user.dir`)
2. `public File GITLET_DIR` — `.gitlet` directory
3. `public File STAGE_DIR` — staged blobs (`stage/`, file name = `sha1(content)`)
4. `public File BLOB_DIR` — committed blobs (`blob/`, file name = `sha1(content)`)
5. `public File STAGE` — `stage-meta` (serialized `Stage`)
6. `public File COMMIT_TREE` — `commit-tree` (serialized `CommitTree`)
7. `public File REFERENCES` — `references` (serialized `References`)
8. `public File REMOTES` — `remotes` (serialized `Remotes`)
9. `private BlobStore stage_store` / `blob_store` — file I/O for the two blob dirs
10. `private Stage stage_` — addition & deletion stage
11. `private CommitTree commit_tree_` — all commits indexed by `sha1`
12. `private References references_` — `HEAD` + branch pointers
13. `private Remotes remotes_` — remote name → path
14. `private Logger logger_` — `log / global-log / status` printing
15. `private Context context` — shared view (`cwd, stage, commit_tree, references, blob_store, stage_store`)

`initialize(File dir)` builds all paths, creates `BlobStore`s, calls `readState()` and builds `Context`/`Logger`. Both `Repository()` and `Repository(File gitletDir)` delegate to it.

### Commit
Immutable snapshot. `parent_` is empty for the initial commit; `second_parent_` is non-empty only for merge commits.

#### Fields
1. `private String message_`
2. `private String parent_`
3. `private String second_parent_`
4. `private Date timestamp_` — epoch 0 for initial commit
5. `private String id_` — `sha1(timestamp.toString(), message_, parent_, second_parent_, tracked_.toString())`
6. `private Map<String,String> tracked_` — `TreeMap` filename → blob hash (deterministic `toString` for hashing)

### Context
Immutable carrier of repository state for handlers.

#### Fields
1. `final File cwd`
2. `final Stage stage`
3. `final CommitTree commit_tree`
4. `final References references`
5. `final BlobStore blob_store`
6. `final BlobStore stage_store`

### Stage
Addition and deletion stage.

#### Fields
1. `private Map<String,String> addition_stage` — `HashMap` filename → hash
2. `private Map<String,String> deletion_stage` — `HashMap` filename → hash

### CommitTree
All commits.

#### Fields
1. `private Map<String,Commit> commit_tree_` — `HashMap` hash → `Commit`

### BlobStore
File-based content-addressable store.

#### Fields
1. `private File WORK_DIR`

### References
Branch pointers. `HEAD` is stored as a pointer entry.

#### Fields
1. `private final String HEAD = "HEAD"`
2. `private final String DEFAULT_BRANCH = "master"`
3. `private Map<String,String> pointers_` — name → commit hash (`HashMap`)
4. `private String branch_` — current branch

### Remotes
#### Fields
1. `Map<String,String> remotes` — `TreeMap` name → path (with `/` → `File.separator` conversion)

### Logger
Printing for `log / global-log / status`.

#### Fields
1. `private final Context context`

### Command Handlers
`AddCommand`, `RmCommand`, `CommitCommand`, `CheckoutCommand`, `BranchCommand`, `MergeCommand`, `RemoteCommand` are **stateless** — all logic in `static` methods taking `Context` (and `Remotes` where needed). No fields.

### StuUtils
Utility helpers (`findCommit`, `scanUntrackedFiles`, `findParentsDistance`/`findSplitPoint`, `transfer`). No fields.

---

## Algorithms

### Main
Dispatch table `Map<String, Consumer<String[]>>`:
* `init` / `commit` have custom runners (commit checks empty message before `repo.check()`).
* `add/rm/branch/rm-branch/reset/merge/add-remote/rm-remote` → `runStateful` (check repo, check operand count, run, `save()`).
* `log/global-log/find/status` → `runReadOnly` (check, check, run).
* `checkout` → `runCheckout` (3 forms, then `save()`).
* `push/fetch/pull` → `setupRemote` (resolve remote path, open remote `Repository`) then delegate to `RemoteCommand`; `pull = fetch + merge`.

All `Incorrect operands` / `No command...` / `Not in an initialized...` are thrown as `GitletException` and handled in one `catch`.

### Repository
* `init()` — guards `GITLET_DIR` exists, creates `Stage`, initial `Commit` (epoch 0), `CommitTree`, `References` (master→initial), `Remotes`, `Context`/`Logger`, `mkdir`s, `writeObject`s.
* `check()` / `save()` / `readState()` — existence guard and `Utils.readObject/writeObject` for the four serialized files.
* Other methods are one-line delegates (`add → AddCommand.add`, etc.). `reset` resolves abbreviated id via `StuUtils.findCommit`, `checkoutCommit`, then moves `HEAD` and current branch pointer.

### Commit
`updateID()` hashes the five fields above; `toString()` formats `=== commit / Merge: / Date / message` with `SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z")`.

### Handler Highlights
* **AddCommand** — unstage removal if present; `sha1(content)`; if `headCommit` already tracks same hash, unstage addition; otherwise stage (store blob in `stage_store`). Handles overwrite of staged entry.
* **RmCommand** — unstage addition (and delete staged blob); if tracked in `HEAD`, stage removal and `restrictedDelete` from `cwd`.
* **CommitCommand** — guards empty message / empty stage; `TreeMap tracked = HEAD.tracked + additions - removals`; moves blobs `stage_store → blob_store`; creates `Commit(message, HEAD.id, second_parent, tracked)`, updates `CommitTree` and both `HEAD`/branch pointers.
* **CheckoutCommand** — `checkout(id,file)` resolves abbreviated id, validates existence, `writeContents` from `blob_store`. `checkoutCommit(id)` checks untracked-would-be-overwritten before clearing stage, writes target snapshot, deletes files not in target.
* **BranchCommand** — `branch`/`rmBranch` guard existence and `cannot remove current`.
* **MergeCommand** — guards self-merge, uncommitted changes, finds `split = LCA(curr,given)` via BFS distances, ancestor/fast-forward early returns, builds `union = split ∪ curr ∪ given`, encodes presence as `state = 1|2|4`, `switch(state)` implements spec's 8 rules (checkout/rm vs `resolveConflict`). `resolveConflict` does straight concatenation (`"<<<<<<< HEAD\n" + curContent + "=======\n" + givenContent + ">>>>>>>\n"`, empty string for deleted) and `AddCommand.add`. Finally commits `Merged [given] into [curr]` with `second_parent = given.id`, printing `Encountered a merge conflict.` if needed.
* **RemoteCommand / StuUtils.transfer** — `transfer` BFS from start commit, copies missing commits and their blobs; `push` checks `isAncestor(remoteHead, localHead)` via BFS over both parents, `fetch`/`pull` update `remoteName/branch` pointer; `pull = fetch + merge`.

### Logger
* `log()` — follows first-parent chain from `HEAD`.
* `globalLog()` — iterates `CommitTree` values.
* `status()` — sorts branches/staged/removed; for `Modifications Not Staged` checks tracked files not staged and addition-stage files against `sha1(content)` of `cwd` file; `Untracked` via `StuUtils.scanUntrackedFiles`.

### StuUtils
* `scanUntrackedFiles` — files in `deletion_stage` that re-appeared in `cwd` + files in `cwd` not tracked nor staged.
* `findSplitPoint` — BFS distances from both heads (following both parents), picks common ancestor with minimal `dist1+dist2`.

## Persistence

Directory layout:
```
CWD
└── .gitlet
    ├── stage/          — staged blobs, name = sha1(content)
    ├── blob/           — committed blobs, name = sha1(content)
    ├── stage-meta      — serialized Stage
    ├── commit-tree     — serialized CommitTree
    ├── references      — serialized References
    └── remotes         — serialized Remotes
```

Strategy — prove no state is lost across `java gitlet.Main` invocations:
* Every command constructs `Repository()` → `initialize()` → `readState()` deserializes the four files if they exist, rebuilds `Context`.
* Mutating commands (`add/rm/commit/checkout/branch/merge/remote`) mutate in-memory `Stage/CommitTree/References/Remotes` and `BlobStore` files, then `save()` serializes the four files. `commit` additionally moves blobs `stage → blob`.
* Example: `java gitlet.Main add wug.txt` stores `sha1(content)` blob in `stage/` and adds entry to `Stage`, then `writeObject(STAGE)`. Next invocation `java gitlet.Main commit -m "..."` `readObject(STAGE)` recovers the staged hash, copies blob to `blob/`, updates `CommitTree`/`References`, and `writeObject`s all four files. No in-memory field is needed across runs.
* Serialization uses `Utils.writeObject/readObject` (Java serialization); no pointers are serialized — commits reference parents/blobs by hash strings; transient fields are avoided.

## Key Design Decisions

Firstly, splitting the store of staged blobs and committed blobs ensures these two parts are independent and safe. So the stage operations like `rm` does not affect committed blobs, vice versa.

Secondly, instead of nested if-else sentences, a boolean mask implements a simplified state machine, which is more graceful for the merge logic.
