package gitlet.models;

// TODO: any imports you need here

import gitlet.utils.Utils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

import static gitlet.utils.Constants.OBJECTS_DIR;

public class Commit implements Serializable, Hashable {
  /**
   * TODO: add instance variables here.
   *
   * List all instance variables of the Commit class here with a useful
   * comment above them describing what that variable represents and how that
   * variable is used. We've provided one example for `message`.
   */
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
  private String parent = "";

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

  public String getParent() {
    return parent;
  }

  /**
   * this method build the next commit
   * @param message
   * @return
   */
  public Commit buildNext(String message) {
    Commit commit = new Commit(message, new Date());
    commit.fileNameToBlobHash = fileNameToBlobHash;
    commit.parent = this.sha1Hash();
    return commit;
  }

  @Override
  public String sha1Hash() {
    return Utils.sha1(message, timeStamp.toString(), Utils.serialize(fileNameToBlobHash), parent);
  }

  public void persist() {
    Utils.writeObject(Utils.join(OBJECTS_DIR, this.sha1Hash()), this);
  }

  public void updateIndex(StagingArea stagingArea) {
    HashMap<String, String> stagedBlobs = stagingArea.getStagedBlobs();
    for (String fileName : stagedBlobs.keySet()) {
        // if file name doest not exists in fileNameToBlobMap -> add it. Otherwise -> replace it
        fileNameToBlobHash.put(fileName, stagedBlobs.get(fileName));

        // TODO: deal with the case "stage for removal"
    }

  }
}