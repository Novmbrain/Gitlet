package gitlet.models;


import gitlet.utils.RepositoryHelper;
import gitlet.utils.Utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gitlet.utils.Constants.CWD;
import static gitlet.utils.Constants.LOGS_DIR;
import static gitlet.utils.Utils.join;
import static gitlet.utils.Utils.writeContents;

public class Commit extends GitletObject {

    /**
     * The message of this Commit.
     */
    private final String message;
    /**
     * Time at which a commit is created. Assigned by the constructor
     */
    private final Date timeStamp;
    private String sha1Hash = "";
    /**
     * A map that links file name to its blob
     */
    private HashMap<String, String> fileNameToBlobHash;
    /**
     * * The parent commit of a commit object
     */
    private String firstParentHash = "";
    private String secondParentHash = "";

    public Commit(String message, Date timeStamp) {
        this.message = message;
        this.timeStamp = timeStamp;
        this.fileNameToBlobHash = new HashMap<>();
    }

    public String getSha1Hash() {
        return sha1Hash;
    }

    public String getFirstParentHash() {
        return firstParentHash;
    }

    public String getMessage() {
        return message;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public HashMap<String, String> getFileNameToBlobHash() {
        return fileNameToBlobHash;
    }

    public Commit buildNext(String commitMessage) {
        Commit commit = new Commit(commitMessage, new Date());
        commit.fileNameToBlobHash = fileNameToBlobHash;
        commit.firstParentHash = this.sha1Hash;
        return commit;
    }

    public void persist() {
        sha1Hash = this.sha1Hash();
        RepositoryHelper.persistObject(sha1Hash, this);
        // write the commit hash to the logs directory, concatenate the commit hash to the end of the file
        writeContents(join(LOGS_DIR, sha1Hash), sha1Hash);
    }

    @Override
    protected String sha1Hash() {
        return Utils.sha1(message, timeStamp.toString(), Utils.serialize(fileNameToBlobHash), firstParentHash);
    }

    public void updateIndex(StagingArea stagingArea) {
        stagingArea.getStagedBlobs()
            .forEach((fileName, blobHash) -> {
                if (fileNameToBlobHash.containsKey(fileName)) {
                    fileNameToBlobHash.replace(fileName, blobHash);
                } else {
                    fileNameToBlobHash.put(fileName, blobHash);
                }
            });

        stagingArea.getRemovedBlobs()
            .forEach(fileNameToBlobHash::remove);
    }

    public Set<Commit> getParents() {
        return Stream.of(getFirstParentCommit(), getSecondParentCommit())
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
    }

    public Commit getFirstParentCommit() {
        return isInitialCommit() ? null : RepositoryHelper.getCommit(firstParentHash);
    }

    private Commit getSecondParentCommit() {
        return secondParentHash.isEmpty() ? null : RepositoryHelper.getCommit(secondParentHash);
    }

    public boolean isInitialCommit() {
        return firstParentHash.isEmpty();
    }

    public boolean containsFile(String fileName) {
        return fileNameToBlobHash.containsKey(fileName);
    }

    public boolean isFileInRepoEqual(String fileName) {
        if (!fileNameToBlobHash.containsKey(fileName)) {
            return false;
        }

        String blobHash = fileNameToBlobHash.get(fileName);
        Blob blob = RepositoryHelper.getBlob(blobHash);
        String hash = Utils.sha1(fileName, Utils.readContentsAsString(Utils.join(CWD, fileName)));
        return blob.getFileHash().equals(hash);
    }

    public boolean isFileEqual(String fileName, String fileHash) {
        if (!fileNameToBlobHash.containsKey(fileName)) {
            return false;
        }

        String blobHash = fileNameToBlobHash.get(fileName);
        Blob blob = RepositoryHelper.getBlob(blobHash);
        return blob.getFileHash().equals(fileHash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, timeStamp, fileNameToBlobHash, firstParentHash);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Commit commit = (Commit) o;

        return
            Objects.equals(message, commit.message)
                && Objects.equals(timeStamp, commit.timeStamp)
                && Objects.equals(fileNameToBlobHash, commit.fileNameToBlobHash)
                && Objects.equals(firstParentHash, commit.firstParentHash);
    }

    public boolean isMergeCommit() {
        return !secondParentHash.isEmpty();
    }

    public String getSecondParentHash() {
        return secondParentHash;
    }

    public void setSecondParentHash(String secondParentHash) {
        this.secondParentHash = secondParentHash;
    }

    public void restoreCommit() {
        this.getAllFiles()
            .forEach(fileName -> {
                Blob blob = this.getBlob(fileName);
                writeContents(join(CWD, fileName), blob.getContent());
            });
    }

    public Set<String> getAllFiles() {
        return this.fileNameToBlobHash.keySet();
    }

    public Blob getBlob(String fileName) {
        String blobHash = fileNameToBlobHash.get(fileName);
        return blobHash == null ? null : RepositoryHelper.getBlob(blobHash);
    }
}