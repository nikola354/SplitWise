package bg.sofia.uni.fmi.mjt.splitwise.client.command;

import bg.sofia.uni.fmi.mjt.splitwise.client.exception.UnknownCommandException;
import bg.sofia.uni.fmi.mjt.splitwise.client.io.UserInterface;

import java.util.ArrayList;
import java.util.List;

public class CommandCreator {
    private UserInterface userInterface;

    public CommandCreator(UserInterface userInterface) {
        this.userInterface = userInterface;
    }

    public Command readCommand() throws UnknownCommandException {
        String input = userInterface.read();

        List<String> tokens = getCommandArguments(input);
        String[] args = tokens.subList(1, tokens.size()).toArray(new String[0]);

        CommandName commandName = getCommandName(tokens.get(0)); //get the type of command

        return new Command(commandName, args);
    }

    private static List<String> getCommandArguments(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        boolean insideQuote = false;

        for (char c : input.toCharArray()) {
            if (c == '"') {
                insideQuote = !insideQuote;
            }
            if (c == ' ' && !insideQuote) { //when space is not inside quote split
                tokens.add(sb.toString().replace("\"", "")); //token is ready, lets add it to list
                sb.delete(0, sb.length()); //and reset StringBuilder`s content
            } else {
                sb.append(c); //else add character to token
            }
        }

        //do not forget about last token that doesn't have space after it
        tokens.add(sb.toString().replace("\"", ""));

        return tokens;
    }

    private CommandName getCommandName(String userCommand) throws UnknownCommandException {
        for (CommandName commandName : CommandName.values()) {
            if (userCommand.equals(commandName.userCommand)) {
                return commandName;
            }
        }

        throw new UnknownCommandException("The command that you entered is not in the list with allowed commands");
    }
}
