package gitlet.handllers;

import gitlet.models.Blob;
import gitlet.models.Commit;
import gitlet.models.Repository;
import gitlet.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static gitlet.utils.Constants.CWD;
import static gitlet.utils.Utils.writeContents;

/**
 * @className: Handler7
 * @description: Handle the case where the file is modified in other and HEAD
 * - in the same way →do nothing
 * - in diff ways → conflict
 * @author: Wenjie FU
 * @date: 15/02/2024
 **/
public class MergeHandler7 implements MergeHandler {
    @Override
    public boolean handle(String fileName,
                          Commit headCommit,
                          Commit givenCommit,
                          Commit splitPointCommit,
                          Repository repository) throws IOException {
        boolean handled = false;

        if (!headCommit.containsFile(fileName) && !givenCommit.containsFile(fileName) && splitPointCommit.containsFile(fileName)) {
            handled = true;
        } else if (!headCommit.containsFile(fileName)) {
            Blob blob = givenCommit.getBlob(fileName);
            String givenFileHash = blob.getFileHash();

            if (!splitPointCommit.isFileEqual(fileName, givenFileHash)) {
                handleConflict(fileName, headCommit, givenCommit, repository);
            }
        } else if (!givenCommit.containsFile(fileName)) {
            Blob blob = headCommit.getBlob(fileName);
            String headFileHash = blob.getFileHash();

            if (!splitPointCommit.isFileEqual(fileName, headFileHash)) {
                handleConflict(fileName, headCommit, givenCommit, repository);
            }
        } else if (headCommit.containsFile(fileName) && givenCommit.containsFile(fileName)) {

            Blob blob = splitPointCommit.getBlob(fileName);
            String splitFileHash = blob.getFileHash();

            if (headCommit.isFileEqual(fileName, givenCommit.getBlob(fileName).getFileHash())) {
                // Do nothing
                handled = true;
            } else if (!headCommit.isFileEqual(fileName, splitFileHash) && !givenCommit.isFileEqual(fileName, splitFileHash)) {
                // conflict handling
                handleConflict(fileName, headCommit, givenCommit, repository);
                handled = true;
            }
        }
        return handled;
    }

    private void handleConflict(String fileName, Commit headCommit, Commit givenCommit, Repository repository) throws IOException {
        //String headFileContent = "";
        //String givenFileContent = "";
        //
        //if (headCommit.getBlob(fileName) != null) {
        //    headFileContent = headCommit.getBlob(fileName).getContent();
        //}
        //
        //if (givenCommit.getBlob(fileName) != null) {
        //    givenFileContent = givenCommit.getBlob(fileName).getContent();
        //}
        //
        //String conflictContent = "<<<<<<< HEAD\n";
        //
        //if (headFileContent.isEmpty()) {
        //    conflictContent += "=======\n";
        //} else {
        //    conflictContent += headFileContent + "\n=======\n";
        //}
        //
        //if (givenFileContent.isEmpty()) {
        //    conflictContent += ">>>>>>>";
        //} else {
        //    conflictContent += givenFileContent + "\n>>>>>>>";
        //
        //}

        byte[] current = "".getBytes(StandardCharsets.UTF_8);
        byte[] given = "".getBytes(StandardCharsets.UTF_8);

        if (Objects.nonNull(headCommit.getBlob(fileName))){
            current = headCommit.getBlob(fileName).getContent().getBytes();
        }

        if (Objects.nonNull(givenCommit.getBlob(fileName))){
            given = givenCommit.getBlob(fileName).getContent().getBytes();
        }

        File file = Utils.join(CWD, fileName);
        file.createNewFile();
        writeContents(file,"<<<<<<< HEAD\n",current,"=======\n",given,">>>>>>>\n");

        repository.add(fileName);

        System.out.print("Encountered a merge conflict.");
    }
}