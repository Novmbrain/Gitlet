package gitlet.handllers;

import gitlet.models.Commit;
import gitlet.models.Repository;

/**
 * @className: Handler2
 * @description: Handle the case where the file is modified in HEAD but not other → take HEAD"
 * @author: Wenjie FU
 * @date: 15/02/2024
 **/
public class MergeHandler2 implements MergeHandler {
    @Override
    public boolean handle(String fileName,
                          Commit headCommit,
                          Commit givenCommit,
                          Commit splitPointCommit,
                          Repository repository) {
        boolean handled = false;

        if (headCommit.containsFile(fileName)
            && givenCommit.containsFile(fileName)
            && splitPointCommit.containsFile(fileName)) {

            String splitFileHash = splitPointCommit.getBlob(fileName).getFileHash();

            if (!headCommit.isFileEqual(fileName, splitFileHash)
                && givenCommit.isFileEqual(fileName, splitFileHash)) {
                handled = true;
            }
        }
        return handled;
    }
}