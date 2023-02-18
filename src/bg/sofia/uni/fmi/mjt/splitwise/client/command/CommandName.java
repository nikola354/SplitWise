package bg.sofia.uni.fmi.mjt.splitwise.client.command;

public enum CommandName {
    LOGIN("login"),
    SIGN_UP("sign-up"),
    ADD_FRIEND("add-friend"),
    CREATE_GROUP("create-group"),
    SPLIT("split"),
    SPLIT_GROUP("split-group"),
    GET_STATUS("get-status"),
    RECEIVE("receive"),
    RECEIVE_GROUP("receive-group"),
    PAYMENTS("payments"),
    HELP("help"),
    LOG_OUT("log-out"),
    QUIT("quit");

    public final String userCommand;

    CommandName(String userCommand) {
        this.userCommand = userCommand;
    }
}
