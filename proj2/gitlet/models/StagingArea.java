package gitlet.models;

import gitlet.utils.RepositoryHelper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import static gitlet.utils.Constants.OBJECTS_DIR;
import static gitlet.utils.Constants.STAGING_INDEX;
import static gitlet.utils.Utils.*;

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

  public static StagingArea load() {
    return readObject(STAGING_INDEX, StagingArea.class);
  }

  /**
   * add a file to the staging area if it is not already in the staging area
   * overwrite the file if it is already in the staging area. Overwriting means that
   * 1. delete file's corresponding blob in the .gitlet/objects directory
   * 2. update the file's blob hash in the staging area with the new blob hash
   *
   * @param fileName
   * @param blobHash
   */
  public void stage(String fileName, String blobHash) {
    if (contains(fileName)) {
      clearStagedBlob(fileName);
    }

    stagedBlobs.put(fileName, blobHash);
  }

  public void stageForRemoval(String fileName) {
    removedBlobs.add(fileName);
  }

  public void persist() {
    writeObject(STAGING_INDEX, this);
  }

  public boolean contains(String fileName) {
    return stagedBlobs.containsKey(fileName) || removedBlobs.contains(fileName);
  }

  public boolean StageForAdditionContains(String fileName) {
    return stagedBlobs.containsKey(fileName);
  }

  public boolean StageForRemovalContains(String fileName) {
    return removedBlobs.contains(fileName);
  }

  public void clearStagedBlob(String fileName) {
    join(OBJECTS_DIR, fileName).delete();
    stagedBlobs.remove(fileName);
    removedBlobs.remove(fileName);
  }

  public void clearAllStagedBlobs() {
    stagedBlobs.keySet().forEach(fileName -> join(OBJECTS_DIR,fileName).delete());
    stagedBlobs.clear();
  }

  public void removeAllMapping() {
    stagedBlobs.clear();
    removedBlobs.clear();
  }

  public void removeFromMapping(String fileName) {
    stagedBlobs.remove(fileName);
    removedBlobs.remove(fileName);
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

  public Blob getStagedBlob(String fileName) {
    String blobHash = stagedBlobs.get(fileName);
    return blobHash == null ? null : RepositoryHelper.getBlob(blobHash);
  }
}