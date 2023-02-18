package bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions;

public class GroupException extends Exception {
    public GroupException(String message) {
        super(message);
    }

    public GroupException(String message, Throwable cause) {
        super(message, cause);
    }
}
