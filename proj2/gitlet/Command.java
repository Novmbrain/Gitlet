package gitlet;

import java.io.IOException;

import static gitlet.Repository.isFileExistInRepository;

/**
 * @className: Service
 * @description: TODO
 * @author: Wenjie FU
 * @date: 22/01/2024
 **/

public class Command {

  private static final Repository repository = new Repository();

  /**
   * Usage: java gitlet.Main init
   * <p>
   * Description: Creates a new Gitlet version-control system in the current directory.
   * This system will automatically start with one commit: a commit that contains no files and has the commit message initial commit (just like that, with no punctuation).
   * It will have a single branch: master, which initially points to this initial commit, and master will be the current branch.
   * The timestamp for this initial commit will be 00:00:00 UTC, Thursday, 1 January 1970 in whatever format you choose for dates (this is called “The (Unix) Epoch”, represented internally by the time 0.)
   * Since the initial commit in all repositories created by Gitlet will have exactly the same content,
   * it follows that all repositories will automatically share this commit (they will all have the same UID) and all commits in all repositories will trace back to it.
   * <p>
   * Runtime: Should be constant relative to any significant measure.
   * <p>
   * Failure cases: If there is already a Gitlet version-control system in the current directory, it should abort.
   * It should NOT overwrite the existing system with a new one. Should print the error message A Gitlet version-control system already exists in the current directory.
   * <p>
   * Dangerous?: No
   * <p>
   * Our line count: ~15
   */
  public static void init() {
    if (Repository.gitletExists()) {
      System.out.println("A Gitlet version-control system already exists in the current directory.");
    } else {
      try {
        // Read from the disk the head commit and the staging area status

        repository.init();
      } catch (IOException e) {
        System.out.println("Error when creating the .gitlet directory");
      }
    }
  }

  /**
   * Usage: java gitlet.Main add [file name]
   * <p>
   * Description: Adds a copy of the file as it currently exists to the staging area (see the description of the commit command).
   * For this reason, adding a file is also called staging the file for addition.
   * Staging an already-staged file overwrites the previous entry in the staging area with the new contents.
   * The staging area should be somewhere in .gitlet. If the current working version of the file is identical to the version in the current commit,
   * do not stage it to be added, and remove it from the staging area if it is already there (as can happen when a file is changed, added, and then changed back).
   * The file will no longer be staged for removal (see gitlet rm), if it was at the time of the command.
   * <p>
   * Runtime: Should be constant relative to any significant measure.
   * <p>
   * Failure cases: If the file does not exist, print the error message File does not exist.
   * <p>
   * Dangerous?: No
   * <p>
   * Our line count: ~15
   *
   * @param fileName
   */
  public static void add(String fileName) {
    if (!isFileExistInRepository(fileName)) {
      System.out.println("File does not exist.");
    } else {
      repository.stage(fileName);
    }
  }


  /*
    * Usage: java gitlet.Main commit [message]
    * <p>
    * Description: Saves a snapshot of certain files in the current commit and staging area so they can be restored at a later time, creating a new commit.
    * The commit is said to be tracking the saved files. By default, each commit’s snapshot of files will be exactly the same as its parent commit’s snapshot of files;
    * it will keep versions of files exactly as they are, and not update them. A commit will only update the contents of files it is tracking that have been staged for addition at the time of commit,
    * in which case the commit will now include the version of the file that was staged instead of the version it got from its parent.
    * A commit will save and start tracking any files that were staged for addition but weren’t tracked by its parent. Finally, files tracked in the current commit may be untracked in the new commit as a result being staged for removal by the rm command (below).
    * <p>
    * Runtime: Should be linear with respect to the total size of the files being committed.
    * <p>
    * Failure cases: If no files have been staged, aborts. Otherwise, it creates a new commit with the files in the staging area as tracked files.
    * The commit’s parent is the current head commit. The commit timestamp is the current time.
    * The commit message is the input message. The commit will be saved in the .gitlet/commits directory.
    * The current branch head will be updated to point to this new commit, and the staging area will be cleared.
    * <p>
    * Dangerous?: No
    * <p>
    * Our line count: ~15
    * @param message
   */
  public static void commit(String message) {
    if (message.isEmpty()) {
      System.out.println("Please enter a commit message.");
    } else {
      repository.commit(message);
    }
  }


  public static void log() {
    repository.log();
  }

  public static void status() {
    repository.status();
  }

  /**
   * Currently this method is only used to create the initial master branch
   *
   * @param name
   */
  public static void branch(String name) {

  }

}