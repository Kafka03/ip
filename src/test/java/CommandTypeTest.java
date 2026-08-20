import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests conversion of user input into command types.
 */
class CommandTypeTest {
    @Test
    void fromInputRecognizesCommandWithArguments() {
        assertEquals(CommandType.TODO, CommandType.fromInput("todo read book"));
    }

    @Test
    void fromInputRecognizesCommandWithoutArguments() {
        assertEquals(CommandType.LIST, CommandType.fromInput("list"));
    }

    @Test
    void fromInputRejectsArgumentsForArgumentFreeCommand() {
        assertEquals(CommandType.UNKNOWN, CommandType.fromInput("list extra"));
        assertEquals(CommandType.UNKNOWN, CommandType.fromInput("bye extra"));
    }

    @Test
    void fromInputIsCaseSensitive() {
        assertEquals(CommandType.UNKNOWN, CommandType.fromInput("Todo read book"));
    }
}
