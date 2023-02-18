package bg.sofia.uni.fmi.mjt.splitwise.client.dto.response;

public class Response {
    private final boolean ok;
    private final String response;

    public Response(boolean ok, String response) {
        this.ok = ok;
        this.response = response;
    }

    public boolean isOk() {
        return ok;
    }

    public String getResponse() {
        return response;
    }
}
