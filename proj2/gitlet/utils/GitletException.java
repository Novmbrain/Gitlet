package gitlet.utils;

/**
 * General exception indicating a Gitlet error.  For fatal errors, the
 * result of .getMessage() is the error message to be printed.
 *
 * @author P. N. Hilfinger
 */
public class GitletException extends RuntimeException {


    /**
     * A GitletException with no message.
     */
    public GitletException() {
        super();
    }

    /**
     * A GitletException MSG as its message.
     */
    public GitletException(String msg) {
        super(msg);
    }

}
