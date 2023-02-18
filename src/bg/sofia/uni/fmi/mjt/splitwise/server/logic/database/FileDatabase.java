package bg.sofia.uni.fmi.mjt.splitwise.server.logic.database;

import bg.sofia.uni.fmi.mjt.splitwise.server.logic.friend.FriendsList;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.friend.Group;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.notification.GroupNotification;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.notification.Notification;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.payment.Payment;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.user.User;
import bg.sofia.uni.fmi.mjt.splitwise.server.logic.user.UserPersonal;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FileDatabase implements Database {
    private static final String BIN_ENDING = ".bin";

    private static final String PAYMENTS_FILE = "payments.bin";
    private static final String FRIENDS_NOTIFICATIONS_FILE = "friends_notifications.bin";
    private static final String GROUP_NOTIFICATIONS_FILE = "group_notifications.bin";
    private static final String FRIENDS_LIST_FILE = "friends_list.bin";

    private static final String USERS_DIRECTORY_NAME = "users";
    private static final String GROUPS_DIRECTORY_NAME = "groups";
    private static final String USERS_FILE_NAME = "users.bin";

    private final Path mainDir;
    private final Path usersDir;
    private final Path groupsDir;
    private final Path usersFile;

    public FileDatabase(String mainDir) {
        this.mainDir = Path.of(mainDir);
        usersDir = Path.of(this.mainDir.toString(), USERS_DIRECTORY_NAME);
        groupsDir = Path.of(this.mainDir.toString(), GROUPS_DIRECTORY_NAME);
        usersFile = Path.of(usersDir.toString(), USERS_FILE_NAME);

        try {
            if (!Files.exists(groupsDir)) {
                Files.createDirectories(groupsDir);
            }

            if (!Files.exists(usersDir)) {
                Files.createDirectories(usersDir);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create directory", e);
        }
    }

    @Override
    public Map<String, User> loadUsers() {
        if (!Files.exists(usersFile)) {
            return new HashMap<>();
        }

        Map<String, User> result = new HashMap<>();
        try (var objectInputStream = new ObjectInputStream(new FileInputStream(usersFile.toFile()))) {
            Object userPersonalObject;

            while ((userPersonalObject = objectInputStream.readObject()) != null) {
                UserPersonal userPersonal = (UserPersonal) userPersonalObject;

                List<Notification> notifications = loadFriendsNotifications(userPersonal.getUsername());
                List<GroupNotification> groupNotifications = loadGroupNotifications(userPersonal.getUsername());
                List<Payment> payments = loadPayments(userPersonal.getUsername());
                FriendsList friendsList = loadFriendsList(userPersonal.getUsername());

                result.put(userPersonal.getUsername(),
                    new User(userPersonal, notifications, groupNotifications, friendsList, payments));
            }
        } catch (EOFException e) {
            //EMPTY BODY
        } catch (IOException e) {
            throw new UncheckedIOException("A problem occurred while reading from a file", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("The class was not found ", e);
        }

        return result;
    }

    @Override
    public void addUser(User user) {
        writeOrAppendObjectToFile(usersFile, user.getPersonal());

        createUserDirectory(user);
    }

    @Override
    public void addPayment(String username, Payment payment) {
        Path userDirectory = Path.of(usersDir.toString(), username);
        if (!Files.exists(userDirectory)) {
            throw new IllegalArgumentException(
                "The user with username [%s] is not in the database".formatted(username));
        }

        Path paymentsFile = Path.of(userDirectory.toString(), PAYMENTS_FILE);

        writeOrAppendObjectToFile(paymentsFile, payment);
    }

    @Override
    public void addFriendNotification(String username, Notification notification) {
        Path userDirectory = Path.of(usersDir.toString(), username);
        if (!Files.exists(userDirectory)) {
            throw new IllegalArgumentException(
                "The user with username [%s] is not in the database".formatted(username));
        }

        Path notificationsFile = Path.of(userDirectory.toString(), FRIENDS_NOTIFICATIONS_FILE);

        writeOrAppendObjectToFile(notificationsFile, notification);
    }

    @Override
    public void addGroupNotification(String username, GroupNotification groupNotification) {
        Path userDirectory = Path.of(usersDir.toString(), username);
        if (!Files.exists(userDirectory)) {
            throw new IllegalArgumentException(
                "The user with username [%s] is not in the database".formatted(username));
        }

        Path gNotificationsFile = Path.of(userDirectory.toString(), GROUP_NOTIFICATIONS_FILE);

        writeOrAppendObjectToFile(gNotificationsFile, groupNotification);
    }

    @Override
    public void updateFriendsList(User user) {
        Path userDirectory = Path.of(usersDir.toString(), user.getUsername());
        if (!Files.exists(userDirectory)) {
            throw new IllegalArgumentException(
                "The user with username [%s] is not in the database".formatted(user.getUsername()));
        }

        Path friendsListsFile = Path.of(userDirectory.toString(), FRIENDS_LIST_FILE);

        writeObjectToFile(friendsListsFile, user.getFriendsList());
    }

    @Override
    public void clearNotifications(String username) {
        Path userDirectory = Path.of(usersDir.toString(), username);
        if (!Files.exists(userDirectory)) {
            throw new IllegalArgumentException(
                "The user with username [%s] is not in the database".formatted(username));
        }

        Path notificationsFile = Path.of(userDirectory.toString(), FRIENDS_NOTIFICATIONS_FILE);
        try {
            Files.deleteIfExists(notificationsFile);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to delete file", e);
        }

        Path groupNotificationsFile = Path.of(userDirectory.toString(), GROUP_NOTIFICATIONS_FILE);
        try {
            Files.deleteIfExists(groupNotificationsFile);
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to delete file", e);
        }
    }

    @Override
    public void updateGroup(Group group) {
        Path groupFile = Path.of(groupsDir.toString(), group.getName() + BIN_ENDING);

        writeObjectToFile(groupFile, group);
    }

    @Override
    public Map<String, Group> loadGroups() {
        File[] groupFiles = new File(groupsDir.toString()).listFiles();

        if (groupFiles == null) {
            return new HashMap<>();
        }

        Map<String, Group> result = new HashMap<>();
        for (File file : groupFiles) {
            try (var objectInputStream = new ObjectInputStream(new FileInputStream(file))) {
                Object obj = objectInputStream.readObject();

                if (obj == null) {
                    continue;
                }

                Group group = (Group) obj;
                result.put(group.getName(), group);
            } catch (EOFException e) {
                //EMPTY BODY
            } catch (IOException e) {
                throw new UncheckedIOException("A problem occurred while reading from a file", e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("The class was not found ", e);
            }
        }

        return result;
    }

    private List<Notification> loadFriendsNotifications(String username) {
        Path pathOfFile = Path.of(usersDir.toString(), username, FRIENDS_NOTIFICATIONS_FILE);
        if (!Files.exists(pathOfFile)) {
            return new LinkedList<>();
        }

        List<Notification> result = new LinkedList<>();
        try (var objectInputStream = new ObjectInputStream(new FileInputStream(pathOfFile.toFile()))) {
            Object notificationObject;

            while ((notificationObject = objectInputStream.readObject()) != null) {
                Notification notification = (Notification) notificationObject;
                result.add(notification);
            }
        } catch (EOFException e) {
            //EMPTY BODY
        } catch (IOException e) {
            throw new UncheckedIOException("A problem occurred while reading from a file", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("The class was not found ", e);
        }

        return result;
    }

    private List<GroupNotification> loadGroupNotifications(String username) {
        Path pathOfFile = Path.of(usersDir.toString(), username, GROUP_NOTIFICATIONS_FILE);
        if (!Files.exists(pathOfFile)) {
            return new LinkedList<>();
        }

        List<GroupNotification> result = new LinkedList<>();
        try (var objectInputStream = new ObjectInputStream(new FileInputStream(pathOfFile.toFile()))) {
            Object gNotificationObject;

            while ((gNotificationObject = objectInputStream.readObject()) != null) {
                GroupNotification groupNotification = (GroupNotification) gNotificationObject;
                result.add(groupNotification);
            }
        } catch (EOFException e) {
            //EMPTY BODY
        } catch (IOException e) {
            throw new UncheckedIOException("A problem occurred while reading from a file", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("The class was not found ", e);
        }

        return result;
    }

    private FriendsList loadFriendsList(String username) {
        Path pathOfFile = Path.of(usersDir.toString(), username, FRIENDS_LIST_FILE);
        if (!Files.exists(pathOfFile)) {
            return new FriendsList(username);
        }

        FriendsList result = new FriendsList(username);
        try (var objectInputStream = new ObjectInputStream(new FileInputStream(pathOfFile.toFile()))) {
            Object obj = objectInputStream.readObject();

            if (obj == null) {
                return new FriendsList(username);
            }

            result = (FriendsList) obj;
        } catch (EOFException e) {
            //EMPTY BODY
        } catch (IOException e) {
            throw new UncheckedIOException("A problem occurred while reading from a file", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("The class was not found ", e);
        }

        return result;
    }

    private List<Payment> loadPayments(String username) {
        Path pathOfFile = Path.of(usersDir.toString(), username, PAYMENTS_FILE);
        if (!Files.exists(pathOfFile)) {
            return new LinkedList<>();
        }

        List<Payment> result = new LinkedList<>();
        try (var objectInputStream = new ObjectInputStream(new FileInputStream(pathOfFile.toFile()))) {
            Object paymentObject;

            while ((paymentObject = objectInputStream.readObject()) != null) {
                Payment payment = (Payment) paymentObject;
                result.add(payment);
            }
        } catch (EOFException e) {
            //EMPTY BODY
        } catch (IOException e) {
            throw new UncheckedIOException("A problem occurred while reading from a file", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("The class was not found ", e);
        }

        return result;
    }

    private void createUserDirectory(User user) {
        Path newDirectoryPath = Path.of(usersDir.toString(), user.getUsername());

        if (Files.exists(newDirectoryPath)) {
            throw new IllegalArgumentException("User with this username already exists");
        }

        try {
            Files.createDirectory(newDirectoryPath);
        } catch (IOException e) {
            throw new UncheckedIOException("A problem occurred while creating a directory for the specific user", e);
        }
    }

    private void writeOrAppendObjectToFile(Path filePath, Object obj) {
        if (!Files.exists(filePath)) {
            try {
                Files.createFile(filePath);
            } catch (IOException e) {
                throw new UncheckedIOException("Something went wrong while creating a file", e);
            }

            writeObjectToFile(filePath, obj);
        } else { //this is not the first payment
            //We need MyObjectOutputStream because ObjectOutputStream sets headers every time you open the file
            try (var objectOutputStream = new MyObjectOutputStream(new FileOutputStream(filePath.toFile(), true))) {
                objectOutputStream.writeObject(obj);
                objectOutputStream.flush();
            } catch (IOException e) {
                throw new UncheckedIOException("A problem occurred while writing to file", e);
            }
        }
    }

    private void writeObjectToFile(Path filePath, Object obj) {
        try (var objectOutputStream = new ObjectOutputStream(Files.newOutputStream(filePath))) {
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
        } catch (IOException e) {
            throw new UncheckedIOException("A problem occurred while writing to file", e);
        }
    }
}
