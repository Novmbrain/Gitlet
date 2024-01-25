package gitlet.helpers;

import gitlet.models.Branch;
import gitlet.utils.Utils;

import static gitlet.utils.Constants.HEAD_FILE;
import static gitlet.utils.Constants.REFS_HEADS_DIR;

/**
 * @className: HEADHelper
 * @description: TODO
 * @author: Wenjie FU
 * @date: 25/01/2024
 **/
public class HEADHelper {
  public static void persistHEAD(Branch branch) {
    Utils.writeContents(HEAD_FILE, Utils.join(REFS_HEADS_DIR, branch.getName()).toString());
  }
}