package gitlet.handllers;

import gitlet.models.Commit;
import gitlet.models.Repository;

/**
 * @className: Hander1
 * @description: Handle the case where the file is modified in other but not HEAD → Other"
 * @author: Wenjie FU
 * @date: 15/02/2024
 **/
public class MergeHandler1 implements MergeHandler {
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

            if (headCommit.isFileEqual(fileName, splitFileHash)
                && !givenCommit.isFileEqual(fileName, splitFileHash)) {
                // 1. take the version of other branch
                repository.checkoutFileFromCommit(givenCommit.getSha1Hash(), fileName);
                // 2. add the file to the staging area
                repository.add(fileName);
                handled = true;
            }
        }

        return handled;
    }
}