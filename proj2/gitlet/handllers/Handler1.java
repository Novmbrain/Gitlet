package gitlet.handllers;

import gitlet.models.Commit;
import gitlet.models.Repository;

/**
 * @className: Hander1
 * @description: Handle the case where the file is modified in other but not HEAD → Other"
 * @author: Wenjie FU
 * @date: 15/02/2024
 **/
public class Handler1 implements IHandler {
  @Override
  public boolean handle(String fileName, Commit headCommit, Commit givenCommit, Commit splitPointCommit, Repository repository) {
    boolean handled = false;

    if (headCommit.containsFile(fileName)
      && givenCommit.containsFile(fileName)
      && splitPointCommit.containsFile(fileName)) {

      String splitFileHash = splitPointCommit.getBlob(fileName).getFileHash();

      if (headCommit.isFileIdentical(fileName, splitFileHash)
        && !givenCommit.isFileIdentical(fileName, splitFileHash)) {
        // 1. take the version of other branch
        repository.checkoutFileFromCommit(givenCommit.sha1Hash, fileName);
        // 2. add the file to the staging area
        repository.add(fileName);
        handled = true;
      }
    }

    return handled;
  }
}