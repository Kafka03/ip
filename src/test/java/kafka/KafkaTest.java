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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import kafka.storage.TaskStorage;

/**
 * Tests complete Kafka sessions across the user interface and application layers.
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
    void mainFindsAndRenumbersMatchingTasks() {
        String output = runKafka("todo read book\n"
                + "deadline return book /by June 6th\n"
                + "todo write essay\n"
                + "mark 1\nmark 2\nfind book\nbye\n");

        String normalizedOutput = output.replace("\r\n", "\n");
        assertTrue(normalizedOutput.contains("I worked hard to find the matching tasks in your list king:\n"
                + "1.[T][X] read book\n"
                + "2.[D][X] return book (by: June 6th)\n"
                + "_".repeat(60)));
        assertFalse(output.contains("3.[T][ ] write essay"));
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
    void getResponse_errorThenSuccess_returnsIndependentErrorFlags() {
        TaskStorage taskStorage = new TaskStorage(temporaryDirectory.resolve("tasks.txt"));
        Kafka kafka = new Kafka(taskStorage);

        KafkaResponse errorResponse = kafka.getResponse("todo");
        assertTrue(errorResponse.isError());

        KafkaResponse successResponse = kafka.getResponse("todo read book");
        assertFalse(successResponse.isError());
        assertTrue(errorResponse.isError());
    }

    @Test
    void mainStopsImmediatelyOnBye() {
        String output = runKafka("bye\ntodo should not be added\n");

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
    void renameChangesTaskNameAndPersistsIt() {
        String renameOutput = runKafka("todo read book\nrename 1 read novel\nbye\n");

        assertTrue(renameOutput.contains("[T][ ] read book"));
        assertTrue(renameOutput.contains("[T][ ] read novel"));

        String listOutput = runKafka("list\nbye\n");
        assertTrue(listOutput.contains("1.[T][ ] read novel"));
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

    @Test
    void getResponseUsesCorruptedFileRecovery() throws IOException {
        Path dataFile = temporaryDirectory.resolve("tasks.txt");
        Files.writeString(dataFile, "invalid saved task");
        System.setIn(new ByteArrayInputStream("yes\n".getBytes(StandardCharsets.UTF_8)));
        Kafka kafka = new Kafka(new TaskStorage(dataFile));

        KafkaResponse response = kafka.getResponse("list");

        assertEquals("", Files.readString(dataFile));
        assertTrue(response.message().contains("You have no tasks lined up"));
        assertFalse(response.isError());
    }

    @ParameterizedTest
    @CsvSource({
        "snooze 1 /by 2024-02-29 12pm, D | 1 | report | 29 Feb 2024 1200, "
                + "1.[D][X] report (by: 29 Feb 2024 1200)",
        "snooze 2 /from 9am, E | 1 | meeting | 0900 | Tuesday, 2.[E][X] meeting (from: 0900 to: Tuesday)",
        "snooze 2 /to 5pm, E | 1 | meeting | Monday | 1700, 2.[E][X] meeting (from: Monday to: 1700)",
        "snooze 2 /from 9am /to 5pm, E | 1 | meeting | 0900 | 1700, 2.[E][X] meeting (from: 0900 to: 1700)"
    })
    void getResponse_snooze_persistsScheduleAndCompletionAcrossSessions(String command,
            String expectedRecord, String expectedDisplay) throws IOException {
        Path dataFile = temporaryDirectory.resolve("tasks.txt");
        Kafka kafka = new Kafka(new TaskStorage(dataFile));
        assertFalse(kafka.getResponse("deadline report /by Friday").isError());
        assertFalse(kafka.getResponse("event meeting /from Monday /to Tuesday").isError());
        assertFalse(kafka.getResponse("mark 1").isError());
        assertFalse(kafka.getResponse("mark 2").isError());

        KafkaResponse response = kafka.getResponse(command);

        assertFalse(response.isError());
        assertTrue(response.message().contains("I've rescheduled this task"));
        assertEquals(command.startsWith("snooze 1")
                ? List.of(expectedRecord, "E | 1 | meeting | Monday | Tuesday")
                : List.of("D | 1 | report | Friday", expectedRecord), Files.readAllLines(dataFile));
        KafkaResponse restored = new Kafka(new TaskStorage(dataFile)).getResponse("list");
        assertFalse(restored.isError());
        assertTrue(restored.message().contains(expectedDisplay));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "snooze 1 /by Sunday", "snooze 1 /from Monday", "snooze 2 /to Sunday",
        "snooze 3 /by Sunday", "snooze 4 /by Sunday", "snooze 2 /by",
        "snooze 3 /from Monday /to", "snooze 3 /to Tuesday /from Monday",
        "snooze 2 /by Sun | Mon", "rename 1", "rename 0 changed", "rename 4 changed",
        "rename 1 buy | cook", "find", "unknown"
    })
    void getResponse_invalidEditOrQuery_preservesMemoryAndSavedFile(String command) throws IOException {
        Path dataFile = temporaryDirectory.resolve("tasks.txt");
        Kafka kafka = new Kafka(new TaskStorage(dataFile));
        assertFalse(kafka.getResponse("todo read book").isError());
        assertFalse(kafka.getResponse("deadline report /by Friday").isError());
        assertFalse(kafka.getResponse("event meeting /from Monday /to Tuesday").isError());
        String originalFile = Files.readString(dataFile);
        String originalList = kafka.getResponse("list").message();

        KafkaResponse response = kafka.getResponse(command);

        assertTrue(response.isError(), command);
        assertEquals(originalFile, Files.readString(dataFile));
        assertEquals(originalList, kafka.getResponse("list").message());
    }

    @Test
    void getResponse_unmarkAndDelete_persistAcrossSessions() throws IOException {
        Path dataFile = temporaryDirectory.resolve("tasks.txt");
        Kafka kafka = new Kafka(new TaskStorage(dataFile));
        kafka.getResponse("todo first");
        kafka.getResponse("todo second");
        kafka.getResponse("mark 2");

        assertFalse(kafka.getResponse("unmark 2").isError());
        assertFalse(kafka.getResponse("delete 1").isError());

        assertEquals(List.of("T | 0 | second"), Files.readAllLines(dataFile));
        KafkaResponse response = new Kafka(new TaskStorage(dataFile)).getResponse("list");
        assertTrue(response.message().contains("1.[T][ ] second"));
        assertFalse(response.message().contains("first"));
    }

    @Test
    void getResponse_readOnlyCommands_doNotRewriteStorage() throws IOException {
        Path dataFile = temporaryDirectory.resolve("tasks.txt");
        String savedContents = "T|0|read book\n\nD|1|report|Sunday\n";
        Files.writeString(dataFile, savedContents);
        Kafka kafka = new Kafka(new TaskStorage(dataFile));

        assertFalse(kafka.getResponse("list").isError());
        assertFalse(kafka.getResponse("find book").isError());
        assertFalse(kafka.getResponse("find absent").isError());
        assertTrue(kafka.getResponse("unknown").isError());
        KafkaResponse farewell = kafka.getResponse("bye");
        assertFalse(farewell.isError());
        assertTrue(farewell.message().contains("Bye babe~"));
        assertEquals(savedContents, Files.readString(dataFile));
    }

    @Test
    void getResponse_unreadableStorage_returnsErrorWithoutRequestingOverwrite() {
        Kafka kafka = new Kafka(new TaskStorage(temporaryDirectory));

        KafkaResponse response = kafka.getResponse("list");

        assertTrue(response.isError());
        assertTrue(response.message().contains("Saved tasks could not be loaded"));
        assertFalse(capturedOutput.toString(StandardCharsets.UTF_8).contains("Overwrite it"));
    }

    @Test
    void getResponse_saveFailure_returnsErrorAndPreservesExistingFile() throws IOException {
        Path parentFile = temporaryDirectory.resolve("parent.txt");
        Files.writeString(parentFile, "keep me");
        Kafka kafka = new Kafka(new TaskStorage(parentFile.resolve("tasks.txt")));

        KafkaResponse response = kafka.getResponse("todo read book");

        assertTrue(response.isError());
        assertTrue(response.message().contains("Could not save tasks to"));
        assertEquals("keep me", Files.readString(parentFile));
    }

    @Test
    void corruptedFileRecovery_invalidAnswerThenYes_repromptsAndContinues() throws IOException {
        Path dataFile = temporaryDirectory.resolve("tasks.txt");
        Files.writeString(dataFile, "invalid saved task");

        String output = runKafka("maybe\n Y \ntodo recovered\nbye\n");

        assertTrue(output.contains("Please enter yes or no."));
        assertEquals(2, countOccurrences(output, "Overwrite it with an empty task list"));
        assertEquals(List.of("T | 0 | recovered"), Files.readAllLines(dataFile));
    }

    @ParameterizedTest
    @ValueSource(strings = {" N \n", ""})
    void corruptedFileRecovery_refusalOrEndOfInput_preservesFile(String input) throws IOException {
        Path dataFile = temporaryDirectory.resolve("tasks.txt");
        Files.writeString(dataFile, "invalid saved task");

        String output = runKafka(input);

        assertEquals("invalid saved task", Files.readString(dataFile));
        assertTrue(output.contains("Your task data was not changed."));
        assertFalse(output.contains("Starting with an empty list."));
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

    /**
     * Supplies bad commands alongside the one error each command should produce.
     *
     * @return invalid commands and their matching user-facing errors
     */
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

    /**
     * Counts non-overlapping appearances of one value in some text.
     *
     * @param text complete text to search
     * @param value value whose appearances should be counted
     * @return number of non-overlapping appearances
     */
    private static int countOccurrences(String text, String value) {
        int count = 0;
        int position = 0;
        while ((position = text.indexOf(value, position)) >= 0) {
            count++;
            position += value.length();
        }
        return count;
    }

    /**
     * Runs one isolated Kafka session using temporary storage and captured output.
     *
     * @param input newline-separated commands for the session
     * @return everything Kafka printed during that session
     */
    private String runKafka(String input) {
        capturedOutput.reset();
        System.setIn(new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
        TaskStorage taskStorage = new TaskStorage(temporaryDirectory.resolve("tasks.txt"));
        new Kafka(taskStorage).run();
        return capturedOutput.toString(StandardCharsets.UTF_8);
    }
}
