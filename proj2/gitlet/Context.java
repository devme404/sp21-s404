package gitlet;

import java.io.File;
class Context {
    final File cwd;
    final Stage stage;
    final CommitTree commitTree;
    final References references;
    final BlobStore blobStore;
    final BlobStore stageStore;

    Context(File cwd, Stage stage, CommitTree commitTree,
            References references, BlobStore blobStore, BlobStore stageStore) {
        this.cwd = cwd;
        this.stage = stage;
        this.commitTree = commitTree;
        this.references = references;
        this.blobStore = blobStore;
        this.stageStore = stageStore;
    }

    Commit headCommit() {
        return commitTree.getCommit(references.getHEAD());
    }
}
