import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests complete Kafka command sessions through console input and output.
 */
class KafkaTest {
    @TempDir
    Path temporaryDirectory;

    private InputStream originalInput;
    private PrintStream originalOutput;
    private ByteArrayOutputStream capturedOutput;

    @BeforeEach
    void setUpConsole() {
        originalInput = System.in;
        originalOutput = System.out;
        capturedOutput = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOutput, true, StandardCharsets.UTF_8));
    }

    @AfterEach
    void restoreConsole() {
        System.setIn(originalInput);
        System.setOut(originalOutput);
    }

    @Test
    void mainAddsAndListsTypedTasks() {
        String output = runKafka("todo borrow book\n"
                + "deadline return book /by Sunday\n"
                + "event project meeting /from Mon 2pm /to 4pm\n"
                + "list\nbye\n");

        assertTrue(output.contains("1.[T][ ] borrow book"));
        assertTrue(output.contains("2.[D][ ] return book (by: Sunday)"));
        assertTrue(output.contains(
                "3.[E][ ] project meeting (from: Mon 2pm to: 4pm)"));
        assertTrue(output.contains("Now you have 3 tasks in the list."));
    }

    @Test
    void mainMarksAndUnmarksTypedTask() {
        String output = runKafka("todo read book\nmark 1\nlist\nunmark 1\nlist\nbye\n");

        int markedPosition = output.indexOf("1.[T][X] read book");
        int unmarkedPosition = output.indexOf("1.[T][ ] read book", markedPosition);
        assertTrue(markedPosition >= 0, "The task should be marked");
        assertTrue(unmarkedPosition > markedPosition,
                "The task should later be unmarked");
    }

    @Test
    void mainDeletesTaskAndShiftsRemainingTasks() {
        String output = runKafka("todo first\n"
                + "deadline second /by Sunday\n"
                + "event third /from Monday /to Tuesday\n"
                + "delete 2\nlist\nbye\n");

        assertTrue(output.contains("Aight. I've yeeted this task:"));
        assertTrue(output.contains("[D][ ] second (by: Sunday)"));
        assertTrue(output.contains("1.[T][ ] first"));
        assertTrue(output.contains("2.[E][ ] third (from: Monday to: Tuesday)"));
        assertFalse(output.contains("2.[D][ ] second (by: Sunday)"));
    }

    @Test
    void invalidCommandsDoNotCorruptTaskState() {
        String output = runKafka("todo read book\n"
                + "deadline broken /by\n"
                + "event meeting /from Monday /to Tuesday\n"
                + "mark abc\n"
                + "mark 1\n"
                + "event broken /from /to Friday\n"
                + "unmark 3\n"
                + "deadline submit report /by Sunday\n"
                + "list\nbye\n");

        assertTrue(output.contains("1.[T][X] read book"));
        assertTrue(output.contains(
                "2.[E][ ] meeting (from: Monday to: Tuesday)"));
        assertTrue(output.contains("3.[D][ ] submit report (by: Sunday)"));
        assertFalse(output.contains("4."));
        assertTrue(output.contains("deadline date or time cannot be empty"));
        assertTrue(output.contains("whole number"));
        assertTrue(output.contains("event start or end cannot be empty"));
        assertTrue(output.contains("There is no task with that number"));
    }

    @Test
    void unknownCommandDoesNotAddTask() {
        String output = runKafka("Event invalid /from Monday /to Tuesday\n"
                + "todo valid\nlist\nbye\n");

        assertTrue(output.contains("I don't know that command"));
        assertTrue(output.contains("1.[T][ ] valid"));
        assertFalse(output.contains("2."));
    }

    @Test
    void mainStopsImmediatelyOnBye() {
        String output = runKafka("bye\n");

        assertTrue(output.contains("Bye babe~"));
        assertFalse(output.contains("I've added this task"));
    }

    @Test
    void tasksPersistAcrossSessions() {
        runKafka("todo remember me\nmark 1\nbye\n");

        String output = runKafka("list\nbye\n");

        assertTrue(output.contains("1.[T][X] remember me"));
    }

    private String runKafka(String input) {
        capturedOutput.reset();
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        TaskStorage taskStorage = new TaskStorage(temporaryDirectory.resolve("tasks.txt"));
        new Kafka(taskStorage).run();
        return capturedOutput.toString(StandardCharsets.UTF_8);
    }
}
