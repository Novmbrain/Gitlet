package gitlet.models;

/**
 * @className: HEAD
 * @description: TODO
 * @author: Wenjie FU
 * @date: 26/01/2024
 **/

import gitlet.utils.Utils;

import java.io.File;

import static gitlet.utils.Constants.HEAD_FILE;
import static gitlet.utils.Constants.OBJECTS_DIR;

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
    this.HEADCommit = Utils.readObject(Utils.join(OBJECTS_DIR, HEADCommitHash), Commit.class);
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