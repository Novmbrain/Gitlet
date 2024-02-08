package gitlet.utils;

import gitlet.models.Blob;
import gitlet.models.Commit;
import gitlet.models.GitletObject;

import static gitlet.utils.Constants.OBJECTS_DIR;

/**
 * @className: ObjectsHelper
 * @description: TODO
 * @author: Wenjie FU
 * @date: 27/01/2024
 **/
public class ObjectsHelper {
  public static Commit getCommit(String commitHash) {
    return Utils.readObject(Utils.join(OBJECTS_DIR, commitHash), Commit.class);
  }

  public static Blob getBlob(String blobHash) {
    return Utils.readObject(Utils.join(OBJECTS_DIR, blobHash), Blob.class);
  }

  public static void persistObject(String hash, GitletObject object) {
    Utils.writeObject(Utils.join(OBJECTS_DIR, hash), object);
  }

  public static boolean objectExists(String hash) {
    return Utils.join(OBJECTS_DIR, hash).exists();
  }
}