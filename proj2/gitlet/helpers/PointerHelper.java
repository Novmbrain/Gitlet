package gitlet.helpers;

import gitlet.models.Branch;
import gitlet.utils.Utils;

import java.io.File;

import static gitlet.utils.Constants.*;


/**
 * @className: HEADHelper
 * @description: TODO
 * @author: Wenjie FU
 * @date: 22/01/2024
 **/
public class PointerHelper {
  public static String loadHEAD() {
    if (HEAD_FILE.exists()) {
      return null;
      // return Utils.readContentsAsString(HEAD_FILE);
    } else {
      return null;
    }
  }

  public static void saveHead(String HEAD) {
    Utils.writeContents(HEAD_FILE, HEAD);
  }


  /**
   * We assume that the branch's tip is the same as HEAD commit
   *
   * @return
   */
  public static Branch loadCurrentBranch() {
    // if no branch exists, return null
    if (!HEAD_FILE.exists()) {
      return null;
    } else {
      // get the last part of the path in HEAD_FILE, for example
      String[] splitPath = Utils.readContentsAsString(HEAD_FILE).split(File.pathSeparator);
      String branchName = splitPath[splitPath.length - 1];

      return new Branch(branchName);
    }
  }
}