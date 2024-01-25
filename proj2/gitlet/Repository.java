package gitlet;

import gitlet.models.Blob;
import gitlet.models.Branch;
import gitlet.models.Commit;
import gitlet.models.StagingArea;
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
    } else {
      this.stagingArea = new StagingArea();
    }
  }

  // TODO : implement a advance filesystem-based storage system
  private static void persistCommit(Commit commit) {
    Utils.writeObject(Utils.join(OBJECTS_DIR, Utils.sha1(commit.sha1Data())), commit);
  }

  // Create a new .gitlet directory in the current directory.
  // Inside .gitlet, create objects, refs directories and HEAD and logs files

  /**
   * this method should be called after initializeRepository
   * A branch is represented by
   * - its log
   * - the reference to its tip
   * <p>
   * -.gitlet
   * -objects
   * -refs
   * -heads
   * -master(f): <hash of the tip commit>
   * -logs
   * -master(f)
   * -HEAD(f) : /Users/...../refs/heads/master
   * -index(f) : staging area
   */
  public void initializeRepository() throws IOException {
    makeDirectory();
    createBranchMaster();
    persistHEAD(currentBranch);
    stagingArea.persist();
  }

  private void persistHEAD(Branch currentBranch) {
    Utils.writeContents(HEAD_FILE, Utils.join(REFS_HEADS_DIR, currentBranch.getName()).toString());
  }

  /**
   * -.gitlet
   * -objects
   * -refs
   * -heads
   * -logs
   * -HEAD(f)
   * -index(f)
   */

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

  private Branch getCurrentBranch() {
    String CurrenBranchPath = Utils.readContentsAsString(HEAD_FILE);
    String CurrentBranchTipCommitHash = Utils.readContentsAsString(new File(CurrenBranchPath));

    String currentBranchName = CurrenBranchPath.split(File.pathSeparator)[CurrenBranchPath.split(File.pathSeparator).length - 1];
    return new Branch(currentBranchName, CurrentBranchTipCommitHash);
  }

  public boolean gitletExists() {
    return GITLET_DIR.exists();
  }

  public void stage(String fileName) {
    Blob blob = new Blob(fileName, Utils.readContentsAsString(Utils.join(CWD, fileName)));

    String blobHash = Utils.sha1(blob);
    Utils.writeObject(Utils.join(OBJECTS_DIR, blobHash), blob);

    stagingArea.add(blob.getFileName(), blobHash );
    stagingArea.persist();
  }

  /* TODO: fill in the rest of this class. */
}