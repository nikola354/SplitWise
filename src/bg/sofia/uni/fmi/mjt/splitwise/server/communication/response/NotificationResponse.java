package bg.sofia.uni.fmi.mjt.splitwise.server.communication.response;

import bg.sofia.uni.fmi.mjt.splitwise.server.logic.notification.GroupNotification;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.notification.Notification;

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
}
