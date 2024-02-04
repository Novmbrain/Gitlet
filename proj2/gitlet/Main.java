package gitlet;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 * <p>
 * <p>
 * User will call gitlet multiple time
 *
 * @author TODO
 */
public class Main {

  private static final Map<String, Consumer<String[]>> commandStrategies = new HashMap<>();

  static {
    // Initialize the strategies
    commandStrategies.put("init", args -> {
      Command.init();
    });
    commandStrategies.put("add", args -> {
      String fileName = args[1];
      Command.add(fileName);
    });
    commandStrategies.put("commit", args -> {
      String commitMessage = args[1];
      Command.commit(commitMessage);
    });
    commandStrategies.put("log", args -> Command.log());
    commandStrategies.put("status", args -> Command.status());
    commandStrategies.put("rm", args -> {
      String fileName = args[1];
      Command.rm(fileName);
    });
  }

  /**
   * Usage: java gitlet.Main ARGS, where ARGS contains
   * <COMMAND> <OPERAND1> <OPERAND2> ...
   */
  public static void main(String[] args) {

    /**
     * TODO: Checking order matters
     * If a user inputs a command with the wrong number or format of operands, print the message Incorrect operands. and exit.
     */
    if (args.length == 0) {
      System.out.println("Please enter a command.");
      return;
    }

    String commandType = args[0];

    if (!commandStrategies.containsKey(commandType)) {
      System.out.println("No command with that name exists.");
      return;
    } else if (!commandType.equals("init") && !Repository.gitletExists()) {
      System.out.println("Not in an initialized Gitlet directory.");
      return;
    }

    commandStrategies.get(commandType).accept(args);
  }

  private static boolean operandChecker(String[] args) {
    // check if the number command's operand is correct
    return true;
  }
}