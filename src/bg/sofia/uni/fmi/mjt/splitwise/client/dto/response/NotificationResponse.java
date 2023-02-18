package bg.sofia.uni.fmi.mjt.splitwise.client.dto.response;

import bg.sofia.uni.fmi.mjt.splitwise.client.dto.notification.GroupNotification;
import bg.sofia.uni.fmi.mjt.splitwise.client.dto.notification.Notification;

import java.util.List;

public class NotificationResponse extends Response {
    private List<Notification> friendsNotifications;
    private List<GroupNotification> groupNotifications;

    public NotificationResponse(boolean ok, String response, List<Notification> friendsNotifications,
                                List<GroupNotification> groupNotifications) {
        super(ok, response);
        this.friendsNotifications = friendsNotifications;
        this.groupNotifications = groupNotifications;
    }

    public List<Notification> getFriendsNotifications() {
        return friendsNotifications;
    }

    public List<GroupNotification> getGroupNotifications() {
        return groupNotifications;
    }
}
