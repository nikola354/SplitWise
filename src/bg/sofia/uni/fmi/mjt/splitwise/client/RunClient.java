package bg.sofia.uni.fmi.mjt.splitwise.client;

import bg.sofia.uni.fmi.mjt.splitwise.client.command.Command;
import bg.sofia.uni.fmi.mjt.splitwise.client.command.CommandCreator;
import bg.sofia.uni.fmi.mjt.splitwise.client.command.CommandExecutor;
import bg.sofia.uni.fmi.mjt.splitwise.client.command.CommandName;
import bg.sofia.uni.fmi.mjt.splitwise.client.command.CommandValidator;
import bg.sofia.uni.fmi.mjt.splitwise.client.exception.ServerNotWorkingException;
import bg.sofia.uni.fmi.mjt.splitwise.client.exception.UnknownCommandException;
import bg.sofia.uni.fmi.mjt.splitwise.client.io.ConsoleUI;
import bg.sofia.uni.fmi.mjt.splitwise.client.io.UserInterface;

public class RunClient {
    public static void main(String[] args) {
        UserInterface ui = new ConsoleUI();

        Client client = new Client();
        try {
            client.run();
        } catch (ServerNotWorkingException e) {
            ui.writeError(e.getMessage());
            ui.writeError("First start the server and then this program");
            return;
        }

        CommandValidator validator = new CommandValidator(ui);
        CommandExecutor commandExecutor = new CommandExecutor(ui, client, validator);

        CommandCreator commandCreator = new CommandCreator(ui);

        ui.write("Welcome to SplitWise! Type help to see the available commands: ");

        while (true) {
            Command command;
            try {
                command = commandCreator.readCommand(); //reads command from the user input and validate it
            } catch (UnknownCommandException e) {
                ui.writeError(e.getMessage());
                continue;
            }

            if (command.commandName().equals(CommandName.QUIT)) {
                client.stop();
                return;
            }

            commandExecutor.execute(command);
        }
    }
}
