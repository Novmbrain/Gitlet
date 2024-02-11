package gitlet.models;

import gitlet.utils.ObjectsHelper;
import gitlet.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static gitlet.utils.Constants.*;
import static gitlet.utils.Utils.*;

public class Repository {

  private static StagingArea stagingArea;
  private static Branch currentBranch;

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
      stagingArea = readObject(STAGING_INDEX, StagingArea.class);
      currentBranch = getCurrentBranch();
    } else {
      stagingArea = new StagingArea();
    }
  }

  private static String readFileFromRepositoryAsString(String fileName) {
    return readContentsAsString(join(CWD, fileName));
  }

  public static boolean isFileExistInRepository(String fileName) {
    return plainFilenamesIn(CWD).stream().anyMatch(name -> name.equals(fileName));
  }

  // TODO: add a strict mode to check if the current working directory is a gitlet repository
  public static boolean gitletExists() {
    return GITLET_DIR.exists();
  }

  /**
   * Create a new .gitlet directory in the current directory.
   * Inside .gitlet, create objects, refs directories and HEAD and logs files
   */
  public static void init() throws IOException {
    if (Repository.gitletExists()) {
      messageAndExit("A Gitlet version-control system already exists in the current directory.");
    }

    makeDirectory();
    createBranchMaster();
    persistHEAD();
    stagingArea.persist();
  }

  private static void persistHEAD() {
    writeContents(HEAD_FILE, join(REFS_HEADS_DIR, currentBranch.getName()).toString());
  }

  private static void makeDirectory() throws IOException {
    GITLET_DIR.mkdir();
    OBJECTS_DIR.mkdir();
    REFS_DIR.mkdir();
    REFS_HEADS_DIR.mkdir();
    LOGS_DIR.mkdir();
    HEAD_FILE.createNewFile();
    STAGING_INDEX.createNewFile();
  }

  public static void createBranchMaster() throws IOException {
    // Create and persist the initial initialCommit
    Commit initialCommit = new Commit(INITIAL_COMMIT_MESSAGE, new Date(0));
    initialCommit.persist();

    // Create branch master
    REFS_HEADS_MASTER.createNewFile();
    Branch master = new Branch(MASTER);
    // pointer master to the first initialCommit,
    master.setTipCommit(initialCommit);
    master.persist();
    currentBranch = master;

    // Create a file named master in logs to store the log records of changes made to the master branch
    join(LOGS_DIR, MASTER).createNewFile();
  }

  public static void add(String fileName) {
    if (!isFileExistInRepository(fileName)) {
      messageAndExit("File does not exist.");
    }

    // if the file is identical to the version in the current commit, do not stage it to be added
    // -- case 1: the file is not in the staging area -> do nothing
    // -- case 2: the file is in the staging area -> remove it from the staging area
    if (Head.getHEADCommit().isFileContentHashMatching(fileName)) {
      if (stagingArea.contains(fileName)) {
        stagingArea.clearStagedBlob(fileName);
      }
    } else {
      Blob blob = new Blob(fileName, readFileFromRepositoryAsString(fileName));
      blob.persist();
      // Staging an already-staged file overwrites the previous entry in the staging area with the new contents.
      stagingArea.stageForAddOrOverwrite(blob.getFileName(), blob.sha1Hash());
    }

    stagingArea.persist();
  }

  public static void rm(String fileName) {
    if (!Head.contains(fileName) && !stagingArea.contains(fileName)) {
      messageAndExit("No reason to remove the file.");
    } else {

      if (stagingArea.contains(fileName)) {
        // Unstage the file if it is currently staged for addition.
        stagingArea.clearStagedBlob(fileName);
      }

      if (Head.contains(fileName)) {
        // If the file is tracked in the current commit, stage it for removal and
        stagingArea.stageForRemoval(fileName);

        // remove the file from the working directory if the user has not already done so
        restrictedDelete(join(CWD, fileName));
      }

      stagingArea.persist();
    }
  }

  public static void commit(String message) {
    if (stagingArea.isEmpty()) {
      messageAndExit("No changes added to the commit.");
    } else if (message.isEmpty()) {
      messageAndExit("Please enter a commit message.");
    } else {
      // Create a new commit object by cloning the head commit. As gitlet don't support detached head mode
      // So, the HEAD also points to the tip commit of current branch.
      Commit newCommit = Head.getHEADCommit().buildNext(message);
      newCommit.updateIndex(stagingArea);
      newCommit.persist();

      stagingArea.removeAllMapping();
      currentBranch.setTipCommit(newCommit);

      // Write back to the disk any new objects created and any modified read from the disk
      // TODO: considering extracting this part as method of repository call persistRepository*()
      stagingArea.persist();
      currentBranch.persist();
    }
  }

  public static void log() {
    Commit commit = Head.getHEADCommit();
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

  public static void status() {
    /**
     * tracked files = check staging files + check committed files
     */
    StringBuilder output = new StringBuilder();

    output.append("=== Branches ===\n");
    List<String> strings = plainFilenamesIn(REFS_HEADS_DIR);
    strings.forEach(branchName -> {
      if (branchName.equals(currentBranch.getName())) {
        output.append("*").append(branchName).append("\n");
      } else {
        output.append(branchName).append("\n");
      }
    });
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
    getUntrackedFiles().forEach(fileName -> output.append(fileName).append("\n"));
    output.append("\n");

    System.out.println(output);
  }

  /**
   * Returns a list of the names of all plain files in the directory DIR, in
   * lexicographic order as Java Strings.  Returns null if DIR does
   * not denote a directory.
   * <p>
   * Untracked files are files in the working directory that are
   * - neither staged for addition
   * - nor tracked by the head commit.
   */
  private static Set<String> getUntrackedFiles() {
    List<String> workingDirectoryFiles = plainFilenamesIn(CWD);
    Set<String> committedFile = Head.getHEADCommit().getFileNameToBlobHash().keySet();
    Set<String> untrackedFiles = workingDirectoryFiles.stream()
      .filter(fileName -> !stagingArea.contains(fileName) && !committedFile.contains(fileName))
      .collect(Collectors.toSet());
    return untrackedFiles;
  }

  /**
   * if the file is in the staging area, overwrite it with the version in the commit and unstage it
   * if the file is not in the staging area, overwrite it with the version in the commit
   *
   * @param fileName
   */
  public static void checkoutFile(String fileName) {
    if (!Head.contains(fileName)) {
      messageAndExit("File does not exist in that commit.");
    } else {
      Blob blob = Head.getBlob(fileName);
      writeContents(join(CWD, fileName), blob.getContent());

      if (stagingArea.contains(fileName)) {
        stagingArea.removeFromMapping(fileName);
        stagingArea.persist();
      }
    }
  }

  public static void checkoutFileFromCommit(String commitHash, String fileName) {
    if (!ObjectsHelper.objectExists(commitHash)) {
      messageAndExit("No commit with that id exists.");
    } else {
      Commit commit = ObjectsHelper.getCommit(commitHash);

      if (!commit.containsFile(fileName)) {
        messageAndExit("File does not exist in that commit.");
      } else {
        Blob blob = commit.getBlob(fileName);
        writeContents(join(CWD, fileName), blob.getContent());

        if (stagingArea.contains(fileName)) {
          stagingArea.removeFromMapping(fileName);
          stagingArea.persist();
        }
      }
    }
  }

  public static void checkoutBranch(String branchName) {
    if (!Branch.branchExists(branchName)) {
      messageAndExit("No such branch exists.");
    } else if (branchName.equals(currentBranch.getName())) {
      messageAndExit("No need to checkout the current branch.");
    } else {

      Commit newBranchTipCommit = ObjectsHelper.getCommit(readContentsAsString(join(REFS_HEADS_DIR, branchName)));

      newBranchTipCommit.getFileNameToBlobHash().keySet().forEach(fileName -> {
        if (getUntrackedFiles().contains(fileName)) {
          messageAndExit("There is an untracked file in the way; delete it, or add and commit it first.");
        }
      });

      Head.getHEADCommit().getFileNameToBlobHash().keySet().forEach(fileName ->
        Utils.restrictedDelete(join(CWD, fileName))
      );

      Head.update(newBranchTipCommit, branchName);

      stagingArea.clearAllStagedBlob();
      stagingArea.removeAllMapping();

      // overwrite the files in the working directory with the version in the newBranchTipCommit
      newBranchTipCommit.getFileNameToBlobHash().keySet().forEach(fileName -> {
        Blob blob = newBranchTipCommit.getBlob(fileName);
        writeContents(join(CWD, fileName), blob.getContent());
      });

      //Head.persist();
      currentBranch = getCurrentBranch();
      persistHEAD();
      stagingArea.persist();
    }

  }


  private static Branch getCurrentBranch() {
    String currentBranchTipCommitHash = readContentsAsString(new File(Head.getHEADCommitPath()));

    String[] splitPath = Head.getHEADCommitPath().split(FileSystems.getDefault().getSeparator());
    String currentBranchName = splitPath[splitPath.length - 1];

    return new Branch(currentBranchName, currentBranchTipCommitHash);
  }

  public void branch(String branchName) throws IOException {
    if (Branch.branchExists(branchName)) {
      messageAndExit("A branch with that name already exists.");
    } else {

      join(REFS_HEADS_DIR, branchName).createNewFile();

      Branch branch = new Branch(branchName);
      branch.setTipCommit(Head.getHEADCommit());
      branch.persist();
    }
  }

  // TODO: checkout branch


}