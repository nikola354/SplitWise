package bg.sofia.uni.fmi.mjt.splitwise.server.communication.response;

public class Response {
    private final boolean ok;
    private final String response;

    public Response(boolean ok, String response) {
        this.ok = ok;
        this.response = response;
    }
}
