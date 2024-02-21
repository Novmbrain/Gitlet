package gitlet.handllers;

import gitlet.models.Commit;
import gitlet.models.Repository;

/**
 * @className: Handler5
 * @description: Handle the case where the file is not in split non other but in HEAD → HEAD, do nothing
 * @author: Wenjie FU
 * @date: 15/02/2024
 **/
public class MergeHandler5 implements IMergeHandler {
    @Override
    public boolean handle(String fileName,
                          Commit headCommit,
                          Commit givenCommit,
                          Commit splitPointCommit,
                          Repository repository) {
        boolean handled = headCommit.containsFile(fileName)
            && !givenCommit.containsFile(fileName)
            && !splitPointCommit.containsFile(fileName);

        // Do nothing

        return handled;
    }
}
