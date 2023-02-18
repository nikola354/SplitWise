package bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions;

public class SplitException extends Exception {
    public SplitException(String message) {
        super(message);
    }

    public SplitException(String message, Throwable cause) {
        super(message, cause);
    }
}
