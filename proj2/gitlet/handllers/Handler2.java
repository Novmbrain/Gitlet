package gitlet.handllers;

import gitlet.models.Commit;
import gitlet.models.Repository;

/**
 * @className: Handler2
 * @description: Handle the case where the file is modified in HEAD but not other → take HEAD"
 * @author: Wenjie FU
 * @date: 15/02/2024
 **/
public class Handler2 implements IHandler {
  @Override
  public boolean handle(String fileName, Commit headCommit, Commit givenCommit, Commit splitPointCommit, Repository repository) {
    boolean handled = false;

    if (headCommit.containsFile(fileName)
      && givenCommit.containsFile(fileName)
      && splitPointCommit.containsFile(fileName)) {

      String splitFileHash = splitPointCommit.getBlob(fileName).getFileHash();

      if (!headCommit.isFileHashMatching(fileName, splitFileHash) && givenCommit.isFileHashMatching(fileName, splitFileHash)) {
        // Do nothing
        handled = true;
      }
    }
    return handled;
  }
}