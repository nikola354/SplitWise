package bg.sofia.uni.fmi.mjt.splitwise.server.logic.friend;

import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.ReceiveException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.validation.DoubleValidator;

import java.io.Serial;
import java.io.Serializable;

public class Friendship implements Serializable {
    @Serial
    private static final long serialVersionUID = -4890959945759726885L;
    private String left;
    private String right;
    private double leftOwes; //if positive, left must give money to right; if negative -> left waits to receive money

    public Friendship(String left, String right) {
        this.left = left;
        this.right = right;

        leftOwes = 0;
    }

    public String getLeft() {
        return left;
    }

    public String getRight() {
        return right;
    }

    public void lend(String lender, double amount) {
        if (lender.equals(left)) {
            leftOwes -= amount;
        } else if (lender.equals(right)) {
            leftOwes += amount;
        } else {
            throw new IllegalArgumentException("The lender is not part of this friendship");
        }

        DoubleValidator.round(leftOwes, 2);
    }

    public void receive(String receiver, double amount) throws ReceiveException {
        if (Math.abs(leftOwes) < amount) {
            throw new ReceiveException(
                "You want to mark that you received more money than you are owed. Operation canceled");
        }

        if (receiver.equals(left)) {
            if (leftOwes >= 0) {
                throw new ReceiveException(
                    "Your friend [" + right + "] does not owe you money. You cannot mark money as received");
            }

            leftOwes += amount;
        } else if (receiver.equals(right)) {
            if (leftOwes <= 0) {
                throw new ReceiveException(
                    "Your friend [" + left + "] does not owe you money. You cannot mark money as received");
            }

            leftOwes -= amount;
        } else {
            throw new IllegalArgumentException("The lender is not part of this friendship");
        }

        DoubleValidator.round(leftOwes, 2);
    }

    public double getUserOwes(String username) {
        if (username.equals(left)) {
            return leftOwes;
        } else if (username.equals(right)) {
            return -1 * leftOwes;
        } else {
            throw new IllegalArgumentException("The user is not part of this friendship");
        }
    }

    public String getStatus(String user) {
        DoubleValidator.round(leftOwes, 2);
        if (leftOwes == 0) {
            return "";
        }

        if (user.equals(left)) {
            if (leftOwes > 0) {
                return right + ": You owe " + leftOwes + " BGN";
            }
            return right + ": Owes you " + (-leftOwes) + " BGN";

        } else if (user.equals(right)) {
            if (leftOwes > 0) {
                return left + ": Owes you " + leftOwes + " BGN";
            }

            return left + ": You owe " + (-leftOwes) + " BGN";
        } else {
            throw new IllegalArgumentException("The user is not part of this friendship");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Friendship that)) return false;

        if (Double.compare(that.leftOwes, leftOwes) != 0) return false;
        if (getLeft() != null ? !getLeft().equals(that.getLeft()) : that.getLeft() != null) return false;
        return getRight() != null ? getRight().equals(that.getRight()) : that.getRight() == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = getLeft() != null ? getLeft().hashCode() : 0;
        result = 31 * result + (getRight() != null ? getRight().hashCode() : 0);
        temp = Double.doubleToLongBits(leftOwes);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}