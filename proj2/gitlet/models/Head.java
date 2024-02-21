package gitlet.models;

/**
 * @className: HEAD
 * @author: Wenjie FU
 * @date: 26/01/2024
 **/

import gitlet.utils.RepositoryHelper;
import gitlet.utils.Utils;

import java.io.File;

import static gitlet.utils.Constants.HEAD_FILE;
import static gitlet.utils.Constants.REFS_HEADS_DIR;
import static gitlet.utils.Utils.join;
import static gitlet.utils.Utils.writeContents;

/**
 * singleton HEAD
 */
public class Head {
    private static String headCommitPath;
    private static Commit headCommit;

    static {
        headCommitPath = Utils.readContentsAsString(HEAD_FILE);
        String headCommitHash = Utils.readContentsAsString(new File(headCommitPath));
        headCommit = RepositoryHelper.getCommit(headCommitHash);
    }

    public static Commit getHeadCommit() {
        return headCommit;
    }

    public static void update(Commit commit, String branchName) {
        headCommit = commit;
        headCommitPath = Utils.join(REFS_HEADS_DIR, branchName).getPath();
    }

    public static boolean contains(String fileName) {
        return headCommit.containsFile(fileName);
    }

    public static Blob getBlob(String fileName) {
        String blobHash = headCommit.getFileNameToBlobHash().get(fileName);
        return RepositoryHelper.getBlob(blobHash);
    }

    public static String getHeadCommitPath() {
        return headCommitPath;
    }

    public static void persist() {
        String branchName = new File(headCommitPath).getName();
        writeContents(HEAD_FILE, join(REFS_HEADS_DIR, branchName).toString());
    }
}
