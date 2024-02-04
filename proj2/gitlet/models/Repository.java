package gitlet.models;

import gitlet.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static gitlet.utils.Constants.*;

/**
 * Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 * @author TODO
 */
public class Repository {

  private final StagingArea stagingArea;
  private HEAD head;
  private Branch currentBranch;


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
      this.head = HEAD.getInstance();
    } else {
      this.stagingArea = new StagingArea();
    }
  }

  private static String readFileFromRepositoryAsString(String fileName) {
    return Utils.readContentsAsString(Utils.join(CWD, fileName));
  }

  public static boolean isFileExistInRepository(String fileName) {
    return Utils.plainFilenamesIn(CWD).stream().anyMatch(name -> name.equals(fileName));
  }

  // TODO: add a strict mode to check if the current working directory is a gitlet repository
  public static boolean gitletExists() {
    return GITLET_DIR.exists();
  }

  /**
   * Create a new .gitlet directory in the current directory.
   * Inside .gitlet, create objects, refs directories and HEAD and logs files
   */
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

  public void stage(String fileName) {
    // TODO: The file will no longer be staged for removal (see gitlet rm), if it was at the time of the command.

    // if the file is identical to the version in the current commit, do not stage it to be added
    // -- case 1: the file is not in the staging area -> do nothing
    // -- case 2: the file is in the staging area -> remove it from the staging area
    if (head.getHEADCommit().isFileContentHashMatching(fileName)) {
      if (stagingArea.contains(fileName)) {
        stagingArea.removeFromStagedBlobs(fileName);
      }
    } else {
      Blob blob = new Blob(fileName, readFileFromRepositoryAsString(fileName));
      blob.persist();
      // Staging an already-staged file overwrites the previous entry in the staging area with the new contents.
      stagingArea.stageForAddOrOverwrite(blob.getFileName(), blob.sha1Hash());
    }

    stagingArea.persist();
  }

  public void commit(String message) {
    if (stagingArea.isEmpty()) {
      System.out.println("No changes added to the commit.");
    }

    // Create a new commit object by cloning the head commit. As gitlet don't support detached head mode
    // So, the HEAD also points to the tip commit of current branch.
    Commit newCommit = this.head.getHEADCommit().buildNext(message);
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
    Commit commit = this.head.getHEADCommit();
    while (true) {
      StringBuilder output = new StringBuilder();
      Date date = commit.getTimeStamp();
      String formattedDate = String.format("Date: %ta %tb %td %tT %tY %tz", date, date, date, date, date, date);

      output.append(
        "===\n" +
          "commit " + commit.sha1Hash() + "\n" +
          formattedDate + "\n" +
          commit.getMessage() + "\n");

      System.out.println(output);

      if (commit.isInitialCommit()) {
        break;
      } else {
        commit = commit.getParentCommit();
      }
    }
  }

  public void status() {
    /**
     * tracked files = check staging files + check committed files
     */
    Set<String> committedFile = head.getHEADCommit().getFileNameToBlobHash().keySet();
    List<String> workingDirectoryFiles = Utils.plainFilenamesIn(CWD);
    Set<String> trackedFiles = workingDirectoryFiles.stream()
      .filter(fileName -> stagingArea.contains(fileName) || committedFile.contains(fileName))
      .collect(Collectors.toSet());

    StringBuilder output = new StringBuilder();

    output.append("=== Branches ===\n");
    output.append("*").append(currentBranch.getName()).append("\n");
    // TODO: append other branches
    output.append("\n");

    output.append("=== Staged Files ===\n");
    stagingArea.getStagedBlobs().keySet().forEach(blobName -> output.append(blobName).append("\n"));
    output.append("\n");

    output.append("=== Removed Files ===\n");
    stagingArea.getRemovedBlobs().forEach(blobName -> output.append(blobName).append("\n"));
    output.append("\n");

    output.append("=== Modifications Not Staged For Commit ===\n");
    output.append("\n");

    // TODO:
    /**
     * append files that have been modified in the working directory, but not yet staged
     */

    /**
     * append files that in the most recent commit have been modified in the working directory,
     * but not yet staged
     */

    output.append("=== Untracked Files ===\n");
    // 1. the file in the most recent commits -> already tracked
    // 2. the file in the staging area -> already staged
    //   - staged
    //   - removed
    workingDirectoryFiles.stream()
      .filter(fileName -> !trackedFiles.contains(fileName))
      .forEach(fileName -> output.append(fileName).append("\n"));
    output.append("\n");

    System.out.println(output);
  }

  public void rm(String fileName) {
    if (!head.getHEADCommit().containsFile(fileName) && !stagingArea.contains(fileName)) {
      System.out.println("No reason to remove the file.");
    } else {

      if (stagingArea.contains(fileName)) {
        // Unstage the file if it is currently staged for addition.
        stagingArea.removeFromStagedBlobs(fileName);
      }

      if (head.getHEADCommit().containsFile(fileName)) {
        // If the file is tracked in the current commit, stage it for removal and
        stagingArea.stageForRemoval(fileName);

        // remove the file from the working directory if the user has not already done so
        Utils.restrictedDelete(Utils.join(CWD, fileName));
      }

      stagingArea.persist();
    }
  }
}