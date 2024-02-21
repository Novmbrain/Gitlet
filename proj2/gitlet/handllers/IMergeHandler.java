package gitlet.handllers;

import gitlet.models.Commit;
import gitlet.models.Repository;

/**
 * @className: IHandler
 * @description: 1. Handler1 : Modified in other but not HEAD → Other, stage for addition
 * 2. Handler2 : Modified in HEAD but not other → HEAD, do nothing
 * 3. Handler3 : Unmodified in HEAD but not present in other → Other, stage for removal
 * 4. Handler4 : modified in Other but not present in HEAD → Remain removal, do nothing
 * 5. Handler5 : Not int split non other but in HEAD → HEAD, do nothing
 * 6. Handler6 : Not in split non HEAD but in other → Other, stage fro addition
 * 7. Handler7 : Modified in other and HEAD
 * - in the same way →do nothing
 * - in diff ways → conflit
 * @author: Wenjie FU
 * @date: 15/02/2024
 **/
public interface IMergeHandler {
    boolean handle(String fileName,
                   Commit headCommit,
                   Commit givenCommit,
                   Commit splitPointCommit,
                   Repository repository);
}
