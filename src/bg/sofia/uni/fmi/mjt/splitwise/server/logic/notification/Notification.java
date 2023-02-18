package bg.sofia.uni.fmi.mjt.splitwise.server.logic.notification;

import bg.sofia.uni.fmi.mjt.splitwise.server.logic.user.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.validation.DoubleValidator;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class Notification implements Serializable {
    protected static final String SPLIT = "%s has split %.2f BGN with you [%s]";
    protected static final String ADD_FRIEND = "%s added you as a friend";
    protected static final String RECEIVE = "%s approved the %.2f BGN you sent to them";
    @Serial
    private static final long serialVersionUID = -5280286477286837508L;

    private String text;

    protected Notification(String text) {
        this.text = text;
    }

    public static Notification ofAddingFriend(User actor) {
        return new Notification(ADD_FRIEND.formatted(actor));
    }

    public static Notification ofSplitting(User actor, double amount, String reason) {
        return new Notification(SPLIT.formatted(actor, amount, reason));
    }

    public static Notification ofReceiving(User actor, double amount) {
        return new Notification(RECEIVE.formatted(actor, amount));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Notification that)) return false;

        return Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return text != null ? text.hashCode() : 0;
    }
}
