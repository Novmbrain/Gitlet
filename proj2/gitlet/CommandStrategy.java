package gitlet;

import gitlet.models.Repository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static gitlet.utils.Utils.messageAndExit;

/**
 * @className: Command
 * @author: Wenjie FU
 * @date: 06/02/2024
 **/
public class CommandStrategy {
    public static final Map<String, BiConsumer<String[], Repository>> COMMAND_STRATEGIES = new HashMap<>();

    CommandStrategy() {
        COMMAND_STRATEGIES.put("init", this::init);
        COMMAND_STRATEGIES.put("add", this::add);
        COMMAND_STRATEGIES.put("commit", this::commit);
        COMMAND_STRATEGIES.put("log", this::log);
        COMMAND_STRATEGIES.put("status", this::status);
        COMMAND_STRATEGIES.put("rm", this::remove);
        COMMAND_STRATEGIES.put("checkout", this::checkout);
        COMMAND_STRATEGIES.put("branch", this::branch);
        COMMAND_STRATEGIES.put("global-log", this::globalLog);
        COMMAND_STRATEGIES.put("find", this::find);
        COMMAND_STRATEGIES.put("reset", this::reset);
        COMMAND_STRATEGIES.put("rm-branch", this::rmBranch);
        COMMAND_STRATEGIES.put("merge", this::merge);
    }

    private void merge(String[] args, Repository repository) {
        checkOperandLength(args, 2);

        String branchName = args[1];
        repository.merge(branchName);
    }

    private void rmBranch(String[] args, Repository repository) {
        checkOperandLength(args, 2);

        String branchName = args[1];
        repository.rmBranch(branchName);
    }

    private void reset(String[] args, Repository repository) {
        checkOperandLength(args, 2);

        String commitHash = args[1];
        repository.reset(commitHash);
    }

    private void find(String[] args, Repository repository) {
        checkOperandLength(args, 2);

        String commitMessage = args[1];
        repository.find(commitMessage);
    }

    private void globalLog(String[] args, Repository repository) {
        checkOperandLength(args, 1);

        repository.globalLog();
    }

    private void init(String[] args, Repository repository) {
        checkOperandLength(args, 1);

        try {
            repository.init();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void add(String[] args, Repository repository) {
        checkOperandLength(args, 2);

        String fileName = args[1];
        repository.add(fileName);
    }

    private void commit(String[] args, Repository repository) {
        checkOperandLength(args, 2);

        String commitMessage = args[1];
        repository.commit(commitMessage);
    }

    private void log(String[] args, Repository repository) {
        checkOperandLength(args, 1);
        repository.log();
    }

    private void status(String[] args, Repository repository) {
        checkOperandLength(args, 1);
        repository.status();
    }

    private void remove(String[] args, Repository repository) {
        checkOperandLength(args, 2);

        String fileName = args[1];
        repository.rm(fileName);
    }

    private void checkout(String[] args, Repository repository) {
        if (args.length < 2 || args.length > 4) {
            messageAndExit("Incorrect operands.");
        }

        int length = args.length;
        if (length == 3) {
            String fileName = args[2];
            repository.checkoutFile(fileName);
        } else if (length == 4) {
            if (args[2] != null && !args[2].equals("--")) {
                messageAndExit("Incorrect operands.");
            }

            String commitHash = args[1];
            String fileName = args[3];
            repository.checkoutFileFromCommit(commitHash, fileName);
        } else if (length == 2) {
            String branchName = args[1];
            repository.checkoutBranch(branchName);
        }
    }

    private void branch(String[] args, Repository repository) {
        checkOperandLength(args, 2);

        String branchName = args[1];
        try {
            repository.branch(branchName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void execute(String commandType, String[] args, Repository repository) {
        COMMAND_STRATEGIES.get(commandType).accept(args, repository);
    }

    private void checkOperandLength(String[] args, int length) {

        if (args.length != length) {
            messageAndExit("Incorrect operands.");
        }
    }
}
