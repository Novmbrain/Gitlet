package gitlet.models;

import gitlet.handllers.*;
import gitlet.utils.RepositoryHelper;
import gitlet.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gitlet.utils.Constants.*;
import static gitlet.utils.RepositoryHelper.*;
import static gitlet.utils.Utils.*;

public class Repository {

    private static StagingArea stagingArea;
    private static Branch currentBranch;

    /**
     * - .gitlet
     *    - objects // a directory to store serialized blobs and commit index by the two first hexadecimal sha1 hash
     *        - file1 whose file name is the rest of the sha1 hash number
     *        - file2
     *        - ....
     *    - HEAD // file that stores the path to the current branch's hash number of the HEAD commit
     *              It points to the file under refs/heads for a branch or to a commit if in 'detached HEAD' state.
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
            stagingArea = StagingArea.load();
            currentBranch = getCurrentBranch();
        } else {
            stagingArea = new StagingArea();
        }
    }

    private static String getCurrentBranchName() {
        return new File(Head.getHeadCommitPath()).getName();
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
        Set<String> committedFile = Head.getHeadCommit().getAllFiles();

        Set<String> untrackedFiles = plainFilenamesIn(CWD).stream().
            filter(fileName -> !stagingArea.stageForAdditionContains(fileName)
                && !committedFile.contains(fileName)
                || stagingArea.stageForRemovalContains(fileName)).
            collect(Collectors.toSet());

        return untrackedFiles;
    }

    private static List<MergeHandler> getMergeHandlers() {
        return List.of(new MergeHandler1(),
            new MergeHandler2(),
            new MergeHandler3(),
            new MergeHandler4(),
            new MergeHandler5(),
            new MergeHandler6(),
            new MergeHandler7());
    }

    private static Set<Commit> traverseCommitTree(Commit commit) {
        Set<Commit> set = new HashSet<>();
        Queue<Commit> queue = new LinkedList<>();
        queue.offer(commit);

        while (!queue.isEmpty()) {
            Commit nextCommit = queue.poll();
            set.add(nextCommit);
            nextCommit.getParents().stream().filter(Objects::nonNull).forEach(queue::offer);
        }

        return set;
    }

    public boolean gitletExists() {
        return GITLET_DIR.exists();
    }

    private Branch getCurrentBranch() {
        String currentBranchTipCommitHash = readContentsAsString(new File(Head.getHeadCommitPath()));
        return new Branch(getCurrentBranchName(), currentBranchTipCommitHash);
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
        writeContents(HEAD_FILE, join(REFS_HEADS_DIR, currentBranch.getName()).toString());
        stagingArea.persist();
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
        Branch master = new Branch(MASTER, initialCommit);
        master.persist();
        currentBranch = master;
    }

    public void add(String fileName) {
        if (!isFileExistInRepository(fileName)) {
            messageAndExit("File does not exist.");
        }

        // if the file is identical to the version in the current commit, do not stage it to be added
        // -- case 1: the file is not in the staging area -> do nothing
        // -- case 2: the file is in the staging area -> remove it from the staging area
        if (Head.getHeadCommit().isFileInRepoEqual(fileName)) {
            if (stagingArea.contains(fileName)) {
                stagingArea.clearStagedBlob(fileName);
            }
        } else {
            Blob blob = new Blob(fileName, readFileFromRepositoryAsString(fileName));
            blob.persist();
            // Staging an already-staged file overwrites the previous entry in the staging area with the new contents.
            stagingArea.stage(blob.getFileName(), blob.sha1Hash());
        }

        stagingArea.persist();
    }

    public void rm(String fileName) {
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

    public void commit(String message) {
        commitCheck(message);
        doCommit(message);
    }

    private void commitCheck(String message) {
        if (stagingArea.isEmpty()) {
            messageAndExit("No changes added to the commit.");
        } else if (message.isEmpty()) {
            messageAndExit("Please enter a commit message.");
        }
    }

    private Commit doCommit(String message) {
        // Create a new commit object by cloning the head commit. As gitlet don't support detached head mode
        // So, the HEAD also points to the tip commit of current branch.
        Commit newCommit = Head.getHeadCommit().buildNext(message);

        newCommit.updateIndex(stagingArea);
        newCommit.persist();

        stagingArea.removeAllMapping();
        currentBranch.setTipCommit(newCommit);

        stagingArea.persist();
        currentBranch.persist();
        return newCommit;
    }

    public void log() {
        Stream.iterate(Head.getHeadCommit(), Objects::nonNull, Commit::getFirstParentCommit).forEach(this::logCommit);
    }

    private void logCommit(Commit commit) {
        StringBuilder output = new StringBuilder();
        Date date = commit.getTimeStamp();
        String formattedDate = String.format("Date: %ta %tb %td %tT %tY %tz", date, date, date, date, date, date);

        output.append("===\n" + "commit " + commit.getSha1Hash() + "\n");

        if (commit.isMergeCommit()) {
            output.append("Merge: "
                + commit.getFirstParentHash().substring(0, 7)
                + " " + commit.getSecondParentHash().substring(0, 7) + "\n");
        }

        output.append(formattedDate + "\n" + commit.getMessage() + "\n");

        System.out.println(output);
    }

    public void status() {
        new StatusPrinter().status();
    }

    /**
     * if the file is in the staging area, overwrite it with the version in the commit and unstage it
     * if the file is not in the staging area, overwrite it with the version in the commit
     *
     * @param fileName
     */
    public void checkoutFile(String fileName) {
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

    public void checkoutFileFromCommit(String commitHash, String fileName) {
        if (!RepositoryHelper.objectExists(commitHash)) {
            messageAndExit("No commit with that id exists.");
        } else {
            Commit commit = RepositoryHelper.getCommit(commitHash);

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

    public void rmBranch(String branchName) {
        if (!branchExists(branchName)) {
            messageAndExit("A branch with that name does not exist.");
        } else if (branchName.equals(currentBranch.getName())) {
            messageAndExit("Cannot remove the current branch.");
        } else {
            join(REFS_HEADS_DIR, branchName).delete();
        }
    }

    //In my implementation, the commit log will be sorted in descending order of the commit time stamp.
    public void globalLog() {
        getAllCommits().stream().sorted(Comparator.comparing(Commit::getTimeStamp).reversed()).forEach(this::logCommit);
    }

    /**
     * Get all the commits in the repository including the initial commit and the commit that is no longer reachable
     *
     * @return
     */
    private List<Commit> getAllCommits() {
        return plainFilenamesIn(LOGS_DIR).stream().map(RepositoryHelper::getCommit).collect(Collectors.toList());
    }

    public void find(String commitMessage) {
        Set<Commit> collect = getAllCommits().stream().
            filter(commit -> commit.getMessage().equals(commitMessage)).collect(Collectors.toSet());

        if (collect.isEmpty()) {
            messageAndExit("Found no commit with that message.");
        } else {
            collect.stream().
                sorted(Comparator.comparing(Commit::getTimeStamp).reversed()).
                forEach(commit -> System.out.println(commit.getSha1Hash()));
        }
    }

    public void merge(String givenBranchName) throws IOException {
        if (!branchExists(givenBranchName)) {
            messageAndExit("A branch with that name does not exist.");
        } else if (givenBranchName.equals(currentBranch.getName())) {
            messageAndExit("Cannot merge a branch with itself.");
        } else if (!stagingArea.isEmpty()) {
            messageAndExit("You have uncommitted changes.");
        } else {
            Commit given = RepositoryHelper.getBranchTipCommit(givenBranchName);
            Commit current = RepositoryHelper.getCommit(currentBranch.getTipHash());
            Commit lastCommonAncestor = findLastCommonAncestor(current, given);

            Set<String> allMergedFiles = new HashSet<>(given.getAllFiles());
            allMergedFiles.addAll(current.getAllFiles());

            allMergedFiles.stream().
                filter(getUntrackedFiles()::contains).
                findFirst().
                ifPresent(file ->
                    messageAndExit("There is an untracked file in the way; delete it, or add and commit it first."));

            if (lastCommonAncestor.equals(given)) {
                messageAndExit("Given branch is an ancestor of the current branch.");
            } else if (lastCommonAncestor.equals(current)) {
                this.checkoutBranch(givenBranchName);
                currentBranch.setTipCommit(given);
                Head.update(given, currentBranch.getName());
                messageAndExit("Current branch fast-forwarded.");
            }

            for (String fileName : allMergedFiles) {
                for (MergeHandler handler : getMergeHandlers()) {
                    boolean handled = handler.handle(fileName, current, given, lastCommonAncestor, this);
                    if (handled) {
                        break;
                    }
                }
            }

            String message = "Merged " + givenBranchName + " into " + currentBranch.getName() + ".";
            Commit mergeCommit = this.doCommit(message);
            mergeCommit.setSecondParentHash(given.getSha1Hash());
            mergeCommit.persist();
        }
    }

    private Commit findLastCommonAncestor(Commit commit1, Commit commit2) {
        Set<Commit> commit1Ancestors = traverseCommitTree(commit1);
        Set<Commit> commit2Ancestors = traverseCommitTree(commit2);

        commit1Ancestors.retainAll(commit2Ancestors);

        return commit1Ancestors.stream().max(Comparator.comparing(Commit::getTimeStamp)).orElse(null);
    }

    public void checkoutBranch(String branchName) {
        if (!branchExists(branchName)) {
            messageAndExit("No such branch exists.");
        } else if (branchName.equals(currentBranch.getName())) {
            messageAndExit("No need to checkout the current branch.");
        } else {

            Commit givenCommit = RepositoryHelper.getBranchTipCommit(branchName);

            givenCommit.getAllFiles().stream().
                filter(getUntrackedFiles()::contains).
                findFirst().ifPresent(file
                    -> messageAndExit("There is an untracked file in the way; delete it, or add and commit it first."));

            Head.getHeadCommit().getAllFiles().forEach(fileName -> Utils.restrictedDelete(join(CWD, fileName)));

            Head.update(givenCommit, branchName);

            stagingArea.clearAllStagedBlobs();
            stagingArea.removeAllMapping();

            // overwrite the files in the working directory with the version in the newBranchTipCommit
            givenCommit.restoreCommit();

            Head.persist();
            stagingArea.persist();
        }
    }

    public void branch(String branchName) throws IOException {
        if (branchExists(branchName)) {
            messageAndExit("A branch with that name already exists.");
        } else {
            join(REFS_HEADS_DIR, branchName).createNewFile();

            Branch branch = new Branch(branchName);
            branch.setTipCommit(Head.getHeadCommit());
            branch.persist();
        }
    }

    public void reset(String commitHash) {
        if (!RepositoryHelper.objectExists(commitHash)) {
            messageAndExit("No commit with that id exists.");
        } else {
            Commit commit = RepositoryHelper.getCommit(commitHash);

            commit.getAllFiles().stream().
                filter(getUntrackedFiles()::contains).
                findFirst().
                ifPresent(file ->
                    messageAndExit("There is an untracked file in the way; delete it, or add and commit it first."));

            Head.getHeadCommit().getAllFiles().forEach(fileName -> join(CWD, fileName).delete());

            commit.restoreCommit();
            currentBranch.setTipCommit(commit);
            Head.update(commit, currentBranch.getName());

            stagingArea.clearAllStagedBlobs();
            stagingArea.removeAllMapping();

            stagingArea.persist();
            Head.persist();
            currentBranch.persist();
        }
    }


    private class StatusPrinter {
        Set<String> stagedForRemovalFiles = stagingArea.getRemovedBlobs();
        Set<String> stagedForAdditionFiles = stagingArea.getStagedBlobs().keySet();
        Set<String> committedFiles = Head.getHeadCommit().getAllFiles();
        List<String> allFilesInCWD = plainFilenamesIn(CWD);

        public void status() {

            String output = generateBranchesSection()
                + generateStagedFilesSection()
                + generateRemovedFilesSection()
                + generateModificationsNotStagedSection()
                + generateUntrackedFilesSection();

            System.out.println(output);
        }

        private String generateBranchesSection() {
            StringBuilder output = new StringBuilder();
            List<String> branches = plainFilenamesIn(REFS_HEADS_DIR);

            output.append("=== Branches ===\n");

            branches.stream().
                filter(branchName -> branchName.equals(currentBranch.getName())).
                forEach(branchName -> output.append("*").append(branchName).append("\n"));

            branches.stream().
                filter(branchName -> !branchName.equals(currentBranch.getName())).
                forEach(branchName -> output.append(branchName).append("\n"));

            output.append("\n");

            return output.toString();
        }

        private String generateStagedFilesSection() {
            StringBuilder output = new StringBuilder();

            output.append("=== Staged Files ===\n");
            stagedForAdditionFiles.forEach(blobName -> output.append(blobName).append("\n"));
            output.append("\n");

            return output.toString();
        }

        private String generateRemovedFilesSection() {
            StringBuilder output = new StringBuilder();

            output.append("=== Removed Files ===\n");
            stagedForRemovalFiles.forEach(blobName -> output.append(blobName).append("\n"));
            output.append("\n");

            return output.toString();
        }

        private String generateModificationsNotStagedSection() {
            StringBuilder output = new StringBuilder();
            Set<String> files = new HashSet<>();

            output.append("=== Modifications Not Staged For Commit ===\n");

            // Staged for addition, but with different contents than in the working directory
            files.addAll(getModifiedStagedFiles());
            // Tracked in the current commit, changed in the working directory, but not staged
            files.addAll(getModifiedUnstagedFiles());
            // Staged for addition, but deleted in the working directory
            files.addAll(getDeletedStagedFiles());

            files.forEach(fileName -> output.append(fileName).append("\n"));
            output.append("\n");

            return output.toString();
        }

        private Set<String> getModifiedStagedFiles() {
            return allFilesInCWD.stream()
                .filter(fileName -> stagedForAdditionFiles.contains(fileName))
                .filter(fileName -> !stagingArea.getStagedBlob(fileName).getFileHash()
                    .equals(Utils.sha1(fileName, readFileFromRepositoryAsString(fileName))))
                .collect(Collectors.toSet());
        }

        private Set<String> getModifiedUnstagedFiles() {
            return committedFiles.stream()
                .filter(fileName -> !stagedForAdditionFiles.contains(fileName) && !stagedForRemovalFiles.contains(fileName))
                .filter(fileName -> !allFilesInCWD.contains(fileName) || !Head.getHeadCommit()
                    .isFileInRepoEqual(fileName))
                .collect(Collectors.toSet());
        }

        private Set<String> getDeletedStagedFiles() {
            return stagedForAdditionFiles.stream()
                .filter(fileName -> !allFilesInCWD.contains(fileName))
                .collect(Collectors.toSet());
        }

        private String generateUntrackedFilesSection() {
            StringBuilder output = new StringBuilder();

            output.append("=== Untracked Files ===\n");
            getUntrackedFiles().forEach(fileName -> output.append(fileName).append("\n"));
            output.append("\n");

            return output.toString();
        }
    }
}