package bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions;

public class InvalidUserData extends Exception {
    public InvalidUserData(String message) {
        super(message);
    }

    public InvalidUserData(String message, Throwable cause) {
        super(message, cause);
    }
}
