package bg.sofia.uni.fmi.mjt.splitwise.client.command;

import bg.sofia.uni.fmi.mjt.splitwise.client.exception.UnknownCommandException;
import bg.sofia.uni.fmi.mjt.splitwise.client.io.UserInterface;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class CommandCreatorTest {
    //I know that there is @Mock, but I get exception that ui is null if I use it
    static UserInterface ui = Mockito.mock(UserInterface.class);

    static CommandCreator creator;

    static List<String> arguments;
    static String argumentsAsString;

    @BeforeAll
    static void setUp() {
        creator = new CommandCreator(ui);

        argumentsAsString = "5 georgi beer and chips";
        arguments = new LinkedList<>(List.of("5", "georgi", "beer", "and", "chips"));
    }

    @Test
    void testReadCommandUnknown() {
        when(ui.read()).thenReturn("command that is unknown");

        assertThrows(UnknownCommandException.class, () -> creator.readCommand(),
            "Exception was expected because of unknown command");
    }

    @Test
    void testReadCommandEmptyString() {
        when(ui.read()).thenReturn("");

        assertThrows(UnknownCommandException.class, () -> creator.readCommand(),
            "Exception was expected because of empty string");
    }

    @Test
    void testReadCommandEmptyLine() {
        when(ui.read()).thenReturn(System.lineSeparator());

        assertThrows(UnknownCommandException.class, () -> creator.readCommand(),
            "Exception was expected");
    }

    @Test
    void testReadCommandUnknownLoginCommand() {
        when(ui.read()).thenReturn("logIn nikola qwerty");

        assertThrows(UnknownCommandException.class, () -> creator.readCommand(),
            "Exception was expected");
    }

    //I know that it is not good practice but there was much code duplication
    @Test
    void testReadCommandAllCommandNames() throws UnknownCommandException {
        for (CommandName commandName : CommandName.values()) {
            when(ui.read()).thenReturn(commandName.userCommand + " " + argumentsAsString);

            Command command = creator.readCommand();

            assertEquals(commandName, command.commandName(), "The name of the command is not as expected");
            assertIterableEquals(arguments, List.of(command.arguments()), "The arguments are not as expected");
        }
    }
}
