package bg.sofia.uni.fmi.mjt.splitwise.client.command;

import bg.sofia.uni.fmi.mjt.splitwise.client.io.UserInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;

public class CommandValidatorTest {
    UserInterface ui = Mockito.mock(UserInterface.class);

    CommandValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CommandValidator(ui);
    }

    @Test
    void testValidateSignUpTooManyArguments() {
        Command command =
            new Command(CommandName.SIGN_UP, new String[] {"nikola", "password", "Nikola", "Manolov", "Manolov"});

        assertFalse(validator.validateSignUp(command), "The validator should return false");
        Mockito.verify(ui).writeError(CommandValidator.TOO_MANY_ARGUMENTS_MESSAGE);
    }

    @Test
    void testValidateSignUpNotEnoughArguments() {
        Command command = new Command(CommandName.SIGN_UP, new String[] {"nikola", "password", "Nikola"});

        assertFalse(validator.validateSignUp(command), "The validator should return false");
        Mockito.verify(ui).writeError(CommandValidator.NOT_ENOUGH_ARGUMENTS_MESSAGE);
    }

    @Test
    void testValidateSignUpInvalidUsername() {
        Command command =
            new Command(CommandName.SIGN_UP, new String[] {"1234nikola", "password", "Nikola", "Manolov"});

        assertFalse(validator.validateSignUp(command), "The validator should return false");
        Mockito.verify(ui).writeError(anyString());
    }

    @Test
    void testValidateSignUpPasswordTooLong() {
        Command command = new Command(CommandName.SIGN_UP,
            new String[] {"nikola", "password22121211212121211121122112121212122121212112", "Nikola", "Manolov"});

        assertFalse(validator.validateSignUp(command), "The validator should return false");
        Mockito.verify(ui).writeError(anyString());
    }

    @Test
    void testValidateSignUpPasswordTooShort() {
        Command command = new Command(CommandName.SIGN_UP, new String[] {"nikola", "pass", "Nikola", "Manolov"});

        assertFalse(validator.validateSignUp(command), "The validator should return false");
        Mockito.verify(ui).writeError(anyString());
    }

    @Test
    void testValidateSignUpFirstNameInvalid() {
        Command command = new Command(CommandName.SIGN_UP, new String[] {"nikola", "password", "Ni2kola", "Manolov"});

        assertFalse(validator.validateSignUp(command), "The validator should return false");
        Mockito.verify(ui).writeError(anyString());
    }

    @Test
    void testValidateSignUpFamilyNameInvalid() {
        Command command = new Command(CommandName.SIGN_UP, new String[] {"nikola", "password", "Nikola", "Man2olov"});

        assertFalse(validator.validateSignUp(command), "The validator should return false");
        Mockito.verify(ui).writeError(anyString());
    }

    @Test
    void testValidateSignUpReturnsTrue() {
        Command command = new Command(CommandName.SIGN_UP, new String[] {"nikola", "password", "Nikola", "Manolov"});

        assertTrue(validator.validateSignUp(command), "The validator should return true. Everything is ok");
        Mockito.verify(ui, never()).writeError(anyString());
    }

    @Test
    void testValidateArgumentsCountNotEnough() {
        Command command = new Command(CommandName.LOGIN, new String[] {"nikola"});

        assertFalse(validator.validateArgumentsCount(command), "The validator should return false");
        Mockito.verify(ui).writeError(CommandValidator.NOT_ENOUGH_ARGUMENTS_MESSAGE);
    }

    @Test
    void testValidateArgumentsCountTooMuch() {
        Command command = new Command(CommandName.LOGIN, new String[] {"nikola", "password", "passwordSecondTime"});

        assertFalse(validator.validateArgumentsCount(command), "The validator should return false");
        Mockito.verify(ui).writeError(CommandValidator.TOO_MANY_ARGUMENTS_MESSAGE);
    }

    @Test
    void testValidateArgumentsCountReturnsTrue() {
        Command command = new Command(CommandName.LOGIN, new String[] {"nikola", "password"});

        assertTrue(validator.validateArgumentsCount(command), "The validator should return false");
        Mockito.verify(ui, never()).writeError(anyString());
    }

    @Test
    void testValidateCreateGroupNotEnoughArguments() {
        Command command = new Command(CommandName.CREATE_GROUP, new String[] {"onlyGroupName"});

        assertFalse(validator.validateCreateGroup(command), "The validator should return false");
        Mockito.verify(ui).writeError(CommandValidator.NOT_ENOUGH_ARGUMENTS_MESSAGE);
    }

    @Test
    void testValidateCreateGroupInvalidGroupName() {
        Command command =
            new Command(CommandName.CREATE_GROUP, new String[] {"_invalid_groupName", "member1", "member2"});

        assertFalse(validator.validateCreateGroup(command), "The validator should return false");
        Mockito.verify(ui).writeError(anyString());
    }

    @Test
    void testValidateCreateGroupReturnsTrue() {
        Command command =
            new Command(CommandName.CREATE_GROUP, new String[] {"real123groupName", "member1", "member2"});

        assertTrue(validator.validateCreateGroup(command), "The validator should return true");
        Mockito.verify(ui, never()).writeError(anyString());
    }

    @Test
    void testValidateSplitNotEnoughArguments() {
        Command command = new Command(CommandName.SPLIT, new String[] {"4.5", "nikola"});

        assertFalse(validator.validateSplit(command), "The validator should return false");
        Mockito.verify(ui).writeError(CommandValidator.NOT_ENOUGH_ARGUMENTS_MESSAGE);
    }

    @Test
    void testValidateSplitGroupNotEnoughArguments() {
        Command command = new Command(CommandName.SPLIT_GROUP, new String[] {"4.5", "groupName"});

        assertFalse(validator.validateSplit(command), "The validator should return false");
        Mockito.verify(ui).writeError(CommandValidator.NOT_ENOUGH_ARGUMENTS_MESSAGE);
    }

    @Test
    void testValidateSplitDoubleIsString() {
        Command command = new Command(CommandName.SPLIT, new String[] {"4.d5", "nikola", "reason"});

        assertFalse(validator.validateSplit(command), "The validator should return false");
        Mockito.verify(ui).writeError(CommandValidator.DOUBLE_MESSAGE);
    }

    @Test
    void testValidateSplitTooManyDecimalPlaces() {
        Command command = new Command(CommandName.SPLIT, new String[] {"4.556", "nikola", "reason"});

        assertFalse(validator.validateSplit(command), "The validator should return false");
        Mockito.verify(ui).writeError(CommandValidator.DOUBLE_MESSAGE);
    }

    @Test
    void testValidateSplitReturnsTrue() {
        Command command = new Command(CommandName.SPLIT, new String[] {"4.550", "nikola", "reason", "for", "payment"});

        assertTrue(validator.validateSplit(command), "The validator should return true. Everything is ok");
        Mockito.verify(ui, never()).writeError(anyString());
    }

    @Test
    void testValidateSplitGroupReturnsTrue() {
        Command command = new Command(CommandName.SPLIT, new String[] {"4.5", "groupName", "reason", "for", "payment"});

        assertTrue(validator.validateSplit(command), "The validator should return true. Everything is ok");
        Mockito.verify(ui, never()).writeError(anyString());
    }

    @Test
    void testValidateReceiveNotEnoughArguments() {
        Command command = new Command(CommandName.RECEIVE, new String[] {"4.550"});

        assertFalse(validator.validateReceive(command), "The validator should return false.");
        Mockito.verify(ui).writeError(CommandValidator.NOT_ENOUGH_ARGUMENTS_MESSAGE);
    }

    @Test
    void testValidateReceiveTooManyArguments() {
        Command command = new Command(CommandName.RECEIVE, new String[] {"4.550", "nikola", "why"});

        assertFalse(validator.validateReceive(command), "The validator should return false.");
        Mockito.verify(ui).writeError(CommandValidator.TOO_MANY_ARGUMENTS_MESSAGE);
    }

    @Test
    void testValidateReceiveInvalidDouble() {
        Command command = new Command(CommandName.RECEIVE, new String[] {"4.555", "nikola"});

        assertFalse(validator.validateReceive(command), "The validator should return false.");
        Mockito.verify(ui).writeError(CommandValidator.DOUBLE_MESSAGE);
    }

    @Test
    void testValidateReceiveReturnsTrue() {
        Command command = new Command(CommandName.RECEIVE, new String[] {"4.550", "nikola"});

        assertTrue(validator.validateReceive(command), "The validator should return true.");
        Mockito.verify(ui, never()).writeError(anyString());
    }

    @Test
    void testValidateReceiveGroupNotEnoughArguments() {
        Command command = new Command(CommandName.RECEIVE_GROUP, new String[] {"4.550", "groupName123"});

        assertFalse(validator.validateReceive(command), "The validator should return false.");
        Mockito.verify(ui).writeError(CommandValidator.NOT_ENOUGH_ARGUMENTS_MESSAGE);
    }

    @Test
    void testValidateReceiveGroupTooManyArguments() {
        Command command =
            new Command(CommandName.RECEIVE_GROUP, new String[] {"4.550", "groupName123", "nikola", "why"});

        assertFalse(validator.validateReceive(command), "The validator should return false.");
        Mockito.verify(ui).writeError(CommandValidator.TOO_MANY_ARGUMENTS_MESSAGE);
    }

    @Test
    void testValidateReceiveGroupInvalidDouble() {
        Command command = new Command(CommandName.RECEIVE_GROUP, new String[] {"4.d55", "groupName123", "nikola"});

        assertFalse(validator.validateReceive(command), "The validator should return false.");
        Mockito.verify(ui).writeError(CommandValidator.DOUBLE_MESSAGE);
    }

    @Test
    void testValidateReceiveGroupReturnsTrue() {
        Command command = new Command(CommandName.RECEIVE_GROUP, new String[] {"4.550", "groupName123", "nikola"});

        assertTrue(validator.validateReceive(command), "The validator should return true.");
        Mockito.verify(ui, never()).writeError(anyString());
    }
}

