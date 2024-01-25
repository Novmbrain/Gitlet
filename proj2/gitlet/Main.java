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
     *
     * If a user doesn’t input any arguments, print the message Please enter a command. and exit.
     *
     * If a user inputs a command that doesn’t exist, print the message No command with that name exists. and exit.
     *
     * If a user inputs a command with the wrong number or format of operands, print the message Incorrect operands. and exit.
     *
     * If a user inputs a command that requires being in an initialized Gitlet working directory (i.e., one containing a .gitlet subdirectory),
     * but is not in such a directory, print the message Not in an initialized Gitlet directory.
     */
    String firstArg = args[0];

    if (firstArg.equals("init")) {
      Command.init();
    } else {
      //TODO: check if .gitlet exists
      switch (firstArg) {
        case "add":
          Command.add(args[1]);
          break;
        case "commit":
          Command.commit("");
      }
    }
  }
}