package bg.sofia.uni.fmi.mjt.splitwise.server.logic.calculation;

import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.SplitException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.validation.DoubleValidator;

import java.util.HashMap;
import java.util.Map;

public class Splitter {
    private static final double MINIMUM_SHARE = 0.01;

    public static Map<Double, Integer> split(double amount, int parts) throws SplitException {
        if (Double.compare(amount, 0.0) <= 0 || parts <= 0) {
            throw new IllegalArgumentException("The arguments must be positive");
        }

        if (Double.compare(amount / parts, MINIMUM_SHARE) < 0) {
            throw new SplitException(
                "The amount is not enough to split between the people. " +
                    "If we split it, there will be people with zero share.");
        }

        Map<Double, Integer> result = new HashMap<>();
        splitRec(amount, parts, result);
        return result;
    }
    private static void splitRec(double amount, int parts, Map<Double, Integer> result) {
        if (parts <= 0) {
            return;
        }

        double share = amount / parts;

        if (DoubleValidator.getDecimalPlaces(share) <= 2) {
            result.putIfAbsent(share, 0);
            result.put(share, result.get(share) + parts);

            return;
        }

        share = DoubleValidator.round(share, 2);

        result.putIfAbsent(share, 0);
        result.put(share, result.get(share) + 1);

        splitRec(amount - share, --parts, result);
    }
}
