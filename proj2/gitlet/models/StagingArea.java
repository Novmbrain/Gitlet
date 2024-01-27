package gitlet.models;

import gitlet.utils.ObjectsHelper;
import gitlet.utils.Utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import static gitlet.utils.Constants.*;

/**
 * @className: StagingArea
 * @description: TODO
 * @author: Wenjie FU
 * @date: 25/01/2024
 **/
public class StagingArea implements Serializable {

  public HashMap<String, String> getStagedBlobs() {
    return stagedBlobs;
  }

  /**
   * A HashMap that maps file name to blob hash
   */
  private HashMap<String, String> stagedBlobs;
  private HashSet<String> removedBlobs;

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
    return stagedBlobs.containsKey(fileName);
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

  public boolean isEmpty() {
    return stagedBlobs.isEmpty() || removedBlobs.isEmpty();
  }
}