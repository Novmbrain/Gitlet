package gitlet;

import gitlet.models.Repository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @className: Command
 * @description: TODO
 * @author: Wenjie FU
 * @date: 06/02/2024
 **/
public class CommandStrategy{
  public static final Map<String, Consumer<String[]>> COMMAND_STRATEGIES = new HashMap<>();
  public static final Repository REPOSITORY = new Repository();

  static {
    COMMAND_STRATEGIES.put("init", CommandStrategy::init);
    COMMAND_STRATEGIES.put("add", CommandStrategy::add);
    COMMAND_STRATEGIES.put("commit", CommandStrategy::commit);
    COMMAND_STRATEGIES.put("log", CommandStrategy::log);
    COMMAND_STRATEGIES.put("status", CommandStrategy::status);
    COMMAND_STRATEGIES.put("rm", CommandStrategy::remove);
    COMMAND_STRATEGIES.put("checkout", CommandStrategy::checkout);
    COMMAND_STRATEGIES.put("branch", CommandStrategy::branch);
    COMMAND_STRATEGIES.put("global-log", CommandStrategy::globalLog);
    COMMAND_STRATEGIES.put("find", CommandStrategy::find);
    COMMAND_STRATEGIES.put("reset", CommandStrategy::reset);
    COMMAND_STRATEGIES.put("rm-branch", CommandStrategy::rmBranch);
  }

  private static void rmBranch(String[] args) {
    String branchName = args[1];
    Repository.rmBranch(branchName);
  }

  private static void reset(String[] args) {
  }

  private static void find(String[] args) {
  }

  private static void globalLog(String[] args) {
    Repository.globalLog();
  }

  private static void init(String[] args) {
    try {
      Repository.init();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void add(String[] args) {
    String fileName = args[1];
    Repository.add(fileName);
  }

  private static void commit(String[] args) {
    String commitMessage = args[1];
    Repository.commit(commitMessage);
  }

  private static void log(String[] args) {
    Repository.log();
  }

  private static void status(String[] args) {
    Repository.status();
  }

  private static void remove(String[] args) {
    String fileName = args[1];
    Repository.rm(fileName);
  }

  private static void checkout(String[] args) {
    int length = args.length;
    if (length == 3) {
      String fileName = args[2];
      Repository.checkoutFile(fileName);
    } else if (length == 4) {
      String commitHash = args[1];
      String fileName = args[3];
      Repository.checkoutFileFromCommit(commitHash, fileName);
    } else if (length == 2) {
      String branchName = args[1];
      Repository.checkoutBranch(branchName);
    }
  }

  private static void branch(String[] args) {
    String branchName = args[1];
    try {
      REPOSITORY.branch(branchName);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}