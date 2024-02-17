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

import static gitlet.utils.Constants.*;
import static gitlet.utils.Utils.join;
import static gitlet.utils.Utils.writeContents;

/**
 * singleton HEAD
 */
public class Head {
  private static String HEADCommitPath;
  private static Commit HEADCommit;

  static {
    HEADCommitPath = Utils.readContentsAsString(HEAD_FILE);
    String HEADCommitHash = Utils.readContentsAsString(new File(HEADCommitPath));
    HEADCommit = ObjectsHelper.getCommit(HEADCommitHash);
  }

  public static Commit getHEADCommit() {
    return HEADCommit;
  }

  public static void update(Commit commit, String branchName) {
    HEADCommit = commit;
    HEADCommitPath = Utils.join(REFS_HEADS_DIR, branchName).getPath();
  }

  public static boolean contains(String fileName) {
   return HEADCommit.containsFile(fileName);
  }

  public static Blob getBlob(String fileName) {
    String blobHash = HEADCommit.getFileNameToBlobHash().get(fileName);
    return ObjectsHelper.getBlob(blobHash);
  }

  public static String getHEADCommitPath() {
    return HEADCommitPath;
  }

  public static void persist(){
    String branchName = new File(HEADCommitPath).getName();
    writeContents(HEAD_FILE, join(REFS_HEADS_DIR, branchName).toString());
  }
}