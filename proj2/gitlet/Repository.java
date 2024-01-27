package gitlet;

import gitlet.models.*;
import gitlet.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static gitlet.utils.Constants.*;

/**
 * Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author TODO
 */
public class Repository {

  private HEAD HEAD;
  private Branch currentBranch;
  private StagingArea stagingArea;


  /**
   * - .gitlet
   *    - objects // a directory to store serialized blobs and commit index by the two first hexadecimal sha1 hash number
   *        - file1 whose file name is the rest of the sha1 hash number
   *        - file2
   *        - ....
   *    - HEAD // file that stores the path to the current branch's hash number of the HEAD commit It points to a file under refs/heads for a branch or directly to a commit if in 'detached HEAD' state.
   *    - refs // a directory that holds references to commits, which are
   *        - heads // pointers to the tips of branches
   *    - logs // Stores the log records of changes made
   *        - master
   *        - <other branch name>
   */


  /**
   * The current working directory.
   */
  public Repository() {
    if (gitletExists()) {
      // load the staging area
      this.stagingArea = Utils.readObject(STAGING_INDEX, StagingArea.class);
      this.currentBranch = getCurrentBranch();
      this.HEAD = HEAD.getInstance();
    } else {
      this.stagingArea = new StagingArea();
    }
  }

  /** Create a new .gitlet directory in the current directory.
   * Inside .gitlet, create objects, refs directories and HEAD and logs files
   * */
  public void init() throws IOException {
    makeDirectory();
    createBranchMaster();
    persistHEAD(currentBranch);
    stagingArea.persist();
  }

  private void persistHEAD(Branch currentBranch) {
    Utils.writeContents(HEAD_FILE, Utils.join(REFS_HEADS_DIR, currentBranch.getName()).toString());
  }

  private void makeDirectory() throws IOException {
    GITLET_DIR.mkdir();
    OBJECTS_DIR.mkdir();
    REFS_DIR.mkdir();
    REFS_HEADS_DIR.mkdir();
    LOGS_DIR.mkdir();
    HEAD_FILE.createNewFile();
    STAGING_INDEX.createNewFile();
  }

  public void createBranchMaster() throws IOException {
    // Create and persist the initial initialCommit
    Commit initialCommit = new Commit(INITIAL_COMMIT_MESSAGE, new Date(0));
    initialCommit.persist();

    // Create branch master
    REFS_HEADS_MASTER.createNewFile();
    Branch master = new Branch(MASTER);
    // pointer master to the first initialCommit,
    master.setTipCommit(initialCommit);
    master.persist();
    this.currentBranch = master;

    // Create a file named master in logs to store the log records of changes made to the master branch
    Utils.join(LOGS_DIR, MASTER).createNewFile();
  }

  private Branch getCurrentBranch() {
    String currenBranchPath = Utils.readContentsAsString(HEAD_FILE);
    String currentBranchTipCommitHash = Utils.readContentsAsString(new File(currenBranchPath));

    String[] splitPath = currenBranchPath.split(File.separator);
    String currentBranchName = splitPath[splitPath.length - 1];

    return new Branch(currentBranchName, currentBranchTipCommitHash);
  }

  public boolean gitletExists() {
    return GITLET_DIR.exists();
  }

  public void stage(String fileName) {
    // TODO: If the current working version of the file is identical to the version in the current commit, do not stage it to be added, and remove it from the staging area if it is already there (as can happen when a file is changed, added, and then changed back to it’s original version).
    // TODO: The file will no longer be staged for removal (see gitlet rm), if it was at the time of the command.

    // check if the file is already staged
    if (stagingArea.contains(fileName)) {
      // if the file is identical to the on in staging area, do not stage it to be added
      if (stagingArea.checkIdentical(fileName)) {
        return;
      } else {
        // if the file is already staged, remove it from the staging area and .gitlet/objects
        stagingArea.remove(fileName);
      }
    }

    Blob blob = new Blob(fileName, readFileFromRepositoryAsString(fileName));
    blob.persist();

    stagingArea.add(blob.getFileName(), blob.sha1Hash());
    stagingArea.persist();
  }

  private static String readFileFromRepositoryAsString(String fileName) {
    return Utils.readContentsAsString(Utils.join(CWD, fileName));
  }

  public static boolean isFileExistInRepository(String fileName) {
    return Utils.plainFilenamesIn(CWD).stream().anyMatch(name -> name.equals(fileName));
  }

  public void commit(String message) {
    if (stagingArea.isEmpty()) {
      System.out.println("No changes added to the commit.");
    }

    // Create a new commit object by cloning the head commit. As gitlet don't support detached head mode
    // So, the HEAD also points to the tip commit of current branch.
    Commit newCommit = this.HEAD.getHEADCommit().buildNext(message);
    newCommit.updateIndex(stagingArea);
    newCommit.persist();

    stagingArea.clear();
    currentBranch.setTipCommit(newCommit);

    // Write back to the disk any new objects created and any modified read from the disk
    // TODO: considering extracting this part as method of repository call persistRepository*()
    stagingArea.persist();
    currentBranch.persist();
  }

  public void log() {
    Commit commit = this.HEAD.getHEADCommit();
    while (commit != null) {
      System.out.println("===");
      System.out.println("commit " + commit.sha1Hash());
      System.out.println("Date: " + commit.getTimeStamp());
      System.out.println(commit.getMessage());
      System.out.println();
      commit = commit.getParentCommit();
    }
  }
}