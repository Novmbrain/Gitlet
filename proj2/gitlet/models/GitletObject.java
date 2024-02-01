package gitlet.models;

import java.io.Serializable;

/**
 * @className: GitletObject
 * @description: TODO
 * @author: Wenjie FU
 * @date: 27/01/2024
 **/
public abstract class GitletObject implements Serializable {
  public abstract String sha1Hash();
}