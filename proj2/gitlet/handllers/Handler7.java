package gitlet.handllers;

import gitlet.models.Commit;
import gitlet.models.Repository;

/**
 * @className: Handler7
 * @description: Handle the case where the file is modified in other and HEAD
 *     - in the same way →do nothing
 *     - in diff ways → conflict
 * @author: Wenjie FU
 * @date: 15/02/2024
 **/
public class Handler7 implements IHandler{
  @Override
  public boolean handle(String fileName, Commit headCommit, Commit givenCommit, Commit splitPointCommit, Repository repository) {
    boolean handled = false;

    if (headCommit.containsFile(fileName)
      && givenCommit.containsFile(fileName)
      && splitPointCommit.containsFile(fileName)) {

      String splitFileHash = splitPointCommit.getBlob(fileName).getFileHash();

      if (headCommit.isFileHashMatching(fileName, givenCommit.getBlob(fileName).getFileHash())) {
        // Do nothing
        handled = true;
      } else if (!headCommit.isFileHashMatching(fileName, splitFileHash) && !givenCommit.isFileHashMatching(fileName, splitFileHash)) {
        // conflict handling

        handled = true;
      }
    }
    return handled;
  }

  private void handleConflict(String fileName, Commit headCommit, Commit givenCommit, Commit splitPointCommit, Repository repository) {

  }
}