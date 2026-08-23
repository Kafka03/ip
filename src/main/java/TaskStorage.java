import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Persists tasks so they can be restored between sessions.
 * Each line uses the format {@code type | status | task details}.
 */
class TaskStorage {
    private static final Path DEFAULT_PATH = Path.of("data", "kafka.txt");

    private final Path filePath;

    TaskStorage() {
        this(DEFAULT_PATH);
    }

    TaskStorage(Path filePath) {
        this.filePath = filePath;
    }

    /**
     * Returns the absolute location of the task data file for user guidance.
     */
    Path getFilePath() {
        return filePath.toAbsolutePath().normalize();
    }

    /**
     * Loads all valid task records in their saved order.
     * A missing file represents a user who has not saved any tasks yet.
     *
     * @return the tasks stored in the data file, or an empty list if it is absent
     * @throws KafkaException if the file cannot be read or contains malformed data
     */
    TaskList load() throws KafkaException {
        TaskList tasks = new TaskList();
        if (Files.notExists(filePath)) {
            return tasks;
        }

        try {
            List<String> lines = Files.readAllLines(filePath);
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
    void save(TaskList tasks) throws KafkaException {
        try {
            Path parentDirectory = filePath.getParent();
            if (parentDirectory != null) {
                Files.createDirectories(parentDirectory);
            }

            List<String> lines = tasks.getTasks().stream()
                    .map(Task::toDataString)
                    .toList();
            Files.write(filePath, lines);
        } catch (IOException exception) {
            throw new KafkaException("Could not save tasks to " + filePath, exception);
        }
    }

    /**
     * Converts one saved record into its concrete task subtype.
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
        case "T" -> {
            requireFieldCount(fields, 3, lineNumber);
            task = new Todo(fields[2]);
        }
        case "D" -> {
            requireFieldCount(fields, 4, lineNumber);
            requireNonBlank(fields[3], lineNumber);
            task = new Deadline(fields[2], fields[3]);
        }
        case "E" -> {
            requireFieldCount(fields, 5, lineNumber);
            requireNonBlank(fields[3], lineNumber);
            requireNonBlank(fields[4], lineNumber);
            task = new Event(fields[2], fields[3], fields[4]);
        }
        default -> throw malformedLine(lineNumber);
        }

        if (status.equals("1")) {
            task.mark();
        } else if (!status.equals("0")) {
            throw malformedLine(lineNumber);
        }
        return task;
    }

    private void requireFieldCount(String[] fields, int expected, int lineNumber)
            throws KafkaException {
        if (fields.length != expected) {
            throw malformedLine(lineNumber);
        }
    }

    private void requireNonBlank(String field, int lineNumber) throws KafkaException {
        if (field.isBlank()) {
            throw malformedLine(lineNumber);
        }
    }

    private CorruptedTaskDataException malformedLine(int lineNumber) {
        return new CorruptedTaskDataException(
                "Malformed task data on line " + lineNumber + " of " + filePath);
    }
}
