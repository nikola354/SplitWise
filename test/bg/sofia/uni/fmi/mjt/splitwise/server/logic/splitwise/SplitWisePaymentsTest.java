package bg.sofia.uni.fmi.mjt.splitwise.server.logic.splitwise;

import bg.sofia.uni.fmi.mjt.splitwise.server.logic.database.Database;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.AuthenticationException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.FriendException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.GroupException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.InvalidUserData;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.ReceiveException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.SplitException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.UserAlreadyExists;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.friend.Group;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.notification.GroupNotification;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.notification.Notification;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.payment.Payment;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.user.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.user.UserPersonal;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

public class SplitWisePaymentsTest {
    private static final double DELTA = 0.01;

    Database database = Mockito.mock(Database.class);

    SplitWiseAPI splitWiseAPI;

    static UserPersonal nikola;
    static UserPersonal peter;
    static UserPersonal ivan;

    static Payment friendsPayment;
    static Payment groupPayment;

    String groupName;

    List<Notification> peterNotifications;
    List<GroupNotification> peterGrNotifications;

    @BeforeAll
    static void setUsers() {
        nikola = new UserPersonal("nikola123", "Nikola", "Manolov", "qwerty");
        peter = new UserPersonal("peter123", "Peter", "Ivanov", "qwerty");
        ivan = new UserPersonal("ivan123", "Ivan", "Petrov", "qwerty");

        friendsPayment = new Payment(nikola.getUsername(), 20.44, "toilet paper", Set.of(peter.getUsername()));
        groupPayment =
            new Payment(nikola.getUsername(), 30.33, "cinema tickets", Set.of(peter.getUsername(), ivan.getUsername()));
    }

    @BeforeEach
    void setSplitWiseAPI()
        throws UserAlreadyExists, InvalidUserData, GroupException, UserNotFoundException, SplitException,
        FriendException {
        splitWiseAPI = new SplitWise(database);

        splitWiseAPI.signUp(nikola.getUsername(), nikola.getPassword(), nikola.getFirstName(), nikola.getLastName());
        splitWiseAPI.signUp(peter.getUsername(), peter.getPassword(), peter.getFirstName(), peter.getLastName());
        splitWiseAPI.signUp(ivan.getUsername(), ivan.getPassword(), ivan.getFirstName(), ivan.getLastName());

        User actor = splitWiseAPI.findUser(nikola.getUsername());
        peterNotifications = new LinkedList<>();
        peterGrNotifications = new LinkedList<>();

        splitWiseAPI.addFriend(nikola.getUsername(), peter.getUsername());
        peterNotifications.add(Notification.ofAddingFriend(actor));
        splitWiseAPI.split(friendsPayment.issuer(), friendsPayment.amount(), peter.getUsername(),
            friendsPayment.reason());
        peterNotifications.add(Notification.ofSplitting(actor, friendsPayment.amount() / 2, friendsPayment.reason()));

        groupName = "groupOf3";
        splitWiseAPI.createGroup(nikola.getUsername(), groupName, ivan.getUsername(), peter.getUsername());
        peterGrNotifications.add(GroupNotification.ofCreatingGroup(groupName, actor));
        splitWiseAPI.splitInGroup(groupPayment.issuer(), groupPayment.amount(), groupName, groupPayment.reason());
        peterGrNotifications.add(
            GroupNotification.ofSplitting(groupName, actor, groupPayment.amount(), groupPayment.reason()));

        clearInvocations(database);
    }

    @Test
    void testReceiveInvalidStrings() {
        assertThrows(IllegalArgumentException.class, () ->
                splitWiseAPI.receive("", 5, "    "),
            "Exception was expected to be thrown because of invalid strings");
        Mockito.verify(database, never()).updateFriendsList(any());
    }

    @Test
    void testReceiveDoubleTooLong() {
        assertThrows(IllegalArgumentException.class, () ->
                splitWiseAPI.receive(nikola.getUsername(), 5.555, peter.getUsername()),
            "Exception was expected to be thrown because the double has more than 2 decimal places");
        Mockito.verify(database, never()).updateFriendsList(any());
    }

    @Test
    void testReceiveNegativeDouble() {
        assertThrows(IllegalArgumentException.class, () ->
                splitWiseAPI.receive(nikola.getUsername(), -5, peter.getUsername()),
            "Exception was expected to be thrown because the double is negative");
        Mockito.verify(database, never()).updateFriendsList(any());
    }

    @Test
    void testReceiveThrowsAuthenticationExc() {
        assertThrows(AuthenticationException.class, () ->
                splitWiseAPI.receive("randomUser", 5, peter.getUsername()),
            "Exception was expected to be thrown because the logged-in user does not exist");
        Mockito.verify(database, never()).updateFriendsList(any());
    }

    @Test
    void testReceiveThrowsUserNotFound() {
        assertThrows(UserNotFoundException.class, () ->
                splitWiseAPI.receive(nikola.getUsername(), 5, "randomUser"),
            "Exception was expected to be thrown because the sender does not exist");
        Mockito.verify(database, never()).updateFriendsList(any());
    }

    @Test
    void testReceiveThrowsFriendExc() {
        assertThrows(FriendException.class, () ->
                splitWiseAPI.receive(nikola.getUsername(), 5, ivan.getUsername()),
            "Exception was expected to be thrown because ivan is not friend with nikola");
        Mockito.verify(database, never()).updateFriendsList(any());
    }

    @Test
    void testReceiveNotOwingMoney() {
        assertThrows(ReceiveException.class, () ->
                splitWiseAPI.receive(peter.getUsername(), 5, nikola.getUsername()),
            "Exception was expected to be thrown because nikola does not owe money to peter");
        Mockito.verify(database, never()).updateFriendsList(any());
    }

    @Test
    void testReceiveTooBigAmount() {
        assertThrows(ReceiveException.class, () ->
                splitWiseAPI.receive(nikola.getUsername(), 500, peter.getUsername()),
            "Exception was expected to be thrown because peter does not owe so much money to nikola");
        Mockito.verify(database, never()).updateFriendsList(any());
    }

    @Test
    void testReceiveTestIsReceived() throws UserNotFoundException, FriendException, ReceiveException {
        double received = 5;
        splitWiseAPI.receive(nikola.getUsername(), received, peter.getUsername());

        User peterUser = splitWiseAPI.findUser(peter.getUsername());

        double expected = friendsPayment.amount() / 2 - received;
        assertEquals(expected, peterUser.owesToUser(nikola.getUsername()), DELTA,
            "The new owed amount is not as expected");

        Mockito.verify(database, times(2)).updateFriendsList(any());
    }

    @Test
    void testReceiveInGroupInvalidStrings() {
        assertThrows(IllegalArgumentException.class, () ->
                splitWiseAPI.receiveInGroup("", 5, "    ", null),
            "Exception was expected to be thrown because of invalid strings");
        Mockito.verify(database, never()).updateGroup(any());
    }

    @Test
    void testReceiveInGroupDoubleTooLong() {
        assertThrows(IllegalArgumentException.class, () ->
                splitWiseAPI.receiveInGroup(nikola.getUsername(), 5.555, groupName, peter.getUsername()),
            "Exception was expected to be thrown because the double has more than 2 decimal places");
        Mockito.verify(database, never()).updateGroup(any());
    }

    @Test
    void testReceiveInGroupNegativeDouble() {
        assertThrows(IllegalArgumentException.class, () ->
                splitWiseAPI.receiveInGroup(nikola.getUsername(), -5, groupName, peter.getUsername()),
            "Exception was expected to be thrown because the double is negative");
        Mockito.verify(database, never()).updateGroup(any());
    }

    @Test
    void testReceiveInGroupThrowsAuthenticationExc() {
        assertThrows(AuthenticationException.class, () ->
                splitWiseAPI.receiveInGroup("randomUser", 5, groupName, peter.getUsername()),
            "Exception was expected to be thrown because the logged-in user does not exist");
        Mockito.verify(database, never()).updateGroup(any());
    }

    @Test
    void testReceiveInGroupThrowsUserNotFound() {
        assertThrows(UserNotFoundException.class, () ->
                splitWiseAPI.receiveInGroup(nikola.getUsername(), 5, groupName, "randomUser"),
            "Exception was expected to be thrown because the sender does not exist");
        Mockito.verify(database, never()).updateGroup(any());
    }

    @Test
    void testReceiveInGroupGroupNotFound() {
        assertThrows(GroupException.class, () ->
                splitWiseAPI.receiveInGroup(nikola.getUsername(), 5, "randomGroup", peter.getUsername()),
            "Exception was expected to be thrown because the group does not exist");
        Mockito.verify(database, never()).updateGroup(any());
    }

    @Test
    void testReceiveInGroupReceiverNotInGroup() throws UserNotFoundException, GroupException {
        String notPartOf = "groupWithoutNik";
        splitWiseAPI.createGroup(ivan.getUsername(), notPartOf, peter.getUsername());

        assertThrows(GroupException.class, () ->
                splitWiseAPI.receiveInGroup(nikola.getUsername(), 5, notPartOf, peter.getUsername()),
            "Exception was expected to be thrown because the receiver is not in the group");
        Mockito.verify(database, times(1)).updateGroup(any());
    }

    @Test
    void testReceiveInGroupSenderNotInGroup() throws UserNotFoundException, GroupException {
        String notPartOf = "groupWithoutPeter";
        splitWiseAPI.createGroup(nikola.getUsername(), notPartOf, ivan.getUsername());

        assertThrows(GroupException.class, () ->
                splitWiseAPI.receiveInGroup(nikola.getUsername(), 5, notPartOf, peter.getUsername()),
            "Exception was expected to be thrown because the sender is not in the group");
        Mockito.verify(database, times(1)).updateGroup(any());
    }

    @Test
    void testReceiveInGroupNotOwingMoney() {
        assertThrows(ReceiveException.class, () ->
                splitWiseAPI.receiveInGroup(peter.getUsername(), 5, groupName, nikola.getUsername()),
            "Exception was expected to be thrown because nikola does not owe money to peter");
        Mockito.verify(database, never()).updateGroup(any());
    }

    @Test
    void testReceiveInGroupTooBigAmount() {
        assertThrows(ReceiveException.class, () ->
                splitWiseAPI.receiveInGroup(nikola.getUsername(), 500, groupName, peter.getUsername()),
            "Exception was expected to be thrown because peter does not owe so much money to nikola");
        Mockito.verify(database, never()).updateGroup(any());
    }

    @Test
    void testReceiveInGroupTestIsReceived() throws UserNotFoundException, GroupException, ReceiveException {
        double received = 5;
        splitWiseAPI.receiveInGroup(nikola.getUsername(), received, groupName, peter.getUsername());

        Group group = splitWiseAPI.findGroup(groupName);

        double expected = groupPayment.amount() / 3 - received;
        assertEquals(expected, group.leftOwesToRight(peter.getUsername(), nikola.getUsername()), DELTA,
            "The new owed amount is not as expected");
        assertEquals(-1 * expected, group.leftOwesToRight(nikola.getUsername(), peter.getUsername()), DELTA,
            "The new owed amount is not as expected");

        Mockito.verify(database, times(1)).updateGroup(any());
    }

    @Test
    void testSplitRecalculatingDebts() throws UserNotFoundException, SplitException, FriendException {
        double split = 10;
        splitWiseAPI.split(peter.getUsername(), split, nikola.getUsername(), "splitting backwards");

        User peterUser = splitWiseAPI.findUser(peter.getUsername());
        User nikolaUser = splitWiseAPI.findUser(nikola.getUsername());

        double peterOwes = friendsPayment.amount() / 2 - split / 2;
        assertEquals(peterOwes, peterUser.owesToUser(nikola.getUsername()), DELTA,
            "The dept was not precalculated properly");
        assertEquals(-1 * peterOwes, nikolaUser.owesToUser(peterUser.getUsername()), DELTA,
            "The dept was not precalculated properly");
    }

    @Test
    void testGetPaymentsOfInvalidStrings() {
        assertThrows(IllegalArgumentException.class, () ->
                splitWiseAPI.getPaymentsOf(""),
            "Exception was expected to be thrown because of invalid strings");
    }

    @Test
    void testGetPaymentsOfThrowsAuthenticationExc() {
        assertThrows(AuthenticationException.class, () ->
                splitWiseAPI.getPaymentsOf("randomUser"),
            "Exception was expected to be thrown because the logged-in user does not exist");
    }

    @Test
    void testGetPaymentsOfReturnsList() {
        List<Payment> actual = splitWiseAPI.getPaymentsOf(nikola.getUsername());
        List<Payment> expected = List.of(friendsPayment, groupPayment);
        assertIterableEquals(expected, actual, "The payments are not as expected");
    }

    @Test
    void testGetPaymentsOfReturnsEmptyList() {
        List<Payment> actual = splitWiseAPI.getPaymentsOf(ivan.getUsername());
        List<Payment> expected = new ArrayList<>();
        assertIterableEquals(expected, actual, "The method must return empty list");
    }

    @Test
    void testGetFriendsNotificationsInvalidStrings() {
        assertThrows(IllegalArgumentException.class, () ->
                splitWiseAPI.getFriendsNotifications(""),
            "Exception was expected to be thrown because of invalid strings");
    }

    @Test
    void testGetFriendsNotificationsThrowsAuthenticationExc() {
        assertThrows(UserNotFoundException.class, () ->
                splitWiseAPI.getFriendsNotifications("randomUser"),
            "Exception was expected to be thrown because the user does not exist");
    }

    @Test
    void testGetFriendsNotificationsReturnsList() throws UserNotFoundException {
        List<Notification> actual = splitWiseAPI.getFriendsNotifications(peter.getUsername());

        assertIterableEquals(peterNotifications, actual, "The notifications are not as expected");
    }

    @Test
    void testGetGroupNotificationsInvalidStrings() {
        assertThrows(IllegalArgumentException.class, () ->
                splitWiseAPI.getGroupNotifications(""),
            "Exception was expected to be thrown because of invalid strings");
    }

    @Test
    void testGetGroupNotificationsThrowsAuthenticationExc() {
        assertThrows(UserNotFoundException.class, () ->
                splitWiseAPI.getGroupNotifications("randomUser"),
            "Exception was expected to be thrown because the user does not exist");
    }

    @Test
    void testGetGroupNotificationsReturnsList() throws UserNotFoundException {
        List<GroupNotification> actual = splitWiseAPI.getGroupNotifications(peter.getUsername());

        assertIterableEquals(peterGrNotifications, actual, "The notifications are not as expected");
    }
}
