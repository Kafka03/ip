package kafka.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests command identification for supported and unsupported input.
 */
class CommandTypeTest {
    @Test
    void fromInputRecognizesCommandWithArguments() {
        assertEquals(CommandType.TODO, CommandType.fromInput("todo read book"));
        assertEquals(CommandType.FIND, CommandType.fromInput("find book"));
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
