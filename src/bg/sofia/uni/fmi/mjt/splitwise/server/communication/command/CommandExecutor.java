package bg.sofia.uni.fmi.mjt.splitwise.server.communication.command;

import bg.sofia.uni.fmi.mjt.splitwise.server.communication.response.NotificationResponse;
import bg.sofia.uni.fmi.mjt.splitwise.server.communication.response.PaymentsResponse;
import bg.sofia.uni.fmi.mjt.splitwise.server.communication.response.Response;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.AuthenticationException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.FriendException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.GroupException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.InvalidUserData;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.ReceiveException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.SplitException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.UserAlreadyExists;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.UserNotFoundException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.logger.ErrorLogger;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.logger.WarningLogger;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.notification.GroupNotification;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.notification.Notification;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.splitwise.SplitWiseAPI;

import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static bg.sofia.uni.fmi.mjt.splitwise.server.logic.validation.DoubleValidator.DOUBLE_INVALID;
import static bg.sofia.uni.fmi.mjt.splitwise.server.logic.validation.DoubleValidator.getDouble;

public class CommandExecutor {
    private static final int SECOND_ARGUMENT = 2;
    private static final int THIRD_ARGUMENT = 3;

    private final SplitWiseAPI splitWiseAPI;

    public CommandExecutor(SplitWiseAPI splitWiseAPI) {
        this.splitWiseAPI = splitWiseAPI;
    }

    public Response execute(Command command) {

        Response response;
        try {
            if (command == null || command.commandName() == null) {
                throw new IllegalArgumentException("The command is invalid!"); //this is not expected to happen
            }

            response = switch (command.commandName()) {
                case LOGIN -> login(command.arguments());
                case SIGN_UP -> signUp(command.arguments());
                case ADD_FRIEND -> addFriend(command.arguments());
                case CREATE_GROUP -> createGroup(command.arguments());
                case SPLIT -> split(command.arguments());
                case SPLIT_GROUP -> splitInGroup(command.arguments());
                case GET_STATUS -> getStatus(command.arguments());
                case RECEIVE -> receive(command.arguments());
                case RECEIVE_GROUP -> receiveGroup(command.arguments());
                case PAYMENTS -> getPayments(command.arguments());
            };
        } catch (AuthenticationException | IllegalArgumentException e) {
            WarningLogger.log(e.getMessage() + "; stack trace: " + Arrays.toString(e.getStackTrace()));

            return new Response(false, e.getMessage());
        } catch (UncheckedIOException e) {
            ErrorLogger.log(e.getMessage() + "; stack trace: " + Arrays.toString(e.getStackTrace()));

            return new Response(false,
                "Something went wrong with the server's database! Contact an administrator and show them the " +
                    ErrorLogger.FILE_PATH + " file");
        } catch (Exception e) {
            ErrorLogger.log(e.getMessage() + "; stack trace: " + Arrays.toString(e.getStackTrace()));

            return new Response(false,
                "Something went wrong on the server! Contact an administrator and show them the " +
                    ErrorLogger.FILE_PATH + " file");
        }

        return response;
    }

    private Response login(String... args) {
        String username = args[0];
        String password = args[1];

        List<Notification> friendsNotifications;
        List<GroupNotification> groupNotifications;
        try {
            //copy because the notifications will be cleared
            friendsNotifications = List.copyOf(splitWiseAPI.getFriendsNotifications(username));
            groupNotifications = List.copyOf(splitWiseAPI.getGroupNotifications(username));
        } catch (UserNotFoundException e) {
            return new Response(false, e.getMessage());
        }

        if (!splitWiseAPI.login(username, password)) {
            return new Response(false, "Invalid username or password");
        }
        NotificationResponse response =
            new NotificationResponse(true, "Login successful", friendsNotifications, groupNotifications);

        return response;
    }

    private Response signUp(String... args) {
        String username = args[0];
        String password = args[1];
        String firstName = args[SECOND_ARGUMENT];
        String lastName = args[THIRD_ARGUMENT];

        try {
            splitWiseAPI.signUp(username, password, firstName, lastName);
        } catch (InvalidUserData | UserAlreadyExists e) {
            return new Response(false, e.getMessage());
        }

        return new Response(true, "Successfully signed up");
    }

    private Response addFriend(String... args) {
        String user = args[0];
        String friend = args[1];

        try {
            splitWiseAPI.addFriend(user, friend);
        } catch (UserNotFoundException | UserAlreadyExists e) {
            return new Response(false, e.getMessage());
        }

        return new Response(true, "Successfully added " + friend + " to your friends list");
    }

    private Response createGroup(String... args) {
        String creator = args[0];
        String groupName = args[1];

        String[] participants = new String[args.length - 2];
        System.arraycopy(args, 2, participants, 0, args.length - 2);

        try {
            splitWiseAPI.createGroup(creator, groupName, participants);
        } catch (UserNotFoundException | GroupException e) {
            return new Response(false, e.getMessage());
        }

        return new Response(true, "Group created successfully");
    }

    private Response split(String... args) {
        String payer = args[0];

        double amount = getDouble(args[1]);
        if (amount == DOUBLE_INVALID) {
            return new Response(false, "The amount of money to split is not a floating number");
        }

        String friend = args[SECOND_ARGUMENT];
        String reason = args[THIRD_ARGUMENT];

        try {
            splitWiseAPI.split(payer, amount, friend, reason);
        } catch (UserNotFoundException | SplitException | FriendException e) {
            return new Response(false, e.getMessage());
        }

        return new Response(true, "Splitted " + amount + " BGN between you and " + friend);
    }

    private Response splitInGroup(String... args) {
        String payer = args[0];

        double amount = getDouble(args[1]);
        if (amount == DOUBLE_INVALID) {
            return new Response(false, "The amount of money to split is not a floating number");
        }

        String groupName = args[SECOND_ARGUMENT];
        String reason = args[THIRD_ARGUMENT];

        try {
            splitWiseAPI.splitInGroup(payer, amount, groupName, reason);
        } catch (GroupException | SplitException e) {
            return new Response(false, e.getMessage());
        }

        return new Response(true, "Splitted " + amount + " BGN between you and the members of " + groupName);
    }

    private Response getStatus(String... args) {
        String username = args[0];

        String status;
        try {
            status = splitWiseAPI.getStatus(username);
        } catch (GroupException e) {
            return new Response(false, e.getMessage());
        }

        return new Response(true, status);
    }

    private Response receive(String... args) {
        String receiver = args[0];

        double amount = getDouble(args[1]);
        if (amount == DOUBLE_INVALID) {
            return new Response(false, "The amount of money to receive is not a floating number");
        }

        String sender = args[SECOND_ARGUMENT];

        try {
            splitWiseAPI.receive(receiver, amount, sender);
        } catch (UserNotFoundException | FriendException | ReceiveException e) {
            return new Response(false, e.getMessage());
        }

        return new Response(true, "Marked " + amount + " BGN as received from " + sender);
    }

    private Response receiveGroup(String... args) {
        String receiver = args[0];

        double amount = getDouble(args[1]);
        if (amount == DOUBLE_INVALID) {
            return new Response(false, "The amount of money to receive is not a floating number");
        }

        String groupName = args[SECOND_ARGUMENT];
        String sender = args[THIRD_ARGUMENT];

        try {
            splitWiseAPI.receiveInGroup(receiver, amount, groupName, sender);
        } catch (UserNotFoundException | GroupException | ReceiveException e) {
            return new Response(false, e.getMessage());
        }

        return new Response(true, groupName + ": Marked " + amount + " BGN as received from " + sender);
    }

    private Response getPayments(String... args) {
        String user = args[0];

        return new PaymentsResponse(true, "Payments from " + user + ":", splitWiseAPI.getPaymentsOf(user));
    }
}
