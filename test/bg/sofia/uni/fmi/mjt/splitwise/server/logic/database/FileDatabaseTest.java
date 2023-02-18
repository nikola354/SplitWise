package bg.sofia.uni.fmi.mjt.splitwise.server.logic.database;

import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.GroupException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.SplitException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.friend.Friendship;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.friend.Group;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.notification.GroupNotification;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.notification.Notification;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.payment.Payment;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.user.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.user.UserPersonal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileDatabaseTest {
    private static final String TEST_MAIN_DIR_NAME = "test_database";

    static Database database;

    static User nikola;
    static User ivan;

    static Notification notification;
    static GroupNotification groupNotification;

    static Group group;

    @BeforeAll
    static void setDatabase() throws GroupException {
        database = new FileDatabase(TEST_MAIN_DIR_NAME);

        nikola = new User(new UserPersonal("nikola", "Nikola", "Manolov", "qwerty"));
        ivan = new User(new UserPersonal("ivan", "Ivan", "Petrov", "qwerty"));
        database.addUser(nikola);
        database.addUser(ivan);

        group = new Group("groupName", nikola.getUsername(), ivan.getUsername());
        database.updateGroup(group);

        notification = Notification.ofReceiving(nikola, 5.52);
        groupNotification = GroupNotification.ofSplitting(group.getName(), nikola, 5.52, "Reason");
    }

    @AfterAll
    static void tearDown() {
        if (!deleteDirectory(new File(TEST_MAIN_DIR_NAME))) {
            throw new RuntimeException("Could not delete directories");
        }
    }

    private static boolean deleteDirectory(File toDelete) {
        File[] allContents = toDelete.listFiles();

        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }

        return toDelete.delete();
    }

    @Test
    void testLoadUsersOnlyPersonalInfo() {
        Map<String, User> allUsers = database.loadUsers();

        assertTrue(allUsers.containsKey(ivan.getUsername()), "Not all users have been loaded from the file");
        assertEquals(allUsers.get(ivan.getUsername()).getPersonal(), ivan.getPersonal(),
            "The personal info is not as expected");

        assertTrue(allUsers.containsKey(nikola.getUsername()), "Not all users have been loaded from the file");
        assertEquals(allUsers.get(nikola.getUsername()).getPersonal(), nikola.getPersonal(),
            "The personal info is not as expected");
    }

    @Test
    void testAddUser() {
        User user = new User(new UserPersonal("newUser", "New", "Person", "qwerty"));

        database.addUser(user);

        Map<String, User> allUsers = database.loadUsers();

        assertTrue(allUsers.containsKey(user.getUsername()), "The user was not added");
        assertEquals(allUsers.get(user.getUsername()).getPersonal(), user.getPersonal(),
            "The personal info was not saved properly");
    }

    @Test
    void testAddPayments() {
        Payment payment = new Payment(ivan.getUsername(), 5.52, "Reason for payment", Set.of("gosho", "pesho"));
        Payment payment2 = new Payment(ivan.getUsername(), 5555.52, "Reason for payment2", Set.of("gosho"));

        database.addPayment(ivan.getUsername(), payment);
        database.addPayment(ivan.getUsername(), payment2);

        Map<String, User> allUsers = database.loadUsers();
        List<Payment> actual = allUsers.get(ivan.getUsername()).getPayments();
        List<Payment> expected = List.of(payment, payment2);

        assertIterableEquals(expected, actual, "The payments were not saved properly");
    }

    @Test
    void testAddFriendNotification() {
        Notification notification2 = Notification.ofAddingFriend(nikola);

        database.addFriendNotification(ivan.getUsername(), notification);
        database.addFriendNotification(ivan.getUsername(), notification2);

        Map<String, User> allUsers = database.loadUsers();
        List<Notification> actual = allUsers.get(ivan.getUsername()).getFriendsNotifications();
        List<Notification> expected = List.of(notification, notification2);

        assertIterableEquals(expected, actual, "The notifications were not saved properly");
    }

    @Test
    void testAddGroupNotification() {
        GroupNotification groupNotification1 = GroupNotification.ofCreatingGroup(group.getName(), nikola);

        database.addGroupNotification(ivan.getUsername(), groupNotification);
        database.addGroupNotification(ivan.getUsername(), groupNotification1);

        Map<String, User> allUsers = database.loadUsers();
        List<GroupNotification> actual = allUsers.get(ivan.getUsername()).getGroupNotifications();
        List<GroupNotification> expected = List.of(groupNotification, groupNotification1);

        assertIterableEquals(expected, actual, "The notifications were not saved properly");
    }

    @Test
    void testClearNotifications() {
        database.addFriendNotification(nikola.getUsername(), notification);
        database.addGroupNotification(nikola.getUsername(), groupNotification);

        database.clearNotifications(nikola.getUsername());

        Map<String, User> allUsers = database.loadUsers();

        assertEquals(0, allUsers.get(nikola.getUsername()).getFriendsNotifications().size(),
            "The friend notifications were not cleared");
        assertEquals(0, allUsers.get(nikola.getUsername()).getGroupNotifications().size(),
            "The group notifications were not cleared");
    }

    @Test
    void testUpdateFriendsList() {
        Friendship friendship = new Friendship(nikola.getUsername(), ivan.getUsername());
        friendship.lend(nikola.getUsername(), 10.02);

        nikola.getFriendsList().addFriendship(friendship);
        ivan.getFriendsList().addFriendship(friendship);

        database.updateFriendsList(nikola);
        database.updateFriendsList(ivan);

        Map<String, User> allUsers = database.loadUsers();

        Map<String, Friendship> ivanFriendships = allUsers.get(ivan.getUsername()).getFriendsList().getFriendships();
        Map<String, Friendship> nikolaFriendships =
            allUsers.get(nikola.getUsername()).getFriendsList().getFriendships();

        assertTrue(ivanFriendships.containsKey(nikola.getUsername()), "The friends list was not properly updated");
        assertTrue(nikolaFriendships.containsKey(ivan.getUsername()), "The friends list was not properly updated");

        assertEquals(friendship, ivanFriendships.get(nikola.getUsername()),
            "The friends list was not properly updated");
        assertEquals(friendship, nikolaFriendships.get(ivan.getUsername()),
            "The friends list was not properly updated");
    }

    @Test
    void testLoadGroups() {
        Map<String, Group> allGroups = database.loadGroups();

        assertTrue(allGroups.containsKey(group.getName()), "Not all groups have been loaded from the file");

        Group actual = allGroups.get(group.getName());
        assertEquals(actual.getName(), group.getName(),
            "The group is not as expected");

        Set<String> members = actual.getMembers();
        Set<String> expected = Set.of(ivan.getUsername(), nikola.getUsername());
        assertTrue(members.containsAll(expected), "The group does not have all members");
        assertTrue(expected.containsAll(members), "The group does not have all members");
    }

    @Test
    void testUpdateGroup() throws SplitException {
        group.split(nikola.getUsername(), 5.55);
        Friendship expected =
            group.getFriendsLists().get(nikola.getUsername()).getFriendships().get(ivan.getUsername());

        database.updateGroup(group);

        Map<String, Group> allGroups = database.loadGroups();
        Group loaded = allGroups.get(group.getName());
        Friendship actual = loaded.getFriendsLists().get(nikola.getUsername()).getFriendships().get(ivan.getUsername());

        assertEquals(expected, actual, "The group was not updated properly");
    }
}