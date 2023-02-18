package bg.sofia.uni.fmi.mjt.splitwise.server.logic.notification;

import bg.sofia.uni.fmi.mjt.splitwise.server.logic.user.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.validation.DoubleValidator;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

public class GroupNotification extends Notification implements Serializable {
    protected static final String GROUP_SPLIT = "%s has split %.2f for [%s] in group [%s]" ;

    @Serial
    private static final long serialVersionUID = 1851922253419700158L;
    private String group;

    private GroupNotification(String group, String text) {
        super(text);
        this.group = group;
    }

    public static GroupNotification ofCreatingGroup(String group, User creator) {
        return new GroupNotification(group, creator + " added you to group [" + group + "]");
    }

    public static GroupNotification ofSplitting(String group, User actor, double amount, String reason) {
        return new GroupNotification(group, GROUP_SPLIT.formatted(actor, amount, reason, group));
    }

    public static GroupNotification ofReceiving(String group, User actor, double amount) {
        return new GroupNotification(group, RECEIVE.formatted(actor, amount));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof GroupNotification that)) return false;
        if (!super.equals(o)) return false;

        return Objects.equals(group, that.group);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (group != null ? group.hashCode() : 0);
        return result;
    }
}
