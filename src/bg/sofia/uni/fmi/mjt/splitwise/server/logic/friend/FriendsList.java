package bg.sofia.uni.fmi.mjt.splitwise.server.logic.friend;

import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.ReceiveException;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FriendsList implements Serializable {
    @Serial
    private static final long serialVersionUID = 2844610587003953092L;
    private String owner;
    private Map<String, Friendship> friendships;

    public FriendsList(String owner) {
        this.owner = owner;
        friendships = new HashMap<>();
    }

    public void addFriendship(Friendship friendship) {
        if (friendship.getLeft().equals(owner)) {
            friendships.put(friendship.getRight(), friendship);
        } else if (friendship.getRight().equals(owner)) {
            friendships.put(friendship.getLeft(), friendship);
        } else {
            throw new IllegalArgumentException("This friendship is not for this friends list");
        }
    }

    public String getOwner() {
        return owner;
    }

    public Map<String, Friendship> getFriendships() {
        return Collections.unmodifiableMap(friendships);
    }

    public boolean hasFriend(String friend) {
        return friendships.containsKey(friend);
    }

    public void lendTo(String friend, double amount) {
        if (!friendships.containsKey(friend)) {
            throw new IllegalArgumentException("There is no such friend in this friends list");
        }

        friendships.get(friend).lend(owner, amount);
    }

    public void receiveFrom(String friend, double amount) throws ReceiveException {
        if (!friendships.containsKey(friend)) {
            throw new IllegalArgumentException("There is no such friend in this friends list");
        }

        friendships.get(friend).receive(owner, amount);
    }

    public String getStatus() {
        StringBuilder sb = new StringBuilder();

        for (Friendship friendship : friendships.values()) {
            String status = friendship.getStatus(owner);

            if (!status.isEmpty()) {
                sb.append(status);
                sb.append(System.lineSeparator());
            }
        }

        if (sb.isEmpty()) {
            return "Everything is settled-up." + System.lineSeparator();
        }

        return sb.toString();
    }
}
