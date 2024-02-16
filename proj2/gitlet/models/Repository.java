package gitlet.models;

import gitlet.utils.ObjectsHelper;
import gitlet.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
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

  private String readFileFromRepositoryAsString(String fileName) {
    return readContentsAsString(join(CWD, fileName));
  }

  public  boolean isFileExistInRepository(String fileName) {
    return plainFilenamesIn(CWD).stream().anyMatch(name -> name.equals(fileName));
  }

  // TODO: add a strict mode to check if the current working directory is a gitlet repository
  public  boolean gitletExists() {
    return GITLET_DIR.exists();
  }

  /**
   * Create a new .gitlet directory in the current directory.
   * Inside .gitlet, create objects, refs directories and HEAD and logs files
   */
  public void init() throws IOException {
    if (this.gitletExists()) {
      messageAndExit("A Gitlet version-control system already exists in the current directory.");
    }

    makeDirectory();
    createBranchMaster();
    persistHEAD();
    stagingArea.persist();
  }

  private void persistHEAD() {
    writeContents(HEAD_FILE, join(REFS_HEADS_DIR, currentBranch.getName()).toString());
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

  public  void createBranchMaster() throws IOException {
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

  public  void add(String fileName) {
    if (!isFileExistInRepository(fileName)) {
      messageAndExit("File does not exist.");
    }

    // if the file is identical to the version in the current commit, do not stage it to be added
    // -- case 1: the file is not in the staging area -> do nothing
    // -- case 2: the file is in the staging area -> remove it from the staging area
    if (Head.getHEADCommit().isFileHashMatching(fileName)) {
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

  public  void rm(String fileName) {
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

  public  void commit(String message) {
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

  public  void log() {
    Commit commit = Head.getHEADCommit();

    while (true) {
      logCommit(commit);

      if (commit.isInitialCommit()) {
        break;
      } else {
        commit = commit.getParentCommit();
      }
    }
  }

  public  void status() {
    /**
     * tracked files = check staging files + check committed files
     */
    StringBuilder output = new StringBuilder();

    List<String> branches = plainFilenamesIn(REFS_HEADS_DIR);
    Set<String> stagedForAdditionFiles = stagingArea.getStagedBlobs().keySet();
    Set<String> stagedForRemovalFiles = stagingArea.getRemovedBlobs();

    output.append("=== Branches ===\n");
    branches.forEach(branchName -> {
      if (branchName.equals(currentBranch.getName())) {
        output.append("*").append(branchName).append("\n");
      } else {
        output.append(branchName).append("\n");
      }
    });
    output.append("\n");

    output.append("=== Staged Files ===\n");
    stagedForAdditionFiles.forEach(blobName -> output.append(blobName).append("\n"));
    output.append("\n");

    output.append("=== Removed Files ===\n");
    stagedForRemovalFiles.forEach(blobName -> output.append(blobName).append("\n"));
    output.append("\n");

    output.append("=== Modifications Not Staged For Commit ===\n");
    output.append("\n");
    // TODO:
    // 1. Staged for addition, but with different contents than in the working directory;

    // 2. Staged for addition, but deleted in the working directory;

    // 3. Not staged for addition, but with different contents than in the working directory;

    // 4. Tracked in the working directory, but not in the staging area;


    output.append("=== Untracked Files ===\n");
    // The final category (“Untracked Files”) is for files present in the working directory but neither staged for addition nor tracked.
    // This includes files that have been staged for removal, but then re-created without Gitlet’s knowledge.
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
  private  Set<String> getUntrackedFiles() {
    Set<String> stagedForRemovalFiles = stagingArea.getRemovedBlobs();
    List<String> workingDirectoryFiles = plainFilenamesIn(CWD);

    Set<String> committedFile = Head.getHEADCommit().getFileNameToBlobHash().keySet();
    Set<String> collect1 = workingDirectoryFiles.stream()
      .filter(fileName -> !stagingArea.containsStageFroAddition(fileName) && !committedFile.contains(fileName))
      .collect(Collectors.toSet());
    Set<String> collect2 = workingDirectoryFiles.stream().filter(stagedForRemovalFiles::contains).collect(Collectors.toSet());
    collect1.addAll(collect2);

    return collect1;
  }

  /**
   * if the file is in the staging area, overwrite it with the version in the commit and unstage it
   * if the file is not in the staging area, overwrite it with the version in the commit
   *
   * @param fileName
   */
  public  void checkoutFile(String fileName) {
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

  public  void checkoutFileFromCommit(String commitHash, String fileName) {
    if (!ObjectsHelper.objectExists(commitHash)) {
      messageAndExit("No commit" +
        " with that id exists.");
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

  public  void checkoutBranch(String branchName) {
    if (!Branch.branchExists(branchName)) {
      messageAndExit("No such branch exists.");
    } else if (branchName.equals(currentBranch.getName())) {
      messageAndExit("No need to checkout the current branch.");
    } else {

      Commit newBranchTipCommit = ObjectsHelper.getBranchTipCommit(branchName);

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

      currentBranch = getCurrentBranch();
      persistHEAD();
      stagingArea.persist();
    }
  }


  private  Branch getCurrentBranch() {
    String currentBranchTipCommitHash = readContentsAsString(new File(Head.getHEADCommitPath()));

    String[] splitPath = Head.getHEADCommitPath().split(System.getProperty("file.separator"));
    String currentBranchName = splitPath[splitPath.length - 1];

    return new Branch(currentBranchName, currentBranchTipCommitHash);
  }

  public  void rmBranch(String branchName) {
    if (!Branch.branchExists(branchName)) {
      messageAndExit("A branch with that name does not exist.");
    } else if (branchName.equals(currentBranch.getName())) {
      messageAndExit("Cannot remove the current branch.");
    } else {
      join(REFS_HEADS_DIR, branchName).delete();
    }
  }


  // TODO Consider applying Decision tree pruning
  //In my implementation, the commit log will be sorted in descending order of the commit time stamp.
  public  void globalLog() {
    Set<Commit> allCommit = getAllCommits();

    allCommit.stream()
      .sorted((c1, c2) -> c2.getTimeStamp().compareTo(c1.getTimeStamp()))
      .forEach(this::logCommit);
  }

  private  void logCommit(Commit commit) {
    StringBuilder output = new StringBuilder();
    Date date = commit.getTimeStamp();
    String formattedDate = String.format("Date: %ta %tb %td %tT %tY %tz", date, date, date, date, date, date);

    output.append(
      "===\n" +
        "commit " + commit.sha1Hash() + "\n" +
        formattedDate + "\n" +
        commit.getMessage() + "\n");

    System.out.println(output);
  }

  public  void find(String commitMessage) {
    Set<Commit> allCommit = getAllCommits();

    if (allCommit.stream().noneMatch(commit -> commit.getMessage().equals(commitMessage))) {
      messageAndExit("Found no commit with that message.");
    }

    allCommit.stream()
      .filter(commit -> commit.getMessage().equals(commitMessage))
      .sorted((c1, c2) -> c2.getTimeStamp().compareTo(c1.getTimeStamp()))
      .forEach(commit -> System.out.println(commit.sha1Hash()));
  }

  private  Set<Commit> getAllCommits() {
    Set<Commit> allTipCommits = plainFilenamesIn(REFS_HEADS_DIR)
      .stream()
      .map(branchName ->
        ObjectsHelper.getCommit(readContentsAsString(join(REFS_HEADS_DIR, branchName))))
      .collect(Collectors.toSet());

    Set<Commit> allCommit = new HashSet<>();

    for (Commit tipCommit : allTipCommits) {
      while (true) {

        allCommit.add(tipCommit);

        if (tipCommit.isInitialCommit()) {
          break;
        } else {
          tipCommit = tipCommit.getParentCommit();
        }
      }
    }
    return allCommit;
  }

  public  void merge(String givenBranchName) {
    if (!Branch.branchExists(givenBranchName)) {
      messageAndExit("A branch with that name does not exist.");
    } else if (givenBranchName.equals(currentBranch.getName())) {
      messageAndExit("Cannot merge a branch with itself.");
    } else {
      // TODO: Consider to put the tip commit as a field of the Class Branch
      Commit givenBranchTipCommit = ObjectsHelper.getBranchTipCommit(givenBranchName);
      Commit currentBranchTipCommit = ObjectsHelper.getCommit(currentBranch.getTipHash());


    }
  }

  private  Commit findLastCommonAncestor(Commit commit1, Commit commit2) {
    Set<Commit> commit1Ancestors = new HashSet<>();
    Set<Commit> commit2Ancestors = new HashSet<>();

    while (commit1 != null) {
      commit1Ancestors.add(commit1);
      commit1 = commit1.getParentCommit();
    }

    while (commit2 != null) {
      commit2Ancestors.add(commit2);
      commit2 = commit2.getParentCommit();
    }

    commit1Ancestors.retainAll(commit2Ancestors);

    return commit1Ancestors.stream().max((c1, c2) -> c2.getTimeStamp().compareTo(c1.getTimeStamp())).orElse(null);
  }


  public  void branch(String branchName) throws IOException {
    if (Branch.branchExists(branchName)) {
      messageAndExit("A branch with that name already exists.");
    } else {

      join(REFS_HEADS_DIR, branchName).createNewFile();

      Branch branch = new Branch(branchName);
      branch.setTipCommit(Head.getHEADCommit());
      branch.persist();
    }
  }

}