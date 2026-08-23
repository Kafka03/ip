package kafka.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import kafka.exception.CorruptedTaskDataException;
import kafka.exception.KafkaException;
import kafka.task.Deadline;
import kafka.task.Event;
import kafka.task.Task;
import kafka.task.TaskList;
import kafka.task.Todo;

/**
 * Saves the task squad and brings it back next session like nothing happened.
 * Each line uses the format {@code type | status | task details}.
 */
public class TaskStorage {
    /** Default save-file location relative to the project directory. */
    private static final Path DEFAULT_PATH = Path.of("data", "kafka.txt");
    private static final String TODO_TYPE = "T";
    private static final String DEADLINE_TYPE = "D";
    private static final String EVENT_TYPE = "E";
    private static final String DONE_STATUS = "1";
    private static final String NOT_DONE_STATUS = "0";

    private final Path filePath;

    /**
     * Creates storage backed by {@code data/kafka.txt}.
     */
    public TaskStorage() {
        this(DEFAULT_PATH);
    }

    /**
     * Creates storage backed by a specific file, which is handy for isolated tests.
     *
     * @param filePath task data file to read and write
     */
    public TaskStorage(Path filePath) {
        this.filePath = filePath;
    }

    /**
     * Returns the absolute task-file location so a user can find the drama.
     *
     * @return normalized absolute path to the task data file
     */
    public Path getFilePath() {
        return filePath.toAbsolutePath().normalize();
    }

    /**
     * Loads all valid task records in their saved order.
     * A missing file represents a user who has not saved any tasks yet.
     *
     * @return the tasks stored in the data file, or an empty list if it is absent
     * @throws CorruptedTaskDataException if a saved line is malformed
     * @throws KafkaException if the file cannot be read
     */
    public TaskList load() throws KafkaException {
        TaskList tasks = new TaskList();
        if (Files.notExists(filePath)) {
            return tasks;
        }

        try {
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (!line.isBlank()) {
                    tasks.addTask(parseTask(line, i + 1));
                }
            }
            return tasks;
        } catch (IOException exception) {
            throw new KafkaException("Could not read tasks from " + filePath, exception);
        }
    }

    /**
     * Writes the current tasks to the data file in their list order.
     * Any previous file contents are replaced with the current task list.
     *
     * @param tasks tasks to persist
     * @throws KafkaException if the directory or data file cannot be written
     */
    public void save(TaskList tasks) throws KafkaException {
        try {
            Path parentDirectory = filePath.getParent();
            if (parentDirectory != null) {
                Files.createDirectories(parentDirectory);
            }

            List<String> lines = tasks.getTasks().stream()
                    .map(Task::toDataString)
                    .toList();
            Files.write(filePath, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new KafkaException("Could not save tasks to " + filePath, exception);
        }
    }

    /**
     * Converts one saved record into its concrete task subtype.
     *
     * @param line one complete record from the task data file
     * @param lineNumber one-based source line used in corruption messages
     * @return reconstructed todo, deadline, or event
     * @throws KafkaException if the record cannot be trusted
     */
    private Task parseTask(String line, int lineNumber) throws KafkaException {
        String[] fields = line.split("\\s*\\|\\s*", -1);
        if (fields.length < 3) {
            throw malformedLine(lineNumber);
        }

        String type = fields[0];
        String status = fields[1];
        Task task;

        if (fields[2].isBlank()) {
            throw malformedLine(lineNumber);
        }

        switch (type) {
        case TODO_TYPE -> {
            requireFieldCount(fields, 3, lineNumber);
            task = new Todo(fields[2]);
        }
        case DEADLINE_TYPE -> {
            requireFieldCount(fields, 4, lineNumber);
            requireNonBlank(fields[3], lineNumber);
            task = new Deadline(fields[2], fields[3]);
        }
        case EVENT_TYPE -> {
            requireFieldCount(fields, 5, lineNumber);
            requireNonBlank(fields[3], lineNumber);
            requireNonBlank(fields[4], lineNumber);
            task = new Event(fields[2], fields[3], fields[4]);
        }
        default -> throw malformedLine(lineNumber);
        }

        if (status.equals(DONE_STATUS)) {
            task.mark();
        } else if (!status.equals(NOT_DONE_STATUS)) {
            throw malformedLine(lineNumber);
        }
        return task;
    }

    /**
     * Checks that a record has exactly the fields required by its task type.
     *
     * @param fields fields parsed from the record
     * @param expected required field count
     * @param lineNumber one-based source line used in corruption messages
     * @throws KafkaException if the field count is serving the wrong number
     */
    private void requireFieldCount(String[] fields, int expected, int lineNumber)
            throws KafkaException {
        if (fields.length != expected) {
            throw malformedLine(lineNumber);
        }
    }

    /**
     * Checks that a required saved field contains actual text.
     *
     * @param field saved value to inspect
     * @param lineNumber one-based source line used in corruption messages
     * @throws KafkaException if the field is blank
     */
    private void requireNonBlank(String field, int lineNumber) throws KafkaException {
        if (field.isBlank()) {
            throw malformedLine(lineNumber);
        }
    }

    /**
     * Builds a consistent exception for one malformed storage line.
     *
     * @param lineNumber one-based line containing corrupted data
     * @return exception identifying the malformed line and file
     */
    private CorruptedTaskDataException malformedLine(int lineNumber) {
        return new CorruptedTaskDataException(
                "Malformed task data on line " + lineNumber + " of " + filePath);
    }
}
