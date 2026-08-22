package gitlet;

import java.io.File;
import gitlet.CommitTree.*;

class Context {
    final File cwd;
    final Stage stage;
    final CommitTree commit_tree;
    final References references;
    final BlobStore blob_store;
    final BlobStore stage_store;

    Context(File cwd, Stage stage, CommitTree commit_tree, References references, BlobStore blob_store, BlobStore stage_store) {
        this.cwd = cwd;
        this.stage = stage;
        this.commit_tree = commit_tree;
        this.references = references;
        this.blob_store = blob_store;
        this.stage_store = stage_store;
    }

    Commit headCommit() {
        return commit_tree.getCommit(references.getHEAD());
    }
}
