package gitlet.handllers;

import gitlet.models.Commit;
import gitlet.models.Repository;

/**
 * @className: Handler4
 * @description: Handle the case where the file is unmodified in Other but not present in HEAD → Remain removal, do nothing
 * @author: Wenjie FU
 * @date: 15/02/2024
 **/
public class MergeHandler4 implements IMergeHandler {
  @Override
  public boolean handle(String fileName, Commit headCommit, Commit givenCommit, Commit splitPointCommit, Repository repository) {
    boolean handled = false;

    if (!headCommit.containsFile(fileName)
      && givenCommit.containsFile(fileName)
      && splitPointCommit.containsFile(fileName)) {
      String splitFileHash = splitPointCommit.getBlob(fileName).getFileHash();
      if (givenCommit.isFileInRepoIdentical(fileName, splitFileHash)) {
        // Do nothing
        handled = true;
      }
    }

    return handled;
  }
}