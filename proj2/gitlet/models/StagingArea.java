package gitlet.models;

import gitlet.utils.Utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import static gitlet.utils.Constants.STAGING_INDEX;

/**
 * @className: StagingArea
 * @description: TODO
 * @author: Wenjie FU
 * @date: 25/01/2024
 **/
public class StagingArea implements Serializable {
  private HashMap<String, String> stagedBlobs;
  private HashSet<String> removedBlobs;

  public void add(String fileName, String blobHash) {
    stagedBlobs.put(fileName, blobHash);
  }

  public void persist() {
    Utils.writeObject(STAGING_INDEX, this);
  }
}