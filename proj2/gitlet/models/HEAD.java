package gitlet.models;

/**
 * @className: HEAD
 * @description: TODO
 * @author: Wenjie FU
 * @date: 26/01/2024
 **/

import gitlet.utils.ObjectsHelper;
import gitlet.utils.Utils;

import java.io.File;

import static gitlet.utils.Constants.HEAD_FILE;

/**
 * singleton HEAD
 */
public class HEAD {
  private static HEAD head;
  private String HEADCommitPath;
  private Commit HEADCommit;

  private HEAD() {
    this.HEADCommitPath = Utils.readContentsAsString(HEAD_FILE);
    String HEADCommitHash = Utils.readContentsAsString(new File(HEADCommitPath));
    this.HEADCommit = ObjectsHelper.getCommit(HEADCommitHash);
  }

  public Commit getHEADCommit() {
    return HEADCommit;
  }

  public static HEAD getInstance() {
    if (head == null) {
      return new HEAD();
    } else {
      return head;
    }
  }
}