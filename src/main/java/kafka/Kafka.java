package kafka;

import kafka.command.CommandType;
import kafka.exception.CorruptedTaskDataException;
import kafka.exception.KafkaException;
import kafka.parser.TaskParser;
import kafka.storage.TaskStorage;
import kafka.task.Task;
import kafka.task.TaskList;
import kafka.ui.Ui;

/**
 * Runs the whole Kafka show by coordinating the UI, parser, storage, and task
 * list without making any one of them do everything.
 */
public class Kafka {
    private final Ui ui;
    private final TaskStorage taskStorage;
    private TaskList tasks;

    /**
     * Creates Kafka with its usual {@code data/kafka.txt} storage file.
     */
    Kafka() {
        this(new TaskStorage());
    }

    /**
     * Creates Kafka with a specified storage location, which also makes
     * persistence testable without changing the user's real data file.
     *
     * @param taskStorage storage service Kafka should use for this session
     */
    Kafka(TaskStorage taskStorage) {
        this.ui = new Ui();
        this.taskStorage = taskStorage;
        this.tasks = new TaskList();
    }

    /**
     * Starts a fresh Kafka session, period.
     *
     * @param args command-line arguments; Kafka does not currently use them
     */
    public static void main(String[] args) {
        new Kafka().run();
    }

    /**
     * Loads saved tasks and processes commands until the user says bye.
     */
    void run() {
        ui.greet();

        if (!loadTasks()) {
            ui.close();
            return;
        }

        while (true) {
            String input = ui.readCommand();
            CommandType command = CommandType.fromInput(input);
            if (command == CommandType.BYE) {
                break;
            }
            try {
                processCommand(command, input);
            } catch (KafkaException exception) {
                ui.showError(exception.getMessage());
            }
        }

        ui.sayBye();
        ui.close();
    }

    /**
     * Loads stored tasks and safely handles a corrupted or unreadable file.
     *
     * @return {@code true} when Kafka can proceed to its command loop
     */
    private boolean loadTasks() {
        try {
            tasks = taskStorage.load();
            return true;
        } catch (CorruptedTaskDataException exception) {
            ui.showError(exception.getMessage());
            if (!ui.confirmStorageOverwrite(taskStorage.getFilePath())) {
                ui.showStorageFileLocation(taskStorage.getFilePath());
                return false;
            }

            try {
                taskStorage.save(tasks);
                ui.showStorageOverwritten();
                return true;
            } catch (KafkaException saveException) {
                ui.showError(saveException.getMessage());
                ui.showStorageFileLocation(taskStorage.getFilePath());
                return false;
            }
        } catch (KafkaException exception) {
            ui.showError(exception.getMessage());
            ui.showStorageFileLocation(taskStorage.getFilePath());
            return false;
        }
    }

    /**
     * Sends a recognized command to the method that knows its business.
     *
     * @param command recognized command type
     * @param input complete input containing any command arguments
     * @throws KafkaException if parsing, task handling, or saving fails
     */
    private void processCommand(CommandType command, String input) throws KafkaException {
        switch (command) {
        case LIST -> showList();
        case TODO -> addTodo(input);
        case DEADLINE -> addDeadline(input);
        case EVENT -> addEvent(input);
        case MARK -> markTask(input);
        case UNMARK -> unmarkTask(input);
        case DELETE -> deleteTask(input);
        case UNKNOWN, BYE -> showUnknownCommand();
        }
    }

    /**
     * Displays all tasks in their current order.
     */
    private void showList() {
        ui.showTaskList(tasks);
    }

    /**
     * Parses and adds a todo.
     *
     * @param input complete todo command
     * @throws KafkaException if parsing or saving fails
     */
    private void addTodo(String input) throws KafkaException {
        addAndShow(TaskParser.parseTodo(input));
    }

    /**
     * Parses and adds a deadline.
     *
     * @param input complete deadline command
     * @throws KafkaException if parsing or saving fails
     */
    private void addDeadline(String input) throws KafkaException {
        addAndShow(TaskParser.parseDeadline(input));
    }

    /**
     * Parses and adds an event.
     *
     * @param input complete event command
     * @throws KafkaException if parsing or saving fails
     */
    private void addEvent(String input) throws KafkaException {
        addAndShow(TaskParser.parseEvent(input));
    }

    /**
     * Adds a typed task, saves the whole list, and celebrates the result.
     *
     * @param task parsed task to add
     * @throws KafkaException if the updated list cannot be saved
     */
    private void addAndShow(Task task) throws KafkaException {
        tasks.addTask(task);
        taskStorage.save(tasks);
        ui.showTaskAdded(task, tasks.size());
    }

    /**
     * Marks the task number supplied by the user.
     *
     * @param input complete mark command
     * @throws KafkaException if the number is invalid or saving fails
     */
    private void markTask(String input) throws KafkaException {
        int taskNumber = TaskParser.parseTaskNumber(input, CommandType.MARK.keyword());
        String markedTask = tasks.markTask(taskNumber);
        taskStorage.save(tasks);
        ui.showTaskMarked(markedTask);
    }

    /**
     * Unmarks the task number supplied by the user—comebacks are allowed.
     *
     * @param input complete unmark command
     * @throws KafkaException if the number is invalid or saving fails
     */
    private void unmarkTask(String input) throws KafkaException {
        int taskNumber = TaskParser.parseTaskNumber(input, CommandType.UNMARK.keyword());
        String unmarkedTask = tasks.unmarkTask(taskNumber);
        taskStorage.save(tasks);
        ui.showTaskUnmarked(unmarkedTask);
    }

    /**
     * Deletes the task number supplied by the user.
     *
     * @param input complete delete command
     * @throws KafkaException if the number is invalid or saving fails
     */
    private void deleteTask(String input) throws KafkaException {
        int taskNumber = TaskParser.parseTaskNumber(input, CommandType.DELETE.keyword());
        Task deletedTask = tasks.deleteTask(taskNumber);
        taskStorage.save(tasks);
        ui.showTaskDeleted(deletedTask, tasks.size());
    }

    /**
     * Tells the user that Kafka did not recognize their command.
     */
    private void showUnknownCommand() {
        ui.showUnknownCommand();
    }
}
