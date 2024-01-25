package gitlet;

import gitlet.models.Branch;
import gitlet.models.Commit;
import gitlet.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import static gitlet.helpers.HEADHelper.persistHEAD;
import static gitlet.utils.Constants.*;

// TODO: any imports you need here

/**
 * Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author TODO
 */
public class Repository {
  /**
   * TODO: add instance variables here.
   * <p>
   * List all instance variables of the Repository class here with a useful
   * comment above them describing what that variable represents and how that
   * variable is used. We've provided two examples for you.
   */

  private Branch currentBranch;

  // HEAD contains the path to the current branch's hash number of the HEAD commit. Currently, as HEAD-detached mode is not supported
  // HEAD points to the tip commit of the current branch
  // example: refs/heads/master

  // private String HEAD = PointerHelper.loadHEAD();
  // private Branch currentBranch = PointerHelper.loadCurrentBranch();

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

  }

  /**
   * this method should be called after initializeRepository
   * A branch is represented by
   *  - its log
   *  - the reference to its tip
   *
   * -.gitlet
   *    -objects
   *    -refs
   *        -heads
   *           -master(f): <hash of the tip commit>
   *    -logs
   *        -master(f)
   *    -HEAD(f) : /Users/...../refs/heads/master
   */
  public void initializeRepository() throws IOException {
    makeDirectory();
    createBranchMaster();

    // Update Repository Status
    persistRepositoryStatus();
  }

  private void persistRepositoryStatus() {
    // Pointer HEAD to the tip commit of the branch
    persistHEAD(currentBranch);
  }

  /**
   * Load the repository status from the .gitlet directory
   */
  private void loadRepository() {

    //
  }

  /**
   * @return
   */
  private Branch getCurrentBranch() {
    String CurrenBranchPath = Utils.readContentsAsString(HEAD_FILE);
    String CurrentBranchTipCommitHash = Utils.readContentsAsString(new File(CurrenBranchPath));

    String currentBranchName = CurrenBranchPath.split(File.pathSeparator)[CurrenBranchPath.split(File.pathSeparator).length - 1];
    return new Branch(currentBranchName, CurrentBranchTipCommitHash);
  }

  public boolean gitletExists() {
    return GITLET_DIR.exists();
  }

  // Create a new .gitlet directory in the current directory.
  // Inside .gitlet, create objects, refs directories and HEAD and logs files
  /**
   * -.gitlet
   *    -objects
   *    -refs
   *        -heads
   *    -logs
   *    -HEAD(f)
   */

  private void makeDirectory() throws IOException {
    GITLET_DIR.mkdir();
    OBJECTS_DIR.mkdir();
    REFS_DIR.mkdir();
    REFS_HEADS_DIR.mkdir();
    LOGS_DIR.mkdir();
    HEAD_FILE.createNewFile();
  }

  public void createBranchMaster() throws IOException {
    // Create and persist the initial initialCommit
    Commit initialCommit = new Commit(INITIAL_COMMIT_MESSAGE, new Date(0));
    persistCommit(initialCommit);

    // Create branch master
    REFS_HEADS_MASTER.createNewFile();
    Branch master = new Branch(MASTER);
    // pointer master to the first initialCommit,
    master.setTipCommit(initialCommit);
    this.currentBranch = master;

    // Create a file named master in logs to store the log records of changes made to the master branch
    Utils.join(LOGS_DIR, MASTER).createNewFile();
  }

  // TODO : implement a advance filesystem-based storage system
  private static void persistCommit(Commit commit) {
    Utils.writeObject(Utils.join(OBJECTS_DIR, Utils.sha1(commit.sha1Data())), commit);
  }

  public void createABranch(String name) {
    // Check if the branch need to be created has already existed


  }

  public void createACommit(String message) {

  }


  /* TODO: fill in the rest of this class. */
}