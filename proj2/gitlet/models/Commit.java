package gitlet.models;

// TODO: any imports you need here

import gitlet.utils.Utils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

public class Commit implements Serializable {
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

  public Commit(String message) {
    this.message = message;
    this.timeStamp = new Date();
    this.parent = "";
  }

  public Commit(String message, Date timeStamp) {
    this.message = message;
    this.timeStamp = timeStamp;
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

  public Object[] sha1Data() {
    return new Object[]{message, timeStamp.toString(), Utils.serialize(fileNameToBlobHash), parent};
  }

  public Commit clone() {
    Commit commit = new Commit(message, timeStamp);
    commit.fileNameToBlobHash = fileNameToBlobHash;
    commit.parent = parent;
    return commit;
  }
}