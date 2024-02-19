package gitlet;

import gitlet.models.Repository;
import gitlet.utils.GitletException;

import static gitlet.CommandStrategy.COMMAND_STRATEGIES;
import static gitlet.utils.Utils.messageAndExit;

/**
 * Main class for the Gitlet version control system.
 * This class is responsible for handling the user's commands and executing them.
 */
public class Main {
  /**
   * The repository where the Gitlet data is stored.
   */
  private static final Repository repository = new Repository();

  /**
   * The strategy used to execute the user's commands.
   */
  private static final CommandStrategy commandStrategy = new CommandStrategy();

  /**
   * The main method that is run when the program is started.
   * It checks the user's command and executes it.
   *
   * @param args The arguments provided by the user. The first argument is the command to execute.
   * @throws GitletException If there is an error executing the command.
   */
  public static void main(String[] args) throws GitletException {
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