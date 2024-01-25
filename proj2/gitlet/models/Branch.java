package gitlet.models;

import gitlet.utils.Utils;

import static gitlet.utils.Constants.REFS_HEADS_DIR;

/**
 * @className: Branch
 * @description: TODO
 * @author: Wenjie FU
 * @date: 22/01/2024
 **/
public class Branch {

  private String name;

  private String tipHash;

  public Branch(String name) {
    this.name = name;
  }

  public Branch(String name, String tipHash) {
    this.name = name;
    this.tipHash = tipHash;
  }

  public String getName() {
    return name;
  }

  public String getTipHash() {
    return tipHash;
  }

  public void setTipCommit(Commit commit) {
    this.tipHash = Utils.sha1(commit.sha1Data());
    persistBranchTip();
  }

  private void persistBranchTip() {
    Utils.writeContents(Utils.join(REFS_HEADS_DIR, name), tipHash);
  }
}