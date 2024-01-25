package gitlet.utils;

import java.io.File;

import static gitlet.utils.Utils.join;

/**
 * @className: Constants
 * @description: TODO
 * @author: Wenjie FU
 * @date: 22/01/2024
 **/
public class Constants {
  public static final File CWD = new File(System.getProperty("user.dir"));
  /**
   * The .gitlet directory.
   */
  public static final File GITLET_DIR = join(CWD, ".gitlet");
  public static final File OBJECTS_DIR = join(GITLET_DIR, "objects");
  public static final File REFS_DIR = join(GITLET_DIR, "refs");
  public static final File REFS_HEADS_DIR = join(GITLET_DIR,"refs", "heads");
  public static final File HEAD_FILE = join(GITLET_DIR, "HEAD");
  public static final File LOGS_DIR = join(GITLET_DIR, "logs");
  public static final File STAGING_INDEX = join(GITLET_DIR,"index");


  public static final String INITIAL_COMMIT_MESSAGE = "initial commit";
  public static final String MASTER = "master";
  public static final File REFS_HEADS_MASTER = Utils.join(REFS_HEADS_DIR, MASTER);
}