package bg.sofia.uni.fmi.mjt.splitwise.server.logic.splitwise;

import bg.sofia.uni.fmi.mjt.splitwise.server.logic.database.Database;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.AuthenticationException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.FriendException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.GroupException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.InvalidUserData;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.SplitException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.UserAlreadyExists;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.friend.Friendship;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.friend.Group;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.user.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.user.UserPersonal;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

class SplitWiseTest {
    private static final double DELTA = 0.01;

    Database database = Mockito.mock(Database.class);

    SplitWiseAPI splitWiseAPI;

    static UserPersonal nikola;
    static UserPersonal peter;
    static UserPersonal ivan;

    String groupName;

    @BeforeAll
    static void setUsers() {
        nikola = new UserPersonal("nikola123", "Nikola", "Manolov", "qwerty");
        peter = new UserPersonal("peter123", "Peter", "Ivanov", "qwerty");
        ivan = new UserPersonal("ivan123", "Ivan", "Petrov", "qwerty");
    }

    @BeforeEach
    void setSplitWiseAPI() throws UserAlreadyExists, InvalidUserData, GroupException, UserNotFoundException {
        splitWiseAPI = new SplitWise(database);

        splitWiseAPI.signUp(nikola.getUsername(), nikola.getPassword(), nikola.getFirstName(), nikola.getLastName());
        splitWiseAPI.signUp(peter.getUsername(), peter.getPassword(), peter.getFirstName(), peter.getLastName());
        splitWiseAPI.signUp(ivan.getUsername(), ivan.getPassword(), ivan.getFirstName(), ivan.getLastName());

        groupName = "groupOf3";
        splitWiseAPI.createGroup(nikola.getUsername(), groupName, ivan.getUsername(), peter.getUsername());

        splitWiseAPI.addFriend(nikola.getUsername(), peter.getUsername());

        clearInvocations(database);
    }

    @Test
    void testLoginSuccessful() {
        assertTrue(splitWiseAPI.login(nikola.getUsername(), nikola.getPassword()), "The user was expected to log in");
        Mockito.verify(database).clearNotifications(nikola.getUsername());
    }

    @Test
    void testLoginNoSuchUser() {
        assertFalse(splitWiseAPI.login("dragan", nikola.getPassword()), "There is no such user");
        Mockito.verify(database, never()).clearNotifications(anyString());
    }

    @Test
    void testLoginInvalidPassword() {
        assertFalse(splitWiseAPI.login(nikola.getUsername(), "incorrectPass123"), "The password is incorrect");
        Mockito.verify(database, never()).clearNotifications(anyString());
    }

    @Test
    void testSignUpInvalidStrings() {
        assertThrows(IllegalArgumentException.class, () ->
                splitWiseAPI.signUp("dasdas", "", null, "    "),
            "Exception was expected to be thrown because of invalid strings");
        Mockito.verify(database, never()).addUser(any());
    }

    @Test
    void testSignUpInvalidUsername() {
        assertThrows(InvalidUserData.class, () ->
                splitWiseAPI.signUp("_ivan123", "qwerty", "Ivan", "Petrov"),
            "Exception was expected to be thrown");
        Mockito.verify(database, never()).addUser(any());
    }

    @Test
    void testSignUpInvalidFirstName() {
        assertThrows(InvalidUserData.class, () ->
                splitWiseAPI.signUp("ivan123", "qwerty", "I1van", "Petrov"),
            "Exception was expected to be thrown");
        Mockito.verify(database, never()).addUser(any());
    }

    @Test
    void testSignUpInvalidLastName() {
        assertThrows(InvalidUserData.class, () ->
                splitWiseAPI.signUp("ivan123", "qwerty", "Ivan", "Pe''trov"),
            "Exception was expected to be thrown");
        Mockito.verify(database, never()).addUser(any());
    }

    @Test
    void testSignUpPasswordTooShort() {
        assertThrows(InvalidUserData.class, () ->
                splitWiseAPI.signUp("ivan123", "qw", "Ivan", "Petrov"),
            "Exception was expected to be thrown");
        Mockito.verify(database, never()).addUser(any());
    }

    @Test
    void testSignUpUserExists() {
        assertThrows(UserAlreadyExists.class, () ->
                splitWiseAPI.signUp(nikola.getUsername(), "qwerty", "Ivan", "Petrov"),
            "Exception was expected to be thrown");
        Mockito.verify(database, never()).addUser(any());
    }

    @Test
    void testSignUpSuccessful() throws UserAlreadyExists, InvalidUserData, UserNotFoundException {
        UserPersonal mitko = new UserPersonal("mitko123", "Dimitar", "Mitkov", "qwerty");
        splitWiseAPI.signUp(mitko.getUsername(), mitko.getPassword(), mitko.getFirstName(), mitko.getLastName());

        assertEquals(splitWiseAPI.findUser(mitko.getUsername()).getPersonal(), mitko,
            "The signed up user could not be found in the system");
        Mockito.verify(database, times(1)).addUser(any()); //because of @BeforeEach
    }

    @Test
    void testAddFriendInvalidStrings() {
        assertThrows(IllegalArgumentException.class, () ->
                splitWiseAPI.addFriend("", null),
            "Exception was expected to be thrown because of invalid strings");
        Mockito.verify(database, never()).updateFriendsList(any());
    }

    @Test
    void testAddFriendThrowsAuthenticationExc() {
        assertThrows(AuthenticationException.class, () ->
                splitWiseAPI.addFriend("randomUser", peter.getUsername()),
            "Exception was expected to be thrown because the logged-in user does not exist");
        Mockito.verify(database, never()).updateFriendsList(any());
    }

    @Test
    void testAddFriendThrowsUserNotFoundExc() {
        assertThrows(UserNotFoundException.class, () ->
                splitWiseAPI.addFriend(nikola.getUsername(), "randomUser"),
            "Exception was expected to be thrown");
        Mockito.verify(database, never()).updateFriendsList(any());
    }

    @Test
    void testAddFriendThrowsUserAlreadyExists() throws UserNotFoundException, UserAlreadyExists {
        splitWiseAPI.addFriend(nikola.getUsername(), ivan.getUsername());

        assertThrows(UserAlreadyExists.class, () ->
                splitWiseAPI.addFriend(peter.getUsername(), nikola.getUsername()),
            "Exception was expected to be thrown");
        Mockito.verify(database, times(2)).updateFriendsList(any());
    }

    @Test
    void testAddFriendSameUser() throws UserNotFoundException, UserAlreadyExists {
        assertThrows(UserAlreadyExists.class, () ->
                splitWiseAPI.addFriend(peter.getUsername(), peter.getUsername()),
            "Exception was expected to be thrown");
        Mockito.verify(database, never()).updateFriendsList(any());
    }

    @Test
    void testAddFriendIsAdded() throws UserNotFoundException, UserAlreadyExists {
        splitWiseAPI.addFriend(nikola.getUsername(), ivan.getUsername());

        User nikolaUser = splitWiseAPI.findUser(nikola.getUsername());
        User ivanUser = splitWiseAPI.findUser(ivan.getUsername());

        assertTrue(nikolaUser.hasFriend(ivan.getUsername()), "Ivan was not added to nikola's friends list");
        assertTrue(ivanUser.hasFriend(nikola.getUsername()), "Nikola was not added to ivan's friends list");

        Mockito.verify(database, times(2)).updateFriendsList(any());
    }

    @Test
    void testCreateGroupInvalidStrings() {
        assertThrows(IllegalArgumentException.class, () ->
                splitWiseAPI.createGroup("", null, "    "),
            "Exception was expected to be thrown because of invalid strings");
        Mockito.verify(database, atMostOnce()).updateGroup(any());
    }

    @Test
    void testCreateGroupThrowsAuthenticationExc() {
        assertThrows(AuthenticationException.class, () ->
                splitWiseAPI.createGroup("randomUser", "groupName", peter.getUsername()),
            "Exception was expected to be thrown because the logged-in user does not exist");
        Mockito.verify(database, never()).updateGroup(any());
    }

    @Test
    void testCreateGroupThrowsUserNotFoundExc() {
        assertThrows(UserNotFoundException.class, () ->
                splitWiseAPI.createGroup(nikola.getUsername(), "groupName", "randomMember"),
            "Exception was expected to be thrown");
        Mockito.verify(database, never()).updateGroup(any());
    }

    @Test
    void testCreateGroupInvalidGroupName() {
        assertThrows(GroupException.class, () ->
                splitWiseAPI.createGroup(nikola.getUsername(), "invalid.'*", peter.getUsername()),
            "Exception was expected to be thrown");
        Mockito.verify(database, never()).updateGroup(any());
    }

    @Test
    void testCreateGroupNameAlreadyUsed() {
        assertThrows(GroupException.class, () ->
                splitWiseAPI.createGroup(nikola.getUsername(), groupName, peter.getUsername()),
            "Exception was expected to be thrown");
        Mockito.verify(database, never()).updateGroup(any());
    }

    @Test
    void testCreateGroupSameMembers() {
        assertThrows(GroupException.class, () ->
                splitWiseAPI.createGroup(nikola.getUsername(), "groupName", peter.getUsername(), nikola.getUsername()),
            "Exception was expected to be thrown");
        Mockito.verify(database, never()).updateGroup(any());
    }

    @Test
    void testCreateGroupTestIsCreated() throws UserNotFoundException, GroupException {
        String testGroupName = "groupName123";
        splitWiseAPI.createGroup(nikola.getUsername(), testGroupName, peter.getUsername(), ivan.getUsername());

        Group added = splitWiseAPI.findGroup(testGroupName);

        assertTrue(added.hasMember(ivan.getUsername()), "Group was created without ivan inside");
        assertTrue(added.hasMember(nikola.getUsername()), "Group was created without nikola inside");
        assertTrue(added.hasMember(peter.getUsername()), "Group was created without peter inside");
        Mockito.verify(database, times(1)).updateGroup(any());
    }

    @Test
    void testSplitInvalidStrings() {
        assertThrows(IllegalArgumentException.class, () ->
                splitWiseAPI.split("", 555.55, "    ", null),
            "Exception was expected to be thrown because of invalid strings");
        Mockito.verify(database, never()).updateFriendsList(any());
    }

    @Test
    void testSplitDoubleTooLong() {
        assertThrows(IllegalArgumentException.class, () ->
                splitWiseAPI.split(nikola.getUsername(), 555.555, peter.getUsername(), "beer and chips"),
            "Exception was expected to be thrown because the double has more than 2 decimal places");
        Mockito.verify(database, never()).updateFriendsList(any());
    }

    @Test
    void testSplitNegativeDouble() {
        assertThrows(IllegalArgumentException.class, () ->
                splitWiseAPI.split(nikola.getUsername(), -555.50, peter.getUsername(), "beer and chips"),
            "Exception was expected to be thrown because the double is negative");
        Mockito.verify(database, never()).updateFriendsList(any());
    }

    @Test
    void testSplitThrowsAuthenticationExc() {
        assertThrows(AuthenticationException.class, () ->
                splitWiseAPI.split("randomUser", 555.50, peter.getUsername(), "beer and chips"),
            "Exception was expected to be thrown because the logged-in user does not exist");
        Mockito.verify(database, never()).updateFriendsList(any());
    }

    @Test
    void testSplitThrowsUserNotFound() {
        assertThrows(UserNotFoundException.class, () ->
                splitWiseAPI.split(nikola.getUsername(), 555.50, "randomUser", "beer and chips"),
            "Exception was expected to be thrown");
        Mockito.verify(database, never()).updateFriendsList(any());
    }

    @Test
    void testSplitThrowsSplitExc() {
        assertThrows(SplitException.class, () ->
                splitWiseAPI.split(nikola.getUsername(), 0.01, peter.getUsername(), "beer and chips"),
            "Exception was expected to be thrown because the amount is not enough to be split");
        Mockito.verify(database, never()).updateFriendsList(any());
    }

    @Test
    void testSplitThrowsFriendExc() {
        assertThrows(FriendException.class, () ->
                splitWiseAPI.split(nikola.getUsername(), 555.51, ivan.getUsername(), "beer and chips"),
            "Exception was expected to be thrown");
        Mockito.verify(database, never()).updateFriendsList(any());
    }

    @Test
    void testSplitTestIsSplit() throws UserNotFoundException, SplitException, FriendException {
        splitWiseAPI.split(nikola.getUsername(), 120.26, peter.getUsername(), "beer and chips");

        Friendship friendship = splitWiseAPI.getUsers().get(nikola.getUsername()).getFriendsList().getFriendships()
            .get(peter.getUsername());

        assertEquals(friendship.getUserOwes(nikola.getUsername()), -60.13, DELTA,
            "The amount that the user has to receive is not calculated properly");
        assertEquals(friendship.getUserOwes(peter.getUsername()), 60.13, DELTA,
            "The amount that the user owes is not calculated properly");

        Mockito.verify(database, times(2)).updateFriendsList(any());
    }

    @Test
    void testSplitInGroupInvalidStrings() {
        assertThrows(IllegalArgumentException.class, () ->
                splitWiseAPI.splitInGroup("", 555.55, "    ", null),
            "Exception was expected to be thrown because of invalid strings");
        Mockito.verify(database, never()).updateGroup(any());
    }

    @Test
    void testSplitInGroupDoubleTooLong() {
        assertThrows(IllegalArgumentException.class, () ->
                splitWiseAPI.splitInGroup(nikola.getUsername(), 555.555, groupName, "beer and chips"),
            "Exception was expected to be thrown because the double has more than 2 decimal places");
        Mockito.verify(database, never()).updateGroup(any());
    }

    @Test
    void testSplitInGroupNegativeDouble() {
        assertThrows(IllegalArgumentException.class, () ->
                splitWiseAPI.splitInGroup(nikola.getUsername(), -555.50, groupName, "beer and chips"),
            "Exception was expected to be thrown because the double is negative");
        Mockito.verify(database, never()).updateGroup(any());
    }

    @Test
    void testSplitInGroupThrowsAuthenticationExc() {
        assertThrows(AuthenticationException.class, () ->
                splitWiseAPI.splitInGroup("randomUser", 555.50, groupName, "beer and chips"),
            "Exception was expected to be thrown because the logged-in user does not exist");
        Mockito.verify(database, never()).updateGroup(any());
    }

    @Test
    void testSplitInGroupGroupNotExisting() {
        assertThrows(GroupException.class, () ->
                splitWiseAPI.splitInGroup(nikola.getUsername(), 555.50, "randomGroupName212", "beer and chips"),
            "Exception was expected to be thrown");
        Mockito.verify(database, never()).updateGroup(any());
    }

    @Test
    void testSplitInGroupNotPartOfGroup() throws GroupException, UserNotFoundException {
        String notPartOf = "groupWithoutNik";
        splitWiseAPI.createGroup(ivan.getUsername(), notPartOf, peter.getUsername());

        assertThrows(GroupException.class, () ->
                splitWiseAPI.splitInGroup(nikola.getUsername(), 555.50, notPartOf, "beer and chips"),
            "Exception was expected to be thrown");
        Mockito.verify(database, times(1)).updateGroup(any());
    }

    @Test
    void testSplitInGroupThrowsSplitExc() {
        assertThrows(SplitException.class, () ->
                splitWiseAPI.splitInGroup(nikola.getUsername(), 0.02, groupName, "beer and chips"),
            "Exception was expected to be thrown because the amount is not enough to be split");
        Mockito.verify(database, never()).updateGroup(any());
    }

    @Test
    void testSplitInGroupTestIsSplit() throws SplitException, GroupException, UserNotFoundException {
        double split = 30.45;
        splitWiseAPI.splitInGroup(nikola.getUsername(), split, groupName, "beer and chips");

        Group group = splitWiseAPI.findGroup(groupName);

        double expected = split / 3;

        assertEquals(expected, group.leftOwesToRight(ivan.getUsername(), nikola.getUsername()), DELTA,
            "The amount that nikola has to receive is not calculated properly");
        assertEquals(expected, group.leftOwesToRight(peter.getUsername(), nikola.getUsername()), DELTA,
            "The amount that nikola has to receive is not calculated properly");
        assertEquals(0, group.leftOwesToRight(ivan.getUsername(), peter.getUsername()), DELTA,
            "Ivan should not be in debt with peter");

        Mockito.verify(database, times(1)).updateGroup(any());
    }
}