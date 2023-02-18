package bg.sofia.uni.fmi.mjt.splitwise.server.logic.database;

import bg.sofia.uni.fmi.mjt.splitwise.server.logic.friend.Group;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.notification.GroupNotification;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.notification.Notification;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.payment.Payment;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.user.User;

import java.util.Map;

public interface Database {
    Map<String, User> loadUsers();

    void addUser(User user);

    void addPayment(String username, Payment payment);

    void addFriendNotification(String username, Notification notification);

    void addGroupNotification(String username, GroupNotification groupNotification);

    void clearNotifications(String username); //when logged-in

    void updateFriendsList(User user);

    void updateGroup(Group group);

    Map<String, Group> loadGroups();
}
