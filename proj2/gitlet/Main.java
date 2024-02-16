package gitlet;

import gitlet.models.Repository;
import gitlet.utils.GitletException;

import static gitlet.CommandStrategy.COMMAND_STRATEGIES;
import static gitlet.utils.Utils.messageAndExit;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 * <p>
 * <p>
 * User will call gitlet multiple time
 *
 * @author TODO
 */
public class Main {
  private static final Repository repository = new Repository();
  private static final CommandStrategy commandStrategy = new CommandStrategy();

  /**
   * Usage: java gitlet.Main ARGS, where ARGS contains
   * <COMMAND> <OPERAND1> <OPERAND2> ...
   */
  public static void main(String[] args) throws GitletException {

    /**
     * TODO: Checking order matters
     * If a user inputs a command with the wrong number or format of operands, print the message Incorrect operands. and exit.
     */
    if (args.length == 0) {
      messageAndExit("Please enter a command.");
    }

    String commandType = args[0];

    if (!COMMAND_STRATEGIES.containsKey(commandType)) {
      messageAndExit("No command with that name exists.");
    } else if (!commandType.equals("init") && !repository.gitletExists()) {
      messageAndExit("Not in an initialized Gitlet directory.");
    }

    commandStrategy.execute(commandType, args, repository);
  }
}