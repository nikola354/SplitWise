package bg.sofia.uni.fmi.mjt.splitwise.client.dto;

import java.time.LocalDateTime;
import java.util.Set;

public class Payment {
    private String issuer;
    private double amount;
    private String reason;
    private Set<String> splitWith;

    public Payment(String issuer, double amount, String reason, Set<String> splitWith) {
        this.issuer = issuer;
        this.amount = amount;
        this.reason = reason;
        this.splitWith = splitWith;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("[" + reason + "] payed " + amount + " and have split with [");

        for (String user : splitWith) {
            result.append(user).append(", ");
        }
        result.replace(result.length() - 2, result.length(), "");
        result.append("]");

        return result.toString();
    }
}
