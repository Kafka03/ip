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
    void mainAddsTask() {
        setInput("read book\nbye\n");
        Kafka.main(new String[0]);
        String output = capturedOutput.toString(StandardCharsets.UTF_8);

        assertTrue(output.contains("added: read book uwu~"));
    }

    @Test
    void mainListsStoredTasksInOrder() {
        setInput("read book\nreturn book\nlist\nbye\n");
        Kafka.main(new String[0]);
        String output = capturedOutput.toString(StandardCharsets.UTF_8);

        int firstTaskPosition = output.indexOf("1.[ ] read book uwu~");
        int secondTaskPosition = output.indexOf("2.[ ] return book uwu~");
        assertTrue(firstTaskPosition >= 0, "The first task should be displayed");
        assertTrue(secondTaskPosition > firstTaskPosition,
                "The second task should be displayed after the first task");
    }

    @Test
    void mainMarksAndUnmarksTask() {
        setInput("read book\nmark 1\nlist\nunmark 1\nlist\nbye\n");
        Kafka.main(new String[0]);
        String output = capturedOutput.toString(StandardCharsets.UTF_8);

        int markedPosition = output.indexOf("1.[X] read book uwu~");
        int unmarkedPosition = output.indexOf("1.[ ] read book uwu~", markedPosition);
        assertTrue(output.contains("Nice! I've marked this task as done:"));
        assertTrue(output.contains("  [X] read book uwu~"));
        assertTrue(output.contains("OK, I've marked this task as not done yet:"));
        assertTrue(output.contains("  [ ] read book uwu~"));
        assertTrue(markedPosition >= 0, "The task should be marked as completed");
        assertTrue(unmarkedPosition > markedPosition,
                "The task should be unmarked after it was marked");
    }

    @Test
    void mainStopsImmediatelyOnBye() {
        setInput("bye\n");
        Kafka.main(new String[0]);

        String output = capturedOutput.toString(StandardCharsets.UTF_8);
        assertTrue(output.contains("Bye babe~"));
        assertTrue(!output.contains("added:"));
    }

    private void setInput(String input) {
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
    }
}
