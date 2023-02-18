package bg.sofia.uni.fmi.mjt.splitwise.client.command;

import bg.sofia.uni.fmi.mjt.splitwise.client.io.UserInterface;
import bg.sofia.uni.fmi.mjt.splitwise.client.validation.StringValidator;

import java.util.EnumMap;
import java.util.Map;

import static bg.sofia.uni.fmi.mjt.splitwise.client.validation.DoubleValidator.DOUBLE_INVALID;
import static bg.sofia.uni.fmi.mjt.splitwise.client.validation.DoubleValidator.getDouble;
import static bg.sofia.uni.fmi.mjt.splitwise.client.validation.DoubleValidator.isValidDouble;

public class CommandValidator {
    private static final int THIRD_ARGUMENT = 3;

    public static final String DOUBLE_MESSAGE = "You must enter a valid floating number (with up to 2 decimal places";
    public static final String NOT_ENOUGH_ARGUMENTS_MESSAGE = "Not enough arguments";
    public static final String TOO_MANY_ARGUMENTS_MESSAGE = "Too many arguments given";

    private UserInterface ui;

    private Map<CommandName, Integer> argumentsCount;

    public CommandValidator(UserInterface userInterface) {
        this.ui = userInterface;

        argumentsCount = new EnumMap<>(CommandName.class);
        argumentsCount.put(CommandName.SIGN_UP, CommandLength.SIGN_UP.length);
        argumentsCount.put(CommandName.LOGIN, CommandLength.LOGIN.length);
        argumentsCount.put(CommandName.ADD_FRIEND, CommandLength.ADD_FRIEND.length);
        argumentsCount.put(CommandName.SPLIT, CommandLength.SPLIT.length);
        argumentsCount.put(CommandName.CREATE_GROUP, CommandLength.CREATE_GROUP.length);
        argumentsCount.put(CommandName.SPLIT_GROUP, CommandLength.SPLIT_GROUP.length);
        argumentsCount.put(CommandName.GET_STATUS, CommandLength.GET_STATUS.length);
        argumentsCount.put(CommandName.RECEIVE, CommandLength.RECEIVE.length);
        argumentsCount.put(CommandName.RECEIVE_GROUP, CommandLength.RECEIVE_GROUP.length);
        argumentsCount.put(CommandName.PAYMENTS, CommandLength.PAYMENTS.length);
        argumentsCount.put(CommandName.LOG_OUT, CommandLength.LOG_OUT.length);
    }

    public boolean validateSignUp(Command command) {
        if (!checkSize(command.arguments(), argumentsCount.get(command.commandName()))) {
            return false;
        }

        boolean ok = true;
        if (!StringValidator.isValidUsername(command.arguments()[0])) {
            ui.writeError(
                StringValidator.USERNAME_VALIDATION_MESSAGE.formatted("username", StringValidator.MIN_NAME_LENGTH,
                    StringValidator.MAX_USERNAME_LENGTH));
            ok = false;
        }

        if (!StringValidator.isValidPassword(command.arguments()[1])) {
            ui.writeError(
                StringValidator.PASSWORD_VALIDATION_MESSAGE.formatted("password", StringValidator.MIN_PASS_LENGTH,
                    StringValidator.MAX_PASS_LENGTH));
            ok = false;
        }

        if (!StringValidator.areValidNames(command.arguments()[2])) {
            ui.writeError(
                StringValidator.NAME_VALIDATION_MESSAGE.formatted("first name", StringValidator.MIN_NAME_LENGTH,
                    StringValidator.MAX_NAME_LENGTH));
            ok = false;
        }

        if (!StringValidator.areValidNames(command.arguments()[THIRD_ARGUMENT])) {
            ui.writeError(
                StringValidator.NAME_VALIDATION_MESSAGE.formatted("last name", StringValidator.MIN_NAME_LENGTH,
                    StringValidator.MAX_NAME_LENGTH));
            ok = false;
        }

        return ok;
    }

    public boolean validateArgumentsCount(Command command) {
        return checkSize(command.arguments(), argumentsCount.get(command.commandName()));
    }

    public boolean validateSplit(Command command) {
        if (!checkSizeEnough(command.arguments(), argumentsCount.get(command.commandName()))) {
            return false;
        }

        double amount = getDouble(command.arguments()[0]);
        if (!checkDouble(amount)) {
            return false;
        }

        command.mergeLastArgument(argumentsCount.get(command.commandName()) - 1);

        return true;
    }

    public boolean validateReceive(Command command) {
        if (!checkSize(command.arguments(), argumentsCount.get(command.commandName()))) {
            return false;
        }

        double amount = getDouble(command.arguments()[0]);

        return checkDouble(amount);
    }

    public boolean validateCreateGroup(Command command) {
        if (!checkSizeEnough(command.arguments(), argumentsCount.get(command.commandName()))) {
            return false;
        }

        if (!StringValidator.isValidUsername(command.arguments()[0])) {
            ui.writeError(
                StringValidator.USERNAME_VALIDATION_MESSAGE.formatted("group name", StringValidator.MIN_USERNAME_LENGTH,
                    StringValidator.MAX_NAME_LENGTH));
            return false;
        }

        return true;
    }

    private boolean checkSize(String[] arguments, int desired) {
        if (arguments.length > desired) {
            ui.writeError(TOO_MANY_ARGUMENTS_MESSAGE);
            return false;
        }

        return checkSizeEnough(arguments, desired);
    }

    private boolean checkSizeEnough(String[] arguments, int desired) {
        if (arguments.length < desired) {
            ui.writeError(NOT_ENOUGH_ARGUMENTS_MESSAGE);
            return false;
        }
        return true;
    }

    private boolean checkDouble(double amount) {
        if (amount == DOUBLE_INVALID || !isValidDouble(amount)) {
            ui.writeError(DOUBLE_MESSAGE);
            return false;
        }
        return true;
    }
}