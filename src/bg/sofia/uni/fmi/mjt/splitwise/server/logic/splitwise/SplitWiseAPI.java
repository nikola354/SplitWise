package bg.sofia.uni.fmi.mjt.splitwise.server.logic.splitwise;

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

import java.util.List;
import java.util.Map;

public interface SplitWiseAPI {
    /**
     * @param username the username to log in with
     * @param password
     * @return true if login is successful or false if there is no such username or the password is incorrect
     */
    boolean login(String username, String password);

    /**
     * @param username
     * @param password
     * @param firstName
     * @param lastName
     * @throws IllegalArgumentException if one of the parameters is null, empty or blank
     * @throws InvalidUserData          if one of the parameters is invalid
     * @throws UserAlreadyExists        if there is a user with this username
     */
    void signUp(String username, String password, String firstName, String lastName)
        throws InvalidUserData, UserAlreadyExists;

    /**
     * @param user   the username of the user that orders this action
     * @param friend the username of the friend to be added
     * @throws IllegalArgumentException if one of the parameters is null, empty or blank
     * @throws AuthenticationException  if the user is not in the dataset with registered users
     * @throws UserNotFoundException    if the friend is not in the dataset with registered users
     * @throws UserAlreadyExists        if the two users are already friends or the two strings are the same
     */
    void addFriend(String user, String friend) throws UserNotFoundException, UserAlreadyExists;

    /**
     * @param creator      the creator of the group (logged-in user)
     * @param groupName    the name of the group
     * @param participants the participants except the creator
     * @throws IllegalArgumentException if one of the parameters is null, empty or blank
     * @throws AuthenticationException  if the creator is not in the dataset with registered users
     * @throws UserNotFoundException    if any of the participants is not in the dataset with registered users
     * @throws GroupException           - if the groupName is not a valid username or
     *                                  - if there is already a group with this name or
     *                                  - if 2 or more participants have the same name
     */
    void createGroup(String creator, String groupName, String... participants)
        throws UserNotFoundException, GroupException;

    /**
     * @param payer            the payer who wants to split the bill
     * @param amount           the amount that is paid
     * @param friend           the friend with whom the payer wants to split
     * @param reasonForPayment the description of the expense
     * @throws IllegalArgumentException - if one of the parameters is null, empty or blank or
     *                                  - if the amount is not a valid double or with more than 2 decimal places
     * @throws AuthenticationException  if the creator is not in the dataset with registered users
     * @throws UserNotFoundException    if the friend is not in the dataset with registered users
     * @throws SplitException           if the amount is less than 0.02
     * @throws FriendException          if the friend is not in the friends list of the payer
     */
    void split(String payer, double amount, String friend, String reasonForPayment)
        throws UserNotFoundException, SplitException, FriendException;

    /**
     * @param payer            the payer who wants to split the bill
     * @param amount           the amount that is paid
     * @param groupName        the group in which the payer wants to split the amount
     * @param reasonForPayment the description of the expense
     * @throws IllegalArgumentException - if one of the parameters is null, empty or blank or
     *                                  - if the amount is not a valid double or with more than 2 decimal places
     * @throws AuthenticationException  if the creator is not in the dataset with registered users
     * @throws SplitException           if the amount is less than 0.01 * the number of the members,
     *                                  because everybody must have a share of at least 0.01
     * @throws GroupException           - if a group with this name does not exist or
     *                                  - if the payer is not part of the group
     */
    void splitInGroup(String payer, double amount, String groupName, String reasonForPayment)
        throws GroupException, SplitException;

    String getStatus(String username) throws GroupException;

    /**
     * @param receiver the user who marks the money as received
     * @param amount   the received amount
     * @param sender   the user who owes money
     * @throws IllegalArgumentException - if one of the parameters is null, empty or blank or
     *                                  - if the amount is not a valid double or with more than 2 decimal places
     * @throws AuthenticationException  if the receiver is not in the dataset with registered users
     * @throws UserNotFoundException    if the sender is not in the dataset with registered users
     * @throws FriendException          if the sender is not in the friends list of the receiver
     * @throws ReceiveException         - if the sender does not owe money to the receiver or
     *                                  - if the owed money are less than the amount
     */
    void receive(String receiver, double amount, String sender)
        throws UserNotFoundException, FriendException, ReceiveException;

    /**
     * @param receiver  the user who marks the money as received in the specific group
     * @param amount    the received amount
     * @param groupName the name of the group
     * @param sender    the user who owes money
     * @throws IllegalArgumentException - if one of the parameters is null, empty or blank or
     *                                  - if the amount is not a valid double or with more than 2 decimal places
     * @throws AuthenticationException  if the receiver is not in the dataset with registered users
     * @throws UserNotFoundException    if the sender is not in the dataset with registered users
     * @throws GroupException           - if a group with this name does not exist or
     *                                  - if the receiver or sender are not part of the group
     * @throws ReceiveException         - if the sender does not owe money to the receiver or
     *                                  - if the owed money are less than the amount
     */
    void receiveInGroup(String receiver, double amount, String groupName, String sender)
        throws GroupException, UserNotFoundException, ReceiveException;

    /**
     * @param username the username of the user
     * @return a list with all payments from that user
     * @throws IllegalArgumentException if the parameter is null, empty or blank
     * @throws AuthenticationException  if the username is not in the dataset with registered users
     */
    List<Payment> getPaymentsOf(String username);

    /**
     * @param user the user
     * @return list of all friends notifications for that user
     * @throws IllegalArgumentException if the parameter is null, empty or blank
     * @throws UserNotFoundException    if the user does not exist
     */
    List<Notification> getFriendsNotifications(String user) throws UserNotFoundException;

    /**
     * @param user the user
     * @return list of all group notifications for that user
     * @throws IllegalArgumentException if the parameter is null, empty or blank
     * @throws UserNotFoundException    if the user does not exist
     */
    List<GroupNotification> getGroupNotifications(String user) throws UserNotFoundException;

    /**
     * @return all users in SplitWise
     */
    Map<String, User> getUsers();

    /**
     * @return all groups in SplitWise
     */
    Map<String, Group> getGroups();

    /**
     * @param username the username of the user
     * @return the User object
     * @throws UserNotFoundException if the user does not exist
     */
    User findUser(String username) throws UserNotFoundException;

    /**
     * @param groupName the name of the group
     * @return the group object
     * @throws GroupException if the group does not exist
     */
    Group findGroup(String groupName) throws GroupException;
}
