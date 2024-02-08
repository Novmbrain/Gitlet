package gitlet;

import gitlet.models.Command;

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

  static {
    // Initialize the strategies
    COMMAND_STRATEGIES.put("init", args -> {
      Command.init();
    });

    COMMAND_STRATEGIES.put("add", args -> {
      String fileName = args[1];
      Command.add(fileName);
    });

    COMMAND_STRATEGIES.put("commit", args -> {
      String commitMessage = args[1];
      Command.commit(commitMessage);
    });

    COMMAND_STRATEGIES.put("log", args -> Command.log());

    COMMAND_STRATEGIES.put("status", args -> Command.status());

    COMMAND_STRATEGIES.put("rm", args -> {
      String fileName = args[1];
      Command.rm(fileName);
    });

    COMMAND_STRATEGIES.put("checkout", args -> {
      int length = args.length;
      if (length == 3) {
        String fileName = args[2];
        Command.checkoutFile(fileName);
      } else if (length == 4) {
        String commitHash = args[1];
        String fileName = args[3];
        Command.checkoutFileFromCommit(commitHash, fileName);
      } else if (length == 2) {
        // TODO: checkout branch
        String branchName = args[1];
        Command.checkoutBranch(branchName);
      }
    });
    COMMAND_STRATEGIES.put("branch", args -> {
      String branchName = args[1];
        Command.branch(branchName);
    });
  }
}