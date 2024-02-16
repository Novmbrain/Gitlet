package gitlet.handllers;

import gitlet.models.Commit;
import gitlet.models.Repository;

/**
 * @className: Handler3
 * @description: Handle the case where the file is unmodified in HEAD but not present in other → Other, stage for removal
 * @author: Wenjie FU
 * @date: 15/02/2024
 **/
public class Handler3 implements IHandler{
  @Override
  public boolean handle(String fileName, Commit headCommit, Commit givenCommit, Commit splitPointCommit, Repository repository) {
    boolean handled = false;
    if (headCommit.containsFile(fileName) && !givenCommit.containsFile(fileName) && splitPointCommit.containsFile(fileName)) {
      String splitFileHash = splitPointCommit.getBlob(fileName).getFileHash();
      if (headCommit.isFileHashMatching(fileName, splitFileHash)) {
        repository.rm(fileName);
        handled = true;
      }
    }

    return handled;
  }

}