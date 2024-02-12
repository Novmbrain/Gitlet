package gitlet.utils;

import gitlet.models.Blob;
import gitlet.models.Commit;
import gitlet.models.GitletObject;

import java.io.File;

import static gitlet.utils.Constants.OBJECTS_DIR;

/**
 * @className: ObjectsHelper
 * @description: TODO
 * @author: Wenjie FU
 * @date: 27/01/2024
 **/
public class ObjectsHelper {
  public static Commit getCommit(String commitHash) {
    String index = commitHash.substring(0, 2);
    String rest = commitHash.substring(2);
    File indexDirectory = Utils.join(OBJECTS_DIR, index);

    return Utils.readObject(Utils.join(indexDirectory, rest), Commit.class);
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

  public static boolean objectExists(String hash)
  {
    String index = hash.substring(0, 2);
    String rest = hash.substring(2);
    File indexDirectory = Utils.join(OBJECTS_DIR, index);

    return Utils.join(indexDirectory, rest).exists();
  }
}