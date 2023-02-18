package bg.sofia.uni.fmi.mjt.splitwise.client.command;

public class Command {
    private CommandName commandName;
    private String[] arguments;

    public Command(CommandName commandName, String[] arguments) {
        this.commandName = commandName;
        this.arguments = arguments;
    }

    public CommandName commandName() {
        return commandName;
    }

    public String[] arguments() {
        return arguments;
    }

    public void mergeLastArgument(int last) { //reason for split can be many words and must be merged into one argument
        StringBuilder lastToken = new StringBuilder(arguments[last]);
        for (int i = last + 1; i < arguments.length; i++) {
            lastToken.append(" ").append(arguments[i]);
        }
        arguments[last] = lastToken.toString();

        String[] copy = new String[last + 1];
        System.arraycopy(arguments, 0, copy, 0, last + 1);

        arguments = copy;
    }

    public void addUsernameInFront(String username) { //adds the username of the logged-in user
        String[] copy = new String[arguments.length + 1];

        copy[0] = username;

        System.arraycopy(arguments, 0, copy, 1, arguments.length);
        arguments = copy;
    }
}
