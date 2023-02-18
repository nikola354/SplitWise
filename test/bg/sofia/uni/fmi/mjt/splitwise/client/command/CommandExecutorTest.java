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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;

public class CommandExecutorTest {
    UserInterface ui = Mockito.mock(UserInterface.class);
    Client httpClient = Mockito.mock(Client.class);
    CommandValidator validator = Mockito.mock(CommandValidator.class);

    CommandExecutor commandExecutor;

    static String[] arguments;

    static String okResponseJson;
    static String okResponseMessage;
    static String badResponseJson;
    static String badResponseMessage;

    static Gson gson;

    @BeforeAll
    static void setArguments() {
        arguments = new String[] {"some", "unuseful", "arguments"}; //do not matter because I have mock for validation
    }

    @BeforeAll
    static void setResponses() {
        gson = new Gson();

        okResponseMessage = "Operation executed successfully";
        Response okResponse = new Response(true, okResponseMessage);
        okResponseJson = gson.toJson(okResponse);

        badResponseMessage = "Something went wrong";
        Response badResponse = new Response(false, badResponseMessage);
        badResponseJson = gson.toJson(badResponse);
    }

    @BeforeEach
    void setCommandExecutor() {
        commandExecutor = new CommandExecutor(ui, httpClient, validator);
    }

    @BeforeEach
    void logIn() {
        if (!UserSession.isLoggedIn()) {
            UserSession.logIn("nikola123");
        }
    }

    @Test
    void testExecuteAddFriendNotLoggedIn() {
        UserSession.logOut();

        Command command = new Command(CommandName.ADD_FRIEND, arguments);
        commandExecutor.execute(command);

        Mockito.verify(ui).writeError(CommandExecutor.NEED_LOGIN_MESSAGE);
    }

    @Test
    void testExecuteAddFriendValidationFailed() {
        Command command = new Command(CommandName.ADD_FRIEND, arguments);

        Mockito.when(validator.validateArgumentsCount(command)).thenReturn(false);

        commandExecutor.execute(command);

        Mockito.verify(ui, never()).writeError(anyString());
        Mockito.verify(ui, never()).write(anyString());
    }

    @Test
    void testExecuteAddFriendOkResponse() {
        Command command = new Command(CommandName.ADD_FRIEND, arguments);

        Mockito.when(validator.validateArgumentsCount(command)).thenReturn(true);
        Mockito.when(httpClient.sendRequest(command)).thenReturn(okResponseJson);

        commandExecutor.execute(command);

        Mockito.verify(ui, never()).writeError(anyString());
        Mockito.verify(ui).write(okResponseMessage);
    }

    @Test
    void testExecuteAddFriendBadResponse() {
        Command command = new Command(CommandName.ADD_FRIEND, arguments);

        Mockito.when(validator.validateArgumentsCount(command)).thenReturn(true);
        Mockito.when(httpClient.sendRequest(command)).thenReturn(badResponseJson);

        commandExecutor.execute(command);

        Mockito.verify(ui, never()).write(anyString());
        Mockito.verify(ui).writeError(badResponseMessage);
    }

    @Test
    void testExecuteSplitNotLoggedIn() {
        UserSession.logOut();

        Command command = new Command(CommandName.SPLIT, arguments);
        commandExecutor.execute(command);

        Mockito.verify(ui).writeError(CommandExecutor.NEED_LOGIN_MESSAGE);
    }

    @Test
    void testExecuteSplitValidationFailed() {
        Command command = new Command(CommandName.SPLIT, arguments);

        Mockito.when(validator.validateSplit(command)).thenReturn(false);

        commandExecutor.execute(command);

        Mockito.verify(ui, never()).writeError(anyString());
        Mockito.verify(ui, never()).write(anyString());
    }

    @Test
    void testExecuteSplitOkResponse() {
        Command command = new Command(CommandName.SPLIT, arguments);

        Mockito.when(validator.validateSplit(command)).thenReturn(true);
        Mockito.when(httpClient.sendRequest(command)).thenReturn(okResponseJson);

        commandExecutor.execute(command);

        Mockito.verify(ui, never()).writeError(anyString());
        Mockito.verify(ui).write(okResponseMessage);
    }

    @Test
    void testExecuteSplitBadResponse() {
        Command command = new Command(CommandName.SPLIT, arguments);

        Mockito.when(validator.validateSplit(command)).thenReturn(true);
        Mockito.when(httpClient.sendRequest(command)).thenReturn(badResponseJson);

        commandExecutor.execute(command);

        Mockito.verify(ui, never()).write(anyString());
        Mockito.verify(ui).writeError(badResponseMessage);
    }

    @Test
    void testExecuteReceiveNotLoggedIn() {
        UserSession.logOut();

        Command command = new Command(CommandName.RECEIVE, arguments);
        commandExecutor.execute(command);

        Mockito.verify(ui).writeError(CommandExecutor.NEED_LOGIN_MESSAGE);
    }

    @Test
    void testExecuteReceiveValidationFailed() {
        Command command = new Command(CommandName.RECEIVE, arguments);

        Mockito.when(validator.validateReceive(command)).thenReturn(false);

        commandExecutor.execute(command);

        Mockito.verify(ui, never()).writeError(anyString());
        Mockito.verify(ui, never()).write(anyString());
    }

    @Test
    void testExecuteReceiveOkResponse() {
        Command command = new Command(CommandName.RECEIVE, arguments);

        Mockito.when(validator.validateReceive(command)).thenReturn(true);
        Mockito.when(httpClient.sendRequest(command)).thenReturn(okResponseJson);

        commandExecutor.execute(command);

        Mockito.verify(ui, never()).writeError(anyString());
        Mockito.verify(ui).write(okResponseMessage);
    }

    @Test
    void testExecuteReceiveBadResponse() {
        Command command = new Command(CommandName.RECEIVE, arguments);

        Mockito.when(validator.validateReceive(command)).thenReturn(true);
        Mockito.when(httpClient.sendRequest(command)).thenReturn(badResponseJson);

        commandExecutor.execute(command);

        Mockito.verify(ui, never()).write(anyString());
        Mockito.verify(ui).writeError(badResponseMessage);
    }

    @Test
    void testExecuteCreateGroupNotLoggedIn() {
        UserSession.logOut();

        Command command = new Command(CommandName.CREATE_GROUP, arguments);
        commandExecutor.execute(command);

        Mockito.verify(ui).writeError(CommandExecutor.NEED_LOGIN_MESSAGE);
    }

    @Test
    void testExecuteCreateGroupValidationFailed() {
        Command command = new Command(CommandName.CREATE_GROUP, arguments);

        Mockito.when(validator.validateCreateGroup(command)).thenReturn(false);

        commandExecutor.execute(command);

        Mockito.verify(ui, never()).writeError(anyString());
        Mockito.verify(ui, never()).write(anyString());
    }

    @Test
    void testExecuteCreateGroupOkResponse() {
        Command command = new Command(CommandName.CREATE_GROUP, arguments);

        Mockito.when(validator.validateCreateGroup(command)).thenReturn(true);
        Mockito.when(httpClient.sendRequest(command)).thenReturn(okResponseJson);

        commandExecutor.execute(command);

        Mockito.verify(ui, never()).writeError(anyString());
        Mockito.verify(ui).write(okResponseMessage);
    }

    @Test
    void testExecuteCreateGroupBadResponse() {
        Command command = new Command(CommandName.CREATE_GROUP, arguments);

        Mockito.when(validator.validateCreateGroup(command)).thenReturn(true);
        Mockito.when(httpClient.sendRequest(command)).thenReturn(badResponseJson);

        commandExecutor.execute(command);

        Mockito.verify(ui, never()).write(anyString());
        Mockito.verify(ui).writeError(badResponseMessage);
    }

    @Test
    void testExecutePaymentsNotLoggedIn() {
        UserSession.logOut();

        Command command = new Command(CommandName.PAYMENTS, arguments);
        commandExecutor.execute(command);

        Mockito.verify(ui).writeError(CommandExecutor.NEED_LOGIN_MESSAGE);
    }

    @Test
    void testExecutePaymentsValidationFailed() {
        Command command = new Command(CommandName.PAYMENTS, arguments);

        commandExecutor.execute(command);

        Mockito.verify(ui).writeError(CommandValidator.TOO_MANY_ARGUMENTS_MESSAGE);
        Mockito.verify(ui, never()).write(anyString());
    }

    @Test
    void testExecutePaymentsOkResponse() {
        Payment hotelPayment = new Payment("nikola", 165.54, "hotel", Set.of("friend1"));
        Payment beerPayment = new Payment("ivan", 10.2, "beer", Set.of("nikola", "peter", "neighbor"));
        List<Payment> payments = List.of(hotelPayment, beerPayment);
        String paymentsResponseJson = gson.toJson(new PaymentsResponse(true, "Successfully loaded payments", payments));

        Command command = new Command(CommandName.PAYMENTS, new String[] {});

        Mockito.when(httpClient.sendRequest(command)).thenReturn(paymentsResponseJson);
        commandExecutor.execute(command);

        Mockito.verify(ui, never()).writeError(anyString());
        Mockito.verify(ui).write(hotelPayment.toString());
        Mockito.verify(ui).write(beerPayment.toString());
    }

    @Test
    void testExecutePaymentsOkResponseNoPayments() {
        Command command = new Command(CommandName.PAYMENTS, new String[] {});

        PaymentsResponse paymentsResponse = new PaymentsResponse(true, "Success", new LinkedList<>());
        Mockito.when(httpClient.sendRequest(command)).thenReturn(gson.toJson(paymentsResponse));

        commandExecutor.execute(command);

        Mockito.verify(ui, never()).writeError(anyString());
        Mockito.verify(ui).write(CommandExecutor.NO_PAYMENTS_MESSAGE);
    }

    @Test
    void testExecutePaymentsBadResponse() {
        Command command = new Command(CommandName.PAYMENTS, new String[] {});

        Mockito.when(httpClient.sendRequest(command)).thenReturn(badResponseJson);
        commandExecutor.execute(command);

        Mockito.verify(ui, never()).write(anyString());
        Mockito.verify(ui).writeError(badResponseMessage);
    }

    @Test
    void testExecuteGetStatusNotLoggedIn() {
        UserSession.logOut();

        Command command = new Command(CommandName.GET_STATUS, arguments);
        commandExecutor.execute(command);

        Mockito.verify(ui).writeError(CommandExecutor.NEED_LOGIN_MESSAGE);
    }

    @Test
    void testExecuteGetStatusValidationFailed() {
        Command command = new Command(CommandName.GET_STATUS, arguments);

        commandExecutor.execute(command);

        Mockito.verify(ui).writeError(CommandValidator.TOO_MANY_ARGUMENTS_MESSAGE);
        Mockito.verify(ui, never()).write(anyString());
    }

    @Test
    void testExecuteGetStatusOkResponse() {
        Command command = new Command(CommandName.GET_STATUS, new String[] {});

        Mockito.when(httpClient.sendRequest(command)).thenReturn(okResponseJson);
        commandExecutor.execute(command);

        Mockito.verify(ui, never()).writeError(anyString());
        Mockito.verify(ui).write(okResponseMessage);
    }

    @Test
    void testExecuteGetStatusBadResponse() {
        Command command = new Command(CommandName.GET_STATUS, new String[] {});

        Mockito.when(httpClient.sendRequest(command)).thenReturn(badResponseJson);
        commandExecutor.execute(command);

        Mockito.verify(ui, never()).write(anyString());
        Mockito.verify(ui).writeError(badResponseMessage);
    }

    @Test
    void testExecuteLogOutWhenNotLoggedIn() {
        UserSession.logOut();

        Command command = new Command(CommandName.LOG_OUT, new String[] {});
        commandExecutor.execute(command);

        Mockito.verify(ui).writeError(CommandExecutor.NEED_LOGIN_MESSAGE);
    }

    @Test
    void testExecuteLogOut() {
        assertTrue(UserSession.isLoggedIn(), "The user must be logged-in");

        Command command = new Command(CommandName.LOG_OUT, new String[] {});
        commandExecutor.execute(command);

        assertFalse(UserSession.isLoggedIn(), "Could not log out");

        Mockito.verify(ui, never()).writeError(anyString());
    }

    //commands when not logged-in:
    @Test
    void testExecuteLogInWhenLoggedIn() {
        Command command = new Command(CommandName.LOGIN, arguments);
        commandExecutor.execute(command);

        Mockito.verify(ui).writeError(CommandExecutor.ALREADY_LOGGED_IN_MESSAGE);
    }

    @Test
    void testExecuteLogInValidationFailed() {
        UserSession.logOut();

        Command command = new Command(CommandName.LOGIN, arguments);

        Mockito.when(validator.validateArgumentsCount(command)).thenReturn(false);

        commandExecutor.execute(command);

        Mockito.verify(ui, never()).writeError(anyString());
        Mockito.verify(ui, never()).write(anyString());

        assertFalse(UserSession.isLoggedIn(), "When validation fails, the user must not be logged-in");
    }

    @Test
    void testExecuteLogInOkResponse() {
        UserSession.logOut();

        GroupNotification roommatesGroup = new GroupNotification("Ivan has split 5 BGN", "roommates");
        GroupNotification vacation =
            new GroupNotification("Peter marked 10 BGN as received from you for hotel", "vacation");
        Notification friend = new Notification("Nikola added you as a friend");

        NotificationResponse nResponse =
            new NotificationResponse(true, "Login successful", List.of(friend), List.of(roommatesGroup, vacation));

        Command command = new Command(CommandName.LOGIN, arguments);

        Mockito.when(validator.validateArgumentsCount(command)).thenReturn(true);
        Mockito.when(httpClient.sendRequest(command)).thenReturn(gson.toJson(nResponse));

        commandExecutor.execute(command);

        Mockito.verify(ui, never()).writeError(anyString());

        Mockito.verify(ui).write(CommandExecutor.FRIENDS);
        Mockito.verify(ui).write(friend.getText());
        Mockito.verify(ui).write(CommandExecutor.GROUPS);
        Mockito.verify(ui).write(roommatesGroup.getText());
        Mockito.verify(ui).write(vacation.getText());

        assertTrue(UserSession.isLoggedIn(), "User was expected to be logged in");
    }

    @Test
    void testExecuteLogInBadResponse() {
        UserSession.logOut();

        Command command = new Command(CommandName.LOGIN, arguments);

        Mockito.when(validator.validateArgumentsCount(command)).thenReturn(true);
        Mockito.when(httpClient.sendRequest(command)).thenReturn(badResponseJson);

        commandExecutor.execute(command);

        Mockito.verify(ui, never()).write(anyString());
        Mockito.verify(ui).writeError(badResponseMessage);

        assertFalse(UserSession.isLoggedIn(), "Bad response from server, the user must not be logged-in");
    }

    @Test
    void testExecuteSignUpWhenLoggedIn() {
        Command command = new Command(CommandName.SIGN_UP, arguments);
        commandExecutor.execute(command);

        Mockito.verify(ui).writeError(CommandExecutor.ALREADY_LOGGED_IN_MESSAGE);
        assertTrue(UserSession.isLoggedIn(), "Logged-in user expected");
    }

    @Test
    void testExecuteSignUpValidationFailed() {
        UserSession.logOut();
        Command command = new Command(CommandName.SIGN_UP, arguments);

        Mockito.when(validator.validateSignUp(command)).thenReturn(false);

        commandExecutor.execute(command);

        Mockito.verify(ui, never()).writeError(anyString());
        Mockito.verify(ui, never()).write(anyString());
        assertFalse(UserSession.isLoggedIn(), "Validation failed, no login expected");
    }

    @Test
    void testExecuteSignUpOkResponse() {
        UserSession.logOut();
        Command command = new Command(CommandName.SIGN_UP, arguments);

        Mockito.when(validator.validateSignUp(command)).thenReturn(true);
        Mockito.when(httpClient.sendRequest(command)).thenReturn(okResponseJson);

        commandExecutor.execute(command);

        Mockito.verify(ui, never()).writeError(anyString());
        Mockito.verify(ui).write(okResponseMessage);
        assertTrue(UserSession.isLoggedIn(), "Logged-in user expected");
    }

    @Test
    void testExecuteSignUpBadResponse() {
        UserSession.logOut();
        Command command = new Command(CommandName.SIGN_UP, arguments);

        Mockito.when(validator.validateSignUp(command)).thenReturn(true);
        Mockito.when(httpClient.sendRequest(command)).thenReturn(badResponseJson);

        commandExecutor.execute(command);

        Mockito.verify(ui, never()).write(anyString());
        Mockito.verify(ui).writeError(badResponseMessage);
        assertFalse(UserSession.isLoggedIn(), "Bad response from server, the user must not be registered");
    }
}
