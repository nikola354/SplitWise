package bg.sofia.uni.fmi.mjt.splitwise.client.validation;

import java.util.regex.Pattern;

public class StringValidator {
    public static final String USERNAME_VALIDATION_MESSAGE =
        "The %s must contain between %d and %d alphanumeric characters and _ . It is not allowed to begin with _";

    public static final String PASSWORD_VALIDATION_MESSAGE = "The %s must be between %d and %d characters long";

    public static final String NAME_VALIDATION_MESSAGE =
        "The %s must be between %d and %d letters long. It can contain only letters";

    private static final String REGEX_ONLY_LETTERS = "[a-zA-Z]+";

    public static final int MIN_USERNAME_LENGTH = 4;
    public static final int MAX_USERNAME_LENGTH = 30;

    public static final int MIN_NAME_LENGTH = 2;
    public static final int MAX_NAME_LENGTH = 30;

    public static final int MIN_PASS_LENGTH = 6;
    public static final int MAX_PASS_LENGTH = 30;

    private static final String REGEX_USERNAME = "^[A-Za-z][A-Za-z0-9_]{" + (MIN_USERNAME_LENGTH - 1) + "," + (
        MAX_USERNAME_LENGTH - 1) + "}$";

    public static boolean areValidStrings(String... args) {
        for (String str : args) {
            if (str == null || str.isEmpty() || str.isBlank()) {
                return false;
            }
        }

        return true;
    }

    public static boolean areValidNames(String... names) {
        for (String name : names) {
            if (!Pattern.matches(REGEX_ONLY_LETTERS, name) || name.length() > MAX_NAME_LENGTH ||
                name.length() < MIN_NAME_LENGTH) {
                return false;
            }
        }

        return true;
    }

    public static boolean isValidUsername(String username) {
        return Pattern.matches(REGEX_USERNAME, username);
    }

    public static boolean isValidPassword(String password) {
        return (password.length() <= MAX_PASS_LENGTH && password.length() >= MIN_PASS_LENGTH);
    }
}
