package gitlet.handllers;

import gitlet.models.Commit;
import gitlet.models.Repository;

/**
 * @className: Handler6
 * @description: Handle the case where the file is not in split non HEAD but in other → Other, stage fro addition
 * @author: Wenjie FU
 * @date: 15/02/2024
 **/
public class MergeHandler6 implements IMergeHandler {
  @Override
  public boolean handle(String fileName, Commit headCommit, Commit givenCommit, Commit splitPointCommit, Repository repository) {
    boolean handled = false;

    if (!headCommit.containsFile(fileName)
      && givenCommit.containsFile(fileName)
      && !splitPointCommit.containsFile(fileName)) {
      // 1. take the version of other branch
      repository.checkoutFileFromCommit(givenCommit.sha1Hash, fileName);
      // 2. add the file to the staging area
      repository.add(fileName);
      handled = true;
    }

    return handled;
  }
}