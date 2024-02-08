package gitlet;

import gitlet.models.Repository;

import static gitlet.CommandStrategy.COMMAND_STRATEGIES;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 * <p>
 * <p>
 * User will call gitlet multiple time
 *
 * @author TODO
 */
public class Main {

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

    if (!COMMAND_STRATEGIES.containsKey(commandType)) {
      System.out.println("No command with that name exists.");
      return;
    } else if (!commandType.equals("init") && !Repository.gitletExists()) {
      System.out.println("Not in an initialized Gitlet directory.");
      return;
    }

    COMMAND_STRATEGIES.get(commandType).accept(args);
  }

  private static boolean operandChecker(String[] args) {
    // TODO: check if the number command's operand is correct
    return true;
  }
}