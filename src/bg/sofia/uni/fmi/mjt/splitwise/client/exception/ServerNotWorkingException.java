package bg.sofia.uni.fmi.mjt.splitwise.client.exception;

public class ServerNotWorkingException extends Exception {
    public ServerNotWorkingException(String message) {
        super(message);
    }

    public ServerNotWorkingException(String message, Throwable cause) {
        super(message, cause);
    }
}
