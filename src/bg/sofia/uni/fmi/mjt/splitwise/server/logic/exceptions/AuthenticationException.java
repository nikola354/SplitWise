package bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions;

public class AuthenticationException extends RuntimeException { //runtime because it is not expected
    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
