package bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions;

public class UserAlreadyExists extends Exception {
    public UserAlreadyExists(String message) {
        super(message);
    }

    public UserAlreadyExists(String message, Throwable cause) {
        super(message, cause);
    }
}
