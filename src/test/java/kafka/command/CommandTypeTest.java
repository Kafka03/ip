package kafka.command;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Checks that Kafka identifies commands correctly and rejects impostors.
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
