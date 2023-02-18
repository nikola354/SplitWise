package bg.sofia.uni.fmi.mjt.splitwise.client.command;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;

public class CommandTest {
    Command command;

    @BeforeEach
    void setCommand() {
        command =
            new Command(CommandName.SPLIT, new String[] {"4.50", "nikola", "reason", "for", "payment", "is", "beer"});
    }

    @Test
    void testAddUsernameInFront() {
        String toAdd = "ivan1230;";
        String[] expected = new String[] {toAdd, "4.50", "nikola", "reason", "for", "payment", "is", "beer"};

        command.addUsernameInFront(toAdd);

        assertIterableEquals(List.of(expected), List.of(command.arguments()), "The arguments are not equal");
    }

    @Test
    void testMergeLastArgument() {
        String[] expected = new String[] {"4.50", "nikola", "reason for payment is beer"};

        command.mergeLastArgument(2);

        assertIterableEquals(List.of(expected), List.of(command.arguments()), "The arguments could not merge");
    }
}
