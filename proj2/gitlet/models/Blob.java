package gitlet.models;

import gitlet.utils.RepositoryHelper;
import gitlet.utils.Utils;

/**
 * @className: Blob
 * @description: Blob is a file that is stored in the .gitlet/objects directory. It is uniquely identified by the sha1 hash of its contents.
 *              The contents of a blob are the contents of the file it represents.
 * @author: Wenjie FU
 * @date: 25/01/2024
 **/
public class Blob extends GitletObject {
  private final String fileName;
  private final String fileHash;
  private final String content;

  public Blob(String fileName, String content) {
    this.fileName = fileName;
    this.content = content;
    this.fileHash = Utils.sha1(fileName, content);
  }

  public String getFileName() {
    return fileName;
  }

  public String getFileHash() {
    return fileHash;
  }

  public String getContent() {
    return content;
  }

  @Override
  protected String sha1Hash() {
    return Utils.sha1(fileName, content, fileHash);
  }

  public void persist() {
    RepositoryHelper.persistObject(this.sha1Hash(), this);
  }
}