package bg.sofia.uni.fmi.mjt.splitwise.client.command;

public enum CommandLength {
    LOGIN(2),
    SIGN_UP(4),
    ADD_FRIEND(1),
    CREATE_GROUP(2),
    SPLIT(3),
    SPLIT_GROUP(3),
    GET_STATUS(0),
    RECEIVE(2),
    RECEIVE_GROUP(3),
    PAYMENTS(0),
    LOG_OUT(0);

    public final int length;

    CommandLength(int length) {
        this.length = length;
    }
}
