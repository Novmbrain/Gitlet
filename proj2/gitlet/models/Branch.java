package gitlet.models;

import gitlet.utils.Utils;

import static gitlet.utils.Constants.REFS_HEADS_DIR;

/**
 * @className: Branch
 * @author: Wenjie FU
 * @date: 22/01/2024
 **/
public class Branch {

    private final String name;

    private String tipHash;

    public Branch(String name) {
        this.name = name;
    }

    public Branch(String name, Commit tipCommit) {
        this.name = name;
        this.tipHash = tipCommit.getSha1Hash();
    }

    public Branch(String name, String tipCommitHash) {
        this.name = name;
        this.tipHash = tipCommitHash;
    }

    public String getName() {
        return name;
    }

    public String getTipHash() {
        return tipHash;
    }

    public void setTipCommit(Commit commit) {
        this.tipHash = commit.getSha1Hash();
    }

    public void persist() {
        Utils.writeContents(Utils.join(REFS_HEADS_DIR, name), tipHash);
    }
}