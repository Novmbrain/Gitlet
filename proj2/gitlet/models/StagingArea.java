package gitlet.models;

import gitlet.utils.ObjectsHelper;
import gitlet.utils.Utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import static gitlet.utils.Constants.CWD;
import static gitlet.utils.Constants.STAGING_INDEX;

/**
 * @className: StagingArea
 * @description: TODO
 * @author: Wenjie FU
 * @date: 25/01/2024
 **/
public class StagingArea implements Serializable {


  /**
   * A HashMap that maps file name to blob hash
   */
  private final HashMap<String, String> stagedBlobs;
  private final HashSet<String> removedBlobs;

  public StagingArea() {
    this.stagedBlobs = new HashMap<>();
    this.removedBlobs = new HashSet<>();
  }

  public void add(String fileName, String blobHash) {
    stagedBlobs.put(fileName, blobHash);
  }

  public void persist() {
    Utils.writeObject(STAGING_INDEX, this);
  }

  public boolean contains(String fileName) {
    return stagedBlobs.containsKey(fileName) || removedBlobs.contains(fileName);
  }

  public void remove(String fileName) {
    Utils.restrictedDelete(stagedBlobs.get(fileName));
    stagedBlobs.remove(fileName);
  }

  // TODO: write a unit test for this method
  public boolean checkIdentical(String fileName) {
    String blobHash = stagedBlobs.get(fileName);
    Blob blob = ObjectsHelper.getBlob(blobHash);

    String hash = Utils.sha1(fileName, Utils.readContentsAsString(Utils.join(CWD, fileName)));
    return blob.getFileHash().equals(hash);
  }

  public void clear() {
    stagedBlobs.clear();
    removedBlobs.clear();
  }

  public HashSet<String> getRemovedBlobs() {
    return removedBlobs;
  }

  public HashMap<String, String> getStagedBlobs() {
    return stagedBlobs;
  }


  public boolean isEmpty() {
    return stagedBlobs.isEmpty() && removedBlobs.isEmpty();
  }
}