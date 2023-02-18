package bg.sofia.uni.fmi.mjt.splitwise.server.logic.payment;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

public record Payment(String issuer, double amount, String reason, Set<String> splitWith)  implements Serializable {
}
