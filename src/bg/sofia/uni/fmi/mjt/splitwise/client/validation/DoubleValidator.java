package bg.sofia.uni.fmi.mjt.splitwise.client.validation;

public class DoubleValidator {
    public static final double DOUBLE_INVALID = -1.0;

    public static boolean isValidDouble(double value) {
        return value > 0 && getDecimalPlaces(value) <= 2;
    }

    public static void validateDouble(double value) {
        if (!isValidDouble(value)) {
            throw new IllegalArgumentException("The double argument must be positive and with up to 2 decimal places");
        }
    }

    public static int getDecimalPlaces(double d) {
        String text = Double.toString(Math.abs(d));
        int integerPlaces = text.indexOf('.');

        return text.length() - integerPlaces - 1;
    }

    public static double round(double value, int places) {
        final int ten = 10;

        long factor = (long) Math.pow(ten, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static double getDouble(String str) {
        double amount;
        try {
            amount = Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return DOUBLE_INVALID;
        }

        return amount;
    }
}
