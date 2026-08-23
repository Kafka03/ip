package kafka;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import kafka.storage.TaskStorage;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Tests complete Kafka command sessions through console input and output.
 */
class KafkaTest {
    private static final List<String> INPUT_ERROR_MESSAGES = List.of(
            "toodaloo. todo needs to hv a description alpha",
            "im deaddd. The deadline description cannot be empty.",
            "A deadline must include /by. Do you hate me?",
            "The deadline date or time cannot be empty alpha.",
            "are u event-ing new ways to tease me? The event description cannot be empty.",
            "An event must include /from followed by /to. Do you hate me?",
            "The event start or end cannot be empty my forbidden alpha~",
            "please gimme just a whole numberrr",
            "The task number must be at least 1 meow.",
            "There is no task with that number",
            "Task details cannot contain | sorryyy");

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
                "3.[E][ ] project meeting (from: Mon 2pm to: 1600)"));
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

    @Test
    void rejectingCorruptedFileOverwritePreservesFileAndShowsLocation() throws IOException {
        Path dataFile = temporaryDirectory.resolve("tasks.txt");
        Files.writeString(dataFile, "invalid saved task");

        String output = runKafka("no\n");

        assertEquals("invalid saved task", Files.readString(dataFile));
        assertTrue(output.contains("Your task data was not changed."));
        assertTrue(output.contains(dataFile.toAbsolutePath().toString()));
        assertFalse(output.contains("Starting with an empty list."));
    }

    @Test
    void approvingCorruptedFileOverwriteClearsFileAndContinues() throws IOException {
        Path dataFile = temporaryDirectory.resolve("tasks.txt");
        Files.writeString(dataFile, "invalid saved task");

        String output = runKafka("yes\nlist\nbye\n");

        assertEquals("", Files.readString(dataFile));
        assertTrue(output.contains("Starting with an empty list."));
        assertTrue(output.contains("You have no tasks lined up"));
        assertTrue(output.contains("Bye babe~"));
    }

    @ParameterizedTest
    @MethodSource("invalidInputsAndExpectedErrors")
    void invalidInputShowsOnlyItsMatchingError(String invalidInput, String expectedError) {
        String output = runKafka(invalidInput + "\nbye\n");

        assertEquals(1, countOccurrences(output, expectedError),
                "The matching error should appear exactly once");
        for (String otherError : INPUT_ERROR_MESSAGES) {
            if (!otherError.equals(expectedError)) {
                assertFalse(output.contains(otherError),
                        "The output should not contain the unrelated error: " + otherError);
            }
        }
        assertFalse(output.contains("I've added this task"));
    }

    private static Stream<Arguments> invalidInputsAndExpectedErrors() {
        return Stream.of(
                Arguments.of("todo", INPUT_ERROR_MESSAGES.get(0)),
                Arguments.of("deadline /by Friday", INPUT_ERROR_MESSAGES.get(1)),
                Arguments.of("deadline return book", INPUT_ERROR_MESSAGES.get(2)),
                Arguments.of("deadline return book /by", INPUT_ERROR_MESSAGES.get(3)),
                Arguments.of("event /from 2pm /to 3pm", INPUT_ERROR_MESSAGES.get(4)),
                Arguments.of("event meeting /to 3pm", INPUT_ERROR_MESSAGES.get(5)),
                Arguments.of("event meeting /from /to 3pm", INPUT_ERROR_MESSAGES.get(6)),
                Arguments.of("mark abc", INPUT_ERROR_MESSAGES.get(7)),
                Arguments.of("mark 0", INPUT_ERROR_MESSAGES.get(8)),
                Arguments.of("mark 1", INPUT_ERROR_MESSAGES.get(9)),
                Arguments.of("todo compare A | B", INPUT_ERROR_MESSAGES.get(10)));
    }

    private static int countOccurrences(String text, String value) {
        int count = 0;
        int position = 0;
        while ((position = text.indexOf(value, position)) >= 0) {
            count++;
            position += value.length();
        }
        return count;
    }

    private String runKafka(String input) {
        capturedOutput.reset();
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        TaskStorage taskStorage = new TaskStorage(temporaryDirectory.resolve("tasks.txt"));
        new Kafka(taskStorage).run();
        return capturedOutput.toString(StandardCharsets.UTF_8);
    }
}
