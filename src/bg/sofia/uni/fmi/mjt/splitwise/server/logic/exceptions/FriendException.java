package bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions;

public class FriendException extends Exception {
    public FriendException(String message) {
        super(message);
    }

    public FriendException(String message, Throwable cause) {
        super(message, cause);
    }
}
