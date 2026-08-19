import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

//Tests Kafka java file
class KafkaTest {
    private InputStream originalInput;
    private PrintStream originalOutput;
    private ByteArrayOutputStream capturedOutput;

    /**
     * Saves the real console streams and prepares a fresh output stream for each test.
     */
    @BeforeEach
    void setUpConsole() {
        originalInput = System.in;
        originalOutput = System.out;
        capturedOutput = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOutput, true, StandardCharsets.UTF_8));
    }

    /**
     * Restores the real console streams after each test.
     */
    @AfterEach
    void restoreConsole() {
        System.setIn(originalInput);
        System.setOut(originalOutput);
    }

    @Test
    void replacesLowercaseAndUppercaseL() {
        assertEquals("Hewwo Wiwy uwu~", Kafka.makeUwu("Hello Lily"));
    }

    @Test
    void leavesOtherCharactersUnchanged() {
        assertEquals("cat uwu~", Kafka.makeUwu("cat"));
    }

    @Test
    void handlesEmptyInput() {
        assertEquals(" uwu~", Kafka.makeUwu(""));
    }

    @Test
    void commandKafkaAddsTask() {
        System.setIn(new ByteArrayInputStream(
                "read book\nbye\n".getBytes(StandardCharsets.UTF_8)));
        Kafka.commandKafka();
        String output = capturedOutput.toString(StandardCharsets.UTF_8);

        assertTrue(output.contains("added: read book uwu~"));
    }

    @Test
    void commandKafkaListsStoredTasksInOrder() {
        System.setIn(new ByteArrayInputStream(
                "read book\nreturn book\nlist\nbye\n".getBytes(StandardCharsets.UTF_8)));
        Kafka.commandKafka();
        String output = capturedOutput.toString(StandardCharsets.UTF_8);

        int firstTaskPosition = output.indexOf("1. read book uwu~");
        int secondTaskPosition = output.indexOf("2. return book uwu~");
        assertTrue(firstTaskPosition >= 0, "The first task should be displayed");
        assertTrue(secondTaskPosition > firstTaskPosition,
                "The second task should be displayed after the first task");
    }

    @Test
    void commandKafkaStopsImmediatelyOnBye() {
        System.setIn(new ByteArrayInputStream("bye\n".getBytes(StandardCharsets.UTF_8)));
        Kafka.commandKafka();

        assertEquals("", capturedOutput.toString(StandardCharsets.UTF_8));
    }
}
