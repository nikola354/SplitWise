package bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions;

public class GroupAlreadyExists extends Exception {
    public GroupAlreadyExists(String message) {
        super(message);
    }

    public GroupAlreadyExists(String message, Throwable cause) {
        super(message, cause);
    }
}
