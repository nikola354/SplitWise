package bg.sofia.uni.fmi.mjt.splitwise.client.dto.notification;

public class Notification {
    private String text;
    private String type;

    public Notification(String text) {
        this.text = text;

        this.type = this.getClass().getName();
    }

    public String getText() {
        return text;
    }
}
