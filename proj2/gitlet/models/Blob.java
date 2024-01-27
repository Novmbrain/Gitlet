package gitlet.models;

import gitlet.utils.Utils;

import java.io.Serializable;

import static gitlet.utils.Constants.OBJECTS_DIR;

/**
 * @className: Blob
 * @description: Blob is a file that is stored in the .gitlet/objects directory. It is uniquely identified by the sha1 hash of its contents.
 *              The contents of a blob are the contents of the file it represents.
 * @author: Wenjie FU
 * @date: 25/01/2024
 **/
public class Blob implements Serializable, Hashable {
  private String fileName;
  private String fileHash;
  private String content;

  public Blob(String fileName, String content) {
    this.fileName = fileName;
    this.content = content;
    // Two files that have different names but the same content will have different sha1 hash
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
  public String sha1Hash() {
    return Utils.sha1(fileName, content, fileHash);
  }

  public void persist() {
    Utils.writeObject(Utils.join(OBJECTS_DIR, this.sha1Hash()), this);
  }
}