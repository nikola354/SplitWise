package bg.sofia.uni.fmi.mjt.splitwise.server.logic.friend;

import bg.sofia.uni.fmi.mjt.splitwise.server.logic.calculation.Splitter;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.GroupException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.ReceiveException;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.exceptions.SplitException;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Group implements Serializable {
    @Serial
    private static final long serialVersionUID = 4237572493374807466L;
    private String name;
    Map<String, FriendsList> friendsLists; //everyone has a friends-list filled with everyone else

    public Group(String name, String... members) throws GroupException {
        this.name = name;

        friendsLists = new HashMap<>();
        addMembersToFriendsLists(members);
    }

    public String getName() {
        return name;
    }

    public Map<String, FriendsList> getFriendsLists() {
        return Collections.unmodifiableMap(friendsLists);
    }

    public void split(String payer, double amount) throws SplitException {
        if (!friendsLists.containsKey(payer)) {
            throw new IllegalArgumentException("The payer of this expense is not in this group");
        }

        Map<Double, Integer> shares = Splitter.split(amount, friendsLists.size());

        Collection<Friendship> friendships = friendsLists.get(payer).getFriendships().values();
        Iterator<Friendship> iterator = friendships.iterator();

        for (Map.Entry<Double, Integer> share : shares.entrySet()) {
            for (int i = 0; i < share.getValue(); i++) {
                if (!iterator.hasNext()) { //the share values are one more than the friends (the payer also has a share)
                    return;
                }

                iterator.next().lend(payer, share.getKey());
            }
        }
    }

    public void receive(String receiver, double amount, String sender) throws ReceiveException {
        if (!friendsLists.containsKey(receiver) || !friendsLists.containsKey(sender)) {
            throw new IllegalArgumentException("The receiver or sender are not in this group");
        }

        friendsLists.get(receiver).receiveFrom(sender, amount);
    }

    public boolean hasMember(String username) {
        return friendsLists.containsKey(username);
    }

    public Set<String> getMembers() {
        return friendsLists.keySet();
    }

    private void addMembersToFriendsLists(String... members) throws GroupException {
        for (String member : members) {
            if (friendsLists.containsKey(member)) {
                throw new GroupException("There are 2 identical usernames in list of group members");
            }

            friendsLists.put(member, new FriendsList(member));
        }

        for (int i = 0; i < members.length; i++) {
            for (int j = i + 1; j < members.length; j++) {
                Friendship fr = new Friendship(members[i], members[j]);
                friendsLists.get(members[i]).addFriendship(fr);
                friendsLists.get(members[j]).addFriendship(fr); //2 literals to one object
            }
        }
    }

    public String getStatusFor(String user) throws GroupException {
        if (!friendsLists.containsKey(user)) {
            throw new GroupException("The user is not part of this group");
        }

        return name + ":" + System.lineSeparator() + friendsLists.get(user).getStatus();
    }

    public double leftOwesToRight(String left, String right) throws GroupException {
        if (!hasMember(left) || !hasMember(right)) {
            throw new GroupException("The users are not part of this group");
        }

        return friendsLists.get(left).getFriendships().get(right).getUserOwes(left);
    }
}
