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
    String firstArg = args[0];

    if (firstArg.equals("init")) {
      Command.init();
    } else {
      //TODO: check if .gitlet exists

      switch (firstArg) {
        case "add":
          Command.add("");
          break;
        case "commit":
          Command.commit("");
      }
    }
  }
}