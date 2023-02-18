package bg.sofia.uni.fmi.mjt.splitwise.server.logic.user;

import bg.sofia.uni.fmi.mjt.splitwise.server.logic.friend.FriendsList;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.notification.GroupNotification;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.notification.Notification;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.payment.Payment;

import java.util.LinkedList;
import java.util.List;

public class User {
    private UserPersonal personal;

    private List<Notification> friendsNotifications;
    private List<GroupNotification> groupNotifications;
    private FriendsList friendsList;
    private List<Payment> payments;

    public User(UserPersonal personal) {
        this(personal, new LinkedList<>(), new LinkedList<>(), new FriendsList(personal.getUsername()),
            new LinkedList<>());
    }

    public User(UserPersonal personal, List<Notification> friendsNotifications,
                List<GroupNotification> groupNotifications,
                FriendsList friendsList, List<Payment> payments) {
        this.personal = personal;
        this.friendsNotifications = friendsNotifications;
        this.groupNotifications = groupNotifications;
        this.friendsList = friendsList;
        this.payments = payments;
    }

    public boolean login(String username, String password) {
        if (!personal.login(username, password)) {
            return false;
        }

        friendsNotifications.clear();
        groupNotifications.clear();

        return true;
    }

    public UserPersonal getPersonal() {
        return personal;
    }

    public String getUsername() {
        return personal.getUsername();
    }

    public FriendsList getFriendsList() {
        return friendsList;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void addNotification(Notification notification) {
        if (notification instanceof GroupNotification gn) {
            groupNotifications.add(gn);
        } else {
            friendsNotifications.add(notification);
        }
    }

    public void addPayment(Payment payment) {
        payments.add(payment);
    }

    public String getStatus() {
        return friendsList.getStatus();
    }

    public List<Notification> getFriendsNotifications() {
        return friendsNotifications;
    }

    public List<GroupNotification> getGroupNotifications() {
        return groupNotifications;
    }

    public boolean hasFriend(String username) {
        return friendsList.hasFriend(username);
    }

    public double owesToUser(String username) {
        if (!hasFriend(username)) {
            throw new IllegalArgumentException("The users are not friends");
        }

        return friendsList.getFriendships().get(username).getUserOwes(this.getUsername());
    }

    @Override
    public String toString() {
        return personal.toString();
    }
}
