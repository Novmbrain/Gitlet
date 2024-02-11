package gitlet.models;


import gitlet.utils.ObjectsHelper;
import gitlet.utils.Utils;

import java.util.Date;
import java.util.HashMap;

import static gitlet.utils.Constants.CWD;

public class Commit extends GitletObject {

  /**
   * The message of this Commit.
   */
  private final String message;
  /**
   * Time at which a commit is created. Assigned by the constructor
   */
  private final Date timeStamp;
  /**
   * A map that links file name to its blob
   */
  private HashMap<String, String> fileNameToBlobHash;
  /**
   * * The parent commit of a commit object
   */
  private String parentHash = "";

  public Commit(String message, Date timeStamp) {
    this.message = message;
    this.timeStamp = timeStamp;
    this.fileNameToBlobHash = new HashMap<>();
  }

  public String getMessage() {
    return message;
  }

  public Date getTimeStamp() {
    return timeStamp;
  }

  public HashMap<String, String> getFileNameToBlobHash() {
    return fileNameToBlobHash;
  }

  /**
   * this method build the next commit
   *
   * @param message
   * @return
   */
  public Commit buildNext(String message) {
    Commit commit = new Commit(message, new Date());
    commit.fileNameToBlobHash = fileNameToBlobHash;
    commit.parentHash = this.sha1Hash();
    return commit;
  }

  @Override
  public String sha1Hash() {
    return Utils.sha1(message, timeStamp.toString(), Utils.serialize(fileNameToBlobHash), parentHash);
  }

  public void persist() {
    ObjectsHelper.persistObject(this.sha1Hash(), this);
  }

  public void updateIndex(StagingArea stagingArea) {
    HashMap<String, String> stagedBlobs = stagingArea.getStagedBlobs();

    stagingArea.getStagedBlobs().forEach((fileName, blobHash) -> {
      if (fileNameToBlobHash.containsKey(fileName)) {
        fileNameToBlobHash.replace(fileName, blobHash);
      } else {
        fileNameToBlobHash.put(fileName, blobHash);
      }
    });

    stagingArea.getRemovedBlobs().forEach(fileNameToBlobHash::remove);
  }

  public Commit getParentCommit() {
    return ObjectsHelper.getCommit(parentHash);
  }

  public boolean isInitialCommit() {
    return parentHash.isEmpty();
  }

  public boolean containsFile(String fileName) {
    return fileNameToBlobHash.containsKey(fileName);
  }

  public boolean isFileContentHashMatching(String fileName) {
    if (!fileNameToBlobHash.containsKey(fileName)) {
      return false;
    }

    String blobHash = fileNameToBlobHash.get(fileName);
    Blob blob = ObjectsHelper.getBlob(blobHash);
    String hash = Utils.sha1(fileName, Utils.readContentsAsString(Utils.join(CWD, fileName)));
    return blob.getFileHash().equals(hash);
  }

  public Blob getBlob(String fileName) {
    String blobHash = fileNameToBlobHash.get(fileName);
    return ObjectsHelper.getBlob(blobHash);
  }
}