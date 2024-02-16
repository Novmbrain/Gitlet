package gitlet.utils;

import gitlet.models.Blob;
import gitlet.models.Commit;
import gitlet.models.GitletObject;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static gitlet.utils.Constants.OBJECTS_DIR;
import static gitlet.utils.Constants.REFS_HEADS_DIR;
import static gitlet.utils.Utils.*;

/**
 * @className: ObjectsHelper
 * @description: TODO
 * @author: Wenjie FU
 * @date: 27/01/2024
 **/
public class ObjectsHelper {
  public static Commit getCommit(String commitHash) {
    if (commitHash.length() < 2) {
      messageAndExit("The commit hash is too short.");
    }

    String index = commitHash.substring(0, 2);
    String rest = commitHash.substring(2);

    File indexDirectory = Utils.join(OBJECTS_DIR, index);
    // List all files in the directory matching the abbreviated ID
    List<String> matchingFiles = Utils.plainFilenamesIn(indexDirectory).stream().filter(name -> name.startsWith(rest)).collect(Collectors.toList());

    if (matchingFiles.isEmpty()) {
      messageAndExit("No commit with that id exists.");
    } else if (matchingFiles.size() > 1) {
      messageAndExit("Multiple commits with that id exist.");
    }

    return Utils.readObject(Utils.join(indexDirectory, matchingFiles.get(0)), Commit.class);
  }

  public static Blob getBlob(String blobHash) {
    String index = blobHash.substring(0, 2);
    String rest = blobHash.substring(2);
    File indexDirectory = Utils.join(OBJECTS_DIR, index);

    return Utils.readObject(Utils.join(indexDirectory, rest), Blob.class);
  }

  public static void persistObject(String hash, GitletObject object) {
    String index = hash.substring(0, 2);
    String rest = hash.substring(2);
    File indexDirectory = Utils.join(OBJECTS_DIR, index);
    indexDirectory.mkdirs();

    Utils.writeObject(Utils.join(indexDirectory, rest), object);
  }

  public static boolean objectExists(String hash) {
    String index = hash.substring(0, 2);
    String rest = hash.substring(2);
    File indexDirectory = Utils.join(OBJECTS_DIR, index);

    List<String> matchingFiles = Utils.plainFilenamesIn(indexDirectory).stream().filter(name -> name.startsWith(rest)).collect(Collectors.toList());

    if (matchingFiles.isEmpty()) {
      messageAndExit("No commit with that id exists.");
    } else if (matchingFiles.size() > 1) {
      messageAndExit("Multiple commits with that id exist.");
    }
    return matchingFiles.size() == 1;
  }


  public static Commit getBranchTipCommit(String branchName) {
    return ObjectsHelper.getCommit(readContentsAsString(join(REFS_HEADS_DIR, branchName)));
  }
}