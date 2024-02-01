package gitlet;

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
    // TODO: what if args is empty?
    /**
     *
     * TODO:
     * There are some failure cases you need to handle that don’t apply to a particular command. Here they are:
     * <p>
     * If a user doesn’t input any arguments, print the message Please enter a command. and exit.
     * <p>
     * If a user inputs a command that doesn’t exist, print the message No command with that name exists. and exit.
     * <p>
     * If a user inputs a command with the wrong number or format of operands, print the message Incorrect operands. and exit.
     * <p>
     * If a user inputs a command that requires being in an initialized Gitlet working directory (i.e., one containing a .gitlet subdirectory),
     * but is not in such a directory, print the message Not in an initialized Gitlet directory.
     */
    String commandType = args[0];

    if (commandType.equals("init")) {
      Command.init();
    } else {
      // TODO: check if .gitlet exists
      switch (commandType) {
        case "add":
          String fileName = args[1];
          Command.add(fileName);
          break;
        case "commit":
          String commitMessage = args[1];
          Command.commit(commitMessage);
          break;
        case "log":
          Command.log();
          break;
      }
    }
  }
}