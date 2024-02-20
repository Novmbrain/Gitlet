package gitlet.models;


import gitlet.utils.RepositoryHelper;
import gitlet.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

import static gitlet.utils.Constants.CWD;
import static gitlet.utils.Utils.join;
import static gitlet.utils.Utils.writeContents;

public class Commit extends GitletObject {

  /**
   * The message of this Commit.
   */
  private final String message;
  /**
   * Time at which a commit is created. Assigned by the constructor
   */
  private final Date timeStamp;
  public String sha1Hash = "";
  /**
   * A map that links file name to its blob
   */
  private HashMap<String, String> fileNameToBlobHash;
  /**
   * * The parent commit of a commit object
   */
  private String firstParentHash = "";
  private String secondParentHash = "";

  public Commit(String message, Date timeStamp) {
    this.message = message;
    this.timeStamp = timeStamp;
    this.fileNameToBlobHash = new HashMap<>();
  }

  public String getFirstParentHash() {
    return firstParentHash;
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
    commit.firstParentHash = this.sha1Hash;
    return commit;
  }

  @Override
  protected String sha1Hash() {
    return Utils.sha1(message, timeStamp.toString(), Utils.serialize(fileNameToBlobHash), firstParentHash);
  }

  public void persist() {
    sha1Hash = this.sha1Hash();
    RepositoryHelper.persistObject(sha1Hash, this);
  }

  public void updateIndex(StagingArea stagingArea) {
    stagingArea.getStagedBlobs().forEach((fileName, blobHash) -> {
      if (fileNameToBlobHash.containsKey(fileName)) {
        fileNameToBlobHash.replace(fileName, blobHash);
      } else {
        fileNameToBlobHash.put(fileName, blobHash);
      }
    });

    stagingArea.getRemovedBlobs().forEach(fileNameToBlobHash::remove);
  }

  public Commit getFirstParentCommit() {
    if (firstParentHash.isEmpty()) {
      return null;
    } else {
      return RepositoryHelper.getCommit(firstParentHash);
    }
  }

  public boolean containsFile(String fileName) {
    return fileNameToBlobHash.containsKey(fileName);
  }

  public boolean isFileInRepoIdentical(String fileName) {
    if (!fileNameToBlobHash.containsKey(fileName)) {
      return false;
    }

    String blobHash = fileNameToBlobHash.get(fileName);
    Blob blob = RepositoryHelper.getBlob(blobHash);
    String hash = Utils.sha1(fileName, Utils.readContentsAsString(Utils.join(CWD, fileName)));
    return blob.getFileHash().equals(hash);
  }

  public boolean isFileInRepoIdentical(String fileName, String fileHash) {
    if (!fileNameToBlobHash.containsKey(fileName)) {
      return false;
    }

    String blobHash = fileNameToBlobHash.get(fileName);
    Blob blob = RepositoryHelper.getBlob(blobHash);
    return blob.getFileHash().equals(fileHash);
  }

  public Blob getBlob(String fileName) {
    String blobHash = fileNameToBlobHash.get(fileName);
    return blobHash == null ? null : RepositoryHelper.getBlob(blobHash);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Commit commit = (Commit) o;
    return Objects.equals(message, commit.message) && Objects.equals(timeStamp, commit.timeStamp) && Objects.equals(fileNameToBlobHash, commit.fileNameToBlobHash) && Objects.equals(firstParentHash, commit.firstParentHash);
  }

  @Override
  public int hashCode() {
    return Objects.hash(message, timeStamp, fileNameToBlobHash, firstParentHash);
  }

  public Set<String> getAllFiles() {
    return this.fileNameToBlobHash.keySet();
  }

  public boolean isMergeCommit() {
    return !secondParentHash.isEmpty();
  }

  public String getSecondParentHash() {
    return secondParentHash;
  }

  public void setSecondParentHash(String secondParentHash) {
    this.secondParentHash = secondParentHash;
  }

  public void restoreCommit() {
    this.getAllFiles().forEach(fileName -> {
      Blob blob = this.getBlob(fileName);
      writeContents(join(CWD, fileName), blob.getContent());
    });
  }
}