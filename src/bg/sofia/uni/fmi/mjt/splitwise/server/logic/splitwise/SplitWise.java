package bg.sofia.uni.fmi.mjt.splitwise.server.logic.splitwise;

import bg.sofia.uni.fmi.mjt.splitwise.server.logic.calculation.Splitter;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.database.Database;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.AuthenticationException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.FriendException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.GroupException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.InvalidUserData;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.ReceiveException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.SplitException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.UserAlreadyExists;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.friend.FriendsList;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.friend.Friendship;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.friend.Group;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.notification.GroupNotification;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.notification.Notification;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.payment.Payment;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.user.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.user.UserPersonal;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.validation.DoubleValidator;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.validation.StringValidator;
import com.google.gson.Gson;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SplitWise implements SplitWiseAPI {
    Gson gson;

    private Map<String, User> users;
    private Map<String, Group> groups;

    private Database database;

    public SplitWise(Database database) {
        this.database = database;

        users = database.loadUsers();
        groups = database.loadGroups();

        gson = new Gson();
    }

    @Override
    public boolean login(String username, String password) {
        StringValidator.validateStrings(username, password);

        if (!users.containsKey(username)) {
            return false;
        }

        if (users.get(username).login(username, password)) {
            database.clearNotifications(username);
            return true;
        }
        return false;
    }

    @Override
    public void signUp(String username, String password, String firstName, String lastName)
        throws InvalidUserData, UserAlreadyExists {

        StringValidator.validateStrings(username, password, firstName, lastName);

        checkForInvalidData(username, password, firstName, lastName);

        if (users.containsKey(username)) {
            throw new UserAlreadyExists("This username is taken");
        }

        User newUser = new User(new UserPersonal(username, firstName, lastName, password));

        users.put(username, newUser);
        database.addUser(newUser);
    }

    private void checkForInvalidData(String username, String password, String firstName, String lastName)
        throws InvalidUserData {
        boolean ok = true;
        StringBuilder errors = new StringBuilder();

        if (!StringValidator.isValidUsername(username)) {
            ok = false;

            errors.append("The username must contain between " + StringValidator.MIN_USERNAME_LENGTH + " and " +
                StringValidator.MAX_USERNAME_LENGTH +
                " alphanumeric characters. It is not allowed to begin with number.");
            errors.append(System.lineSeparator());
        }

        if (!StringValidator.areValidNames(firstName, lastName)) {
            ok = false;

            errors.append("The first name and last name must contain only letters and must be between " +
                StringValidator.MIN_NAME_LENGTH + " and " + StringValidator.MAX_NAME_LENGTH + " characters long.");
            errors.append(System.lineSeparator());
        }

        if (!StringValidator.isValidPassword(password)) {
            ok = false;

            errors.append("The password must be between " + StringValidator.MIN_PASS_LENGTH + " and " +
                StringValidator.MAX_PASS_LENGTH + " characters long.");
            errors.append(System.lineSeparator());
        }

        if (!ok) {
            throw new InvalidUserData(errors.toString());
        }
    }

    @Override
    public void addFriend(String user, String friend) throws UserNotFoundException, UserAlreadyExists {
        StringValidator.validateStrings(user, friend);
        authenticate(user);

        if (user.equals(friend)) {
            throw new UserAlreadyExists("Cannot add you in your own friends list");
        }

        User friendUser = findUser(friend);

        FriendsList friendsList = users.get(user).getFriendsList();
        if (friendsList.hasFriend(friend)) {
            throw new UserAlreadyExists("Nothing added! This user is already in your friends list");
        }

        Friendship friendship = new Friendship(user, friend);
        friendsList.addFriendship(friendship);
        users.get(friend).getFriendsList().addFriendship(friendship);

        Notification addingFriend = Notification.ofAddingFriend(users.get(user));
        friendUser.addNotification(addingFriend);

        database.updateFriendsList(friendUser);
        database.updateFriendsList(users.get(user));
        database.addFriendNotification(friend, addingFriend);
    }

    @Override
    public void createGroup(String creator, String groupName, String... participants)
        throws UserNotFoundException, GroupException {

        StringValidator.validateStrings(creator, groupName);
        StringValidator.validateStrings(participants);
        authenticate(creator);

        if (!StringValidator.isValidUsername(groupName)) {
            throw new GroupException(
                "The group name must contain between " + StringValidator.MIN_USERNAME_LENGTH + " and " +
                    StringValidator.MAX_USERNAME_LENGTH +
                    " alphanumeric characters. It is not allowed to begin with number.");
        }

        for (String participant : participants) {
            checkUserExists(participant);
        }

        if (groups.containsKey(groupName)) {
            throw new GroupException("There is already a group with this name");
        }

        String[] toAdd = new String[participants.length + 1];
        System.arraycopy(participants, 0, toAdd, 0, participants.length);
        toAdd[participants.length] = creator;
        Group newGroup = new Group(groupName, toAdd);

        groups.put(groupName, newGroup);
        database.updateGroup(newGroup);

        GroupNotification notification = GroupNotification.ofCreatingGroup(groupName, users.get(creator));
        for (String participant : participants) {
            users.get(participant).addNotification(notification);
            database.addGroupNotification(participant, notification);
        }
    }

    @Override
    public void split(String payer, double amount, String friend, String reasonForPayment)
        throws UserNotFoundException, SplitException, FriendException {

        StringValidator.validateStrings(payer, reasonForPayment);
        authenticate(payer);
        DoubleValidator.validateDouble(amount);

        User friendUser = findUser(friend);
        User payerUser = findUser(payer);

        if (!payerUser.getFriendsList().hasFriend(friend)) {
            throw new FriendException(
                "The user " + friendUser + " is not your friend. You can split expenses only with your friends");
        }

        Map<Double, Integer> shares = Splitter.split(amount, 2);
        double share = shares.keySet().iterator().next();

        payerUser.getFriendsList().lendTo(friend, share);
        database.updateFriendsList(payerUser);
        database.updateFriendsList(friendUser);

        Payment payment = new Payment(payer, amount, reasonForPayment, Set.of(friend));
        payerUser.addPayment(payment);
        database.addPayment(payer, payment);

        Notification notification = Notification.ofSplitting(users.get(payer), amount, reasonForPayment);
        friendUser.addNotification(notification);
        database.addFriendNotification(friend, notification);
    }

    @Override
    public void splitInGroup(String payer, double amount, String groupName, String reasonForPayment)
        throws GroupException, SplitException {

        StringValidator.validateStrings(payer, reasonForPayment, groupName);
        authenticate(payer);
        DoubleValidator.validateDouble(amount);

        Group group = findGroup(groupName);
        if (!group.hasMember(payer)) {
            throw new GroupException(
                "You are not member of this group. Cannot split expenses in groups you are not part of");
        }

        group.split(payer, amount);
        database.updateGroup(group);

        Set<String> splitWith = new java.util.HashSet<>(Set.copyOf(group.getMembers()));
        splitWith.remove(payer);
        Payment payment = new Payment(payer, amount, reasonForPayment, splitWith);
        users.get(payer).addPayment(payment);
        database.addPayment(payer, payment);

        GroupNotification n = GroupNotification.ofSplitting(groupName, users.get(payer), amount, reasonForPayment);
        for (String member : group.getMembers()) {
            if (!member.equals(payer)) {
                users.get(member).addNotification(n);
                database.addGroupNotification(member, n);
            }
        }
    }

    @Override
    public String getStatus(String username) throws GroupException {
        StringValidator.validateStrings(username);
        authenticate(username);

        StringBuilder sb = new StringBuilder();

        sb.append("Friends:");
        sb.append(System.lineSeparator());
        sb.append(users.get(username).getStatus());

        sb.append("Groups:");
        sb.append(System.lineSeparator());
        for (Group group : groups.values()) {
            if (group.hasMember(username)) {
                sb.append(group.getStatusFor(username));
            }
        }

        return sb.toString();
    }

    @Override
    public void receive(String receiver, double amount, String sender)
        throws UserNotFoundException, FriendException, ReceiveException {

        StringValidator.validateStrings(receiver, sender);
        authenticate(receiver);
        DoubleValidator.validateDouble(amount);

        User senderUser = findUser(sender);
        User receiverUser = findUser(receiver);

        if (!receiverUser.getFriendsList().hasFriend(sender) || !senderUser.getFriendsList().hasFriend(receiver)) {
            throw new FriendException("You are not friends with this user. Operation canceled");
        }

        receiverUser.getFriendsList().receiveFrom(sender, amount);
        database.updateFriendsList(receiverUser);
        database.updateFriendsList(senderUser);

        Notification notification = Notification.ofReceiving(users.get(receiver), amount);
        senderUser.addNotification(notification);
        database.addFriendNotification(sender, notification);
    }

    @Override
    public void receiveInGroup(String receiver, double amount, String groupName, String sender)
        throws GroupException, UserNotFoundException, ReceiveException {

        StringValidator.validateStrings(receiver, sender, groupName);
        authenticate(receiver);
        DoubleValidator.validateDouble(amount);

        User senderUser = findUser(sender);

        Group group = findGroup(groupName);

        if (!group.hasMember(receiver)) {
            throw new GroupException("You are not in the group with this group name");
        }

        if (!group.hasMember(sender)) {
            throw new GroupException("The user [" + sender + "] is not in the group [" + groupName + "]");
        }

        group.receive(receiver, amount, sender);
        database.updateGroup(group);

        GroupNotification groupNotification = GroupNotification.ofReceiving(groupName, users.get(receiver), amount);
        senderUser.addNotification(groupNotification);
        database.addGroupNotification(sender, groupNotification);
    }

    @Override
    public List<Payment> getPaymentsOf(String username) {
        StringValidator.validateStrings(username);
        authenticate(username);

        return users.get(username).getPayments();
    }

    @Override
    public List<Notification> getFriendsNotifications(String user) throws UserNotFoundException {
        StringValidator.validateStrings(user);

        User foundUser = findUser(user);
        return foundUser.getFriendsNotifications();
    }

    @Override
    public List<GroupNotification> getGroupNotifications(String user) throws UserNotFoundException {
        StringValidator.validateStrings(user);

        User foundUser = findUser(user);
        return foundUser.getGroupNotifications();
    }

    @Override
    public Map<String, User> getUsers() {
        return Collections.unmodifiableMap(users);
    }

    @Override
    public Map<String, Group> getGroups() {
        return Collections.unmodifiableMap(groups);
    }

    public User findUser(String username) throws UserNotFoundException {
        checkUserExists(username);

        return users.get(username);
    }

    private void authenticate(String user) {
        if (!users.containsKey(user)) {
            throw new AuthenticationException("We could not find the logged in user [" + user + "]");
        }
    }

    private void checkUserExists(String username) throws UserNotFoundException {
        if (!users.containsKey(username)) {
            throw new UserNotFoundException("We could not find user with username [" + username + "]");
        }
    }

    public Group findGroup(String groupName) throws GroupException {
        checkGroupExists(groupName);

        return groups.get(groupName);
    }

    private void checkGroupExists(String groupName) throws GroupException {
        if (!groups.containsKey(groupName)) {
            throw new GroupException("There is no group with this group name");
        }
    }
}
