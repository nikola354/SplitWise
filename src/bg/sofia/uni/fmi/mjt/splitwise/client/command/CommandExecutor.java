package bg.sofia.uni.fmi.mjt.splitwise.client.command;

import bg.sofia.uni.fmi.mjt.splitwise.client.Client;
import bg.sofia.uni.fmi.mjt.splitwise.client.dto.Payment;
import bg.sofia.uni.fmi.mjt.splitwise.client.dto.notification.GroupNotification;
import bg.sofia.uni.fmi.mjt.splitwise.client.dto.notification.Notification;
import bg.sofia.uni.fmi.mjt.splitwise.client.dto.response.NotificationResponse;
import bg.sofia.uni.fmi.mjt.splitwise.client.dto.response.PaymentsResponse;
import bg.sofia.uni.fmi.mjt.splitwise.client.dto.response.Response;
import bg.sofia.uni.fmi.mjt.splitwise.client.io.UserInterface;
import bg.sofia.uni.fmi.mjt.splitwise.client.session.UserSession;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CommandExecutor {
    public static final String NEED_LOGIN_MESSAGE = "You need to be logged-in to perform this action";
    public static final String ALREADY_LOGGED_IN_MESSAGE =
        "You are already logged-in. Type " + CommandName.LOG_OUT.userCommand +
            " to log out and then try again.";
    public static final String NO_PAYMENTS_MESSAGE = "You have not payed anything so far";
    public static final String NO_NOTIFICATIONS_MESSAGE = "No notifications to show since your last login!";
    public static final String FRIENDS = "Friends:";
    public static final String GROUPS = "Groups:";

    private UserInterface ui;
    private Client httpClient;

    private Gson gson;

    private CommandValidator validator;

    public CommandExecutor(UserInterface ui, Client httpClient, CommandValidator validator) {
        this.ui = ui;
        this.httpClient = httpClient;

        this.validator = validator;

        gson = new Gson();
    }

    public void execute(Command command) {
        if (command.commandName().equals(CommandName.HELP)) {
            printHelpMenu();
            return;
        }

        try {
            if (!UserSession.isLoggedIn()) {
                switch (command.commandName()) {
                    case SIGN_UP -> signUp(command);
                    case LOGIN -> login(command);
                    default -> ui.writeError(NEED_LOGIN_MESSAGE);
                }
            } else {
                switch (command.commandName()) {
                    case SPLIT, SPLIT_GROUP -> split(command);
                    case ADD_FRIEND -> addFriend(command);
                    case RECEIVE, RECEIVE_GROUP -> receive(command);
                    case PAYMENTS -> showPayments(command);
                    case GET_STATUS -> showStatus(command);
                    case CREATE_GROUP -> createGroup(command);
                    case LOG_OUT -> logOut();
                    default -> ui.writeError(ALREADY_LOGGED_IN_MESSAGE);
                }
            }
        } catch (Exception e) {
            ui.writeError("Something went wrong. Probably the server is down. Try restarting the program");
        }
    }

    private void signUp(Command command) {
        if (validator.validateSignUp(command)) {
            Response response = gson.fromJson(httpClient.sendRequest(command), Response.class);

            if (response.isOk()) {
                UserSession.logIn(command.arguments()[0]);
                ui.write(response.getResponse());
            } else {
                ui.writeError(response.getResponse());
            }
        }
    }

    private void printHelpMenu() {
        ui.write(CommandName.QUIT.userCommand);
        if (!UserSession.isLoggedIn()) {
            ui.write(CommandName.SIGN_UP.userCommand + " <username> <password> <first_name> <last_name>");
            ui.write(CommandName.LOGIN.userCommand + " <username> <password>");
            return;
        }

        ui.write(CommandName.ADD_FRIEND.userCommand + " <username>");
        ui.write(CommandName.CREATE_GROUP.userCommand + " <group_name> <username> <username> ... <username>");
        ui.write(CommandName.SPLIT.userCommand + " <amount> <username> <reason_for_payment>");
        ui.write(CommandName.SPLIT_GROUP.userCommand +
            " <amount> <group_name> <reason_for_payment> //split money between all members in a group");
        ui.write(CommandName.GET_STATUS.userCommand);
        ui.write(CommandName.RECEIVE.userCommand + " <amount> <from> //mark money as received");
        ui.write(CommandName.RECEIVE_GROUP.userCommand +
            " <amount> <group_name> <from> //mark money as received from a friend in a group");
        ui.write(CommandName.PAYMENTS.userCommand + " //see all payments made by you");
        ui.write(CommandName.LOG_OUT.userCommand);
    }

    private void login(Command command) {
        if (!validator.validateArgumentsCount(command)) {
            return;
        }

        String responseJson = httpClient.sendRequest(command);
        Response response = gson.fromJson(responseJson, Response.class);
        if (!response.isOk()) {
            ui.writeError(response.getResponse());
            return;
        }

        UserSession.logIn(command.arguments()[0]);

        ui.write(response.getResponse());

        NotificationResponse nResponse = gson.fromJson(responseJson, NotificationResponse.class);
        if (nResponse.getFriendsNotifications().isEmpty() && nResponse.getGroupNotifications().isEmpty()) {
            ui.write(NO_NOTIFICATIONS_MESSAGE);
        } else {
            printNotifications(nResponse);
        }
    }

    private void addFriend(Command command) {
        if (validator.validateArgumentsCount(command)) {
            printSimpleResponse(command);
        }
    }

    private void split(Command command) {
        if (validator.validateSplit(command)) {
            printSimpleResponse(command);
        }
    }

    private void receive(Command command) {
        if (validator.validateReceive(command)) {
            printSimpleResponse(command);
        }
    }

    private void createGroup(Command command) {
        if (validator.validateCreateGroup(command)) {
            printSimpleResponse(command);
        }
    }

    private void showPayments(Command command) {
        if (command.arguments().length > 0) {
            ui.writeError(CommandValidator.TOO_MANY_ARGUMENTS_MESSAGE);
            return;
        }

        command.addUsernameInFront(UserSession.getLoggedInUser());

        String responseJson = httpClient.sendRequest(command);
        Response response = gson.fromJson(responseJson, Response.class);
        if (!response.isOk()) {
            ui.writeError(response.getResponse());
            return;
        }

        PaymentsResponse paymentsResponse = gson.fromJson(httpClient.sendRequest(command), PaymentsResponse.class);
        List<Payment> payments = paymentsResponse.getPayments();
        if (payments.isEmpty()) {
            ui.write(NO_PAYMENTS_MESSAGE);
        }

        for (Payment p : payments) {
            ui.write(p.toString());
        }
    }

    private void showStatus(Command command) {
        if (command.arguments().length > 0) {
            ui.writeError(CommandValidator.TOO_MANY_ARGUMENTS_MESSAGE);
            return;
        }

        printSimpleResponse(command);
    }

    private void logOut() {
        UserSession.logOut();
        ui.write("Logged out");
    }

    private void printSimpleResponse(Command command) {
        command.addUsernameInFront(UserSession.getLoggedInUser());

        Response response = gson.fromJson(httpClient.sendRequest(command), Response.class);

        if (response.isOk()) {
            ui.write(response.getResponse());
        } else {
            ui.writeError(response.getResponse());
        }
    }

    private void printNotifications(NotificationResponse response) {
        List<Notification> friendsNotifications = response.getFriendsNotifications();

        ui.write("Notifications:");

        if (!friendsNotifications.isEmpty()) {
            ui.write(FRIENDS);
            for (Notification n : friendsNotifications) {
                ui.write(n.getText());
            }
        }

        Map<String, List<GroupNotification>> groupNotifications = new HashMap<>();

        for (GroupNotification gn : response.getGroupNotifications()) {
            groupNotifications.putIfAbsent(gn.getGroup(), new LinkedList<>());
            groupNotifications.get(gn.getGroup()).add(gn);
        }

        if (!groupNotifications.isEmpty()) {
            ui.write(GROUPS);
            for (Map.Entry<String, List<GroupNotification>> entry : groupNotifications.entrySet()) {
                ui.write(entry.getKey() + ": ");
                for (GroupNotification gn : entry.getValue()) {
                    ui.write(gn.getText());
                }
            }
        }
    }
}