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
    if (repository.gitletExists()) {
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



  public static void commit(String message) {
    if (message.isEmpty()) {
      System.out.println("Please enter a commit message.");
    } else {
      repository.commit(message);
    }
  }

  /**
   * Usage: java gitlet.Main rm [file name]
   * <p>
   * Description: Unstage the file if it is currently staged for addition.
   * If the file is tracked in the current commit, mark it to indicate that it is not to be included in the next commit, and remove the file from the working directory if the user has not already done so (do not remove it unless it is tracked in the current commit).
   * The file will no longer be staged for addition (see gitlet add), if it was at the time of the command.
   * <p>
   * Runtime: Should be constant relative to any significant measure.
   * <p>
   * Failure cases: If the file is neither staged nor tracked by the head commit, print the error message No reason to remove the file.
   * If the file is tracked by the head commit and staged for addition, remove the file from the staging area (but not from the working directory) and print the error message Failed to unstage file.
   * <p>
   * Dangerous?: No
   * <p>
   * Our line count: ~15
   *
   * @param fileName
   */
  public static void status() {

  }

  /**
   * Currently this method is only used to create the initial master branch
   *
   * @param name
   */
  public static void branch(String name) {

  }

}