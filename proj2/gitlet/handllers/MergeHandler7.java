package gitlet.handllers;

import gitlet.models.Blob;
import gitlet.models.Commit;
import gitlet.models.Repository;
import gitlet.utils.Utils;

import static gitlet.utils.Constants.CWD;

/**
 * @className: Handler7
 * @description: Handle the case where the file is modified in other and HEAD
 *     - in the same way →do nothing
 *     - in diff ways → conflict
 * @author: Wenjie FU
 * @date: 15/02/2024
 **/
public class MergeHandler7 implements IMergeHandler {
  @Override
  public boolean handle(String fileName, Commit headCommit, Commit givenCommit, Commit splitPointCommit, Repository repository) {
    boolean handled = false;

    if (headCommit.containsFile(fileName)
      && givenCommit.containsFile(fileName)) {

      Blob blob = splitPointCommit.getBlob(fileName);
      String splitFileHash = blob == null ? "" : blob.getFileHash();

      if (headCommit.isFileIdentical(fileName, givenCommit.getBlob(fileName).getFileHash())) {
        // Do nothing
        handled = true;
      } else if (!headCommit.isFileIdentical(fileName, splitFileHash) && !givenCommit.isFileIdentical(fileName, splitFileHash)) {
        // conflict handling
        handleConflict(fileName, headCommit, givenCommit, repository);
        handled = true;
      }
    }
    return handled;
  }

  private void handleConflict(String fileName, Commit headCommit, Commit givenCommit, Repository repository) {
    String headFileContent = headCommit.getBlob(fileName).getContent();
    String givenFileContent = givenCommit.getBlob(fileName).getContent();

    String conflictContent = "<<<<<<< HEAD\n" + headFileContent + "\n" + "=======\n" + givenFileContent + "\n" + ">>>>>>>";
    Utils.writeContents(Utils.join(CWD, fileName), conflictContent);
    repository.add(fileName);
  }
}