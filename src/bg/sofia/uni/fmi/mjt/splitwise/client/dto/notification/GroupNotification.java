package bg.sofia.uni.fmi.mjt.splitwise.client.dto.notification;

public class GroupNotification extends Notification {
    private String group;

    public GroupNotification(String text, String group) {
        super(text);
        this.group = group;
    }

    public String getGroup() {
        return group;
    }
}
