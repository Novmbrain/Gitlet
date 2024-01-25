package gitlet.models;

import gitlet.utils.Utils;

import java.io.Serializable;

/**
 * @className: Blob
 * @description: Blob is a file that is stored in the .gitlet/objects directory. It is uniquely identified by the sha1 hash of its contents.
 *              The contents of a blob are the contents of the file it represents.
 * @author: Wenjie FU
 * @date: 25/01/2024
 **/
public class Blob implements Serializable {
  private String fileName;
  private String fileHash;
  private String content;

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
}