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
 * Coordinates the user interface, command parser, task storage, and task list.
 */
public class Kafka {
    private final Ui ui;
    private final TaskStorage taskStorage;
    private TaskList tasks;
    private boolean isLoaded;
    private boolean wasLastResponseError;

    /**
     * Creates Kafka with its usual {@code data/kafka.txt} storage file.
     */
    public Kafka() {
        this(new TaskStorage());
    }

    /**
     * Creates Kafka with the specified task storage service.
     *
     * @param taskStorage storage service Kafka should use for this session
     */
    Kafka(TaskStorage taskStorage) {
        this.ui = new Ui();
        this.taskStorage = taskStorage;
        this.tasks = new TaskList();
        this.isLoaded = false;
        this.wasLastResponseError = false;
    }

    /**
     * Starts a Kafka session.
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
            System.out.println(getResponse(input));
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
            isLoaded = true;
            return true;
        } catch (CorruptedTaskDataException exception) {
            ui.showError(exception.getMessage());
            if (!ui.confirmStorageOverwrite(taskStorage.getFilePath())) {
                ui.showStorageFileLocation(taskStorage.getFilePath());
                return false;
            }

            try {
                taskStorage.save(tasks);
                isLoaded = true;
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
     * Dispatches a recognized command to the corresponding handler.
     *
     * @param command recognized command type
     * @param input complete input containing any command arguments
     * @throws KafkaException if parsing, task handling, or saving fails
     */
    private String processCommand(CommandType command, String input) throws KafkaException {
        return switch (command) {
            case LIST -> listTasks();
            case TODO -> addTodo(input);
            case DEADLINE -> addDeadline(input);
            case EVENT -> addEvent(input);
            case MARK -> markTask(input);
            case UNMARK -> unmarkTask(input);
            case DELETE -> deleteTask(input);
            case FIND -> findTasks(input);
            case UNKNOWN, BYE -> handleUnknownCommand();
            default -> handleUnknownCommand();
        };
    }

    /**
     * Displays all tasks in their current order.
     */
    private String listTasks() {
        return ui.formatTaskList(tasks);
    }

    /**
     * Parses and adds a todo.
     *
     * @param input complete todo command
     * @throws KafkaException if parsing or saving fails
     */
    private String addTodo(String input) throws KafkaException {
        return addAndGetResponse(TaskParser.parseTodo(input));
    }

    /**
     * Parses and adds a deadline.
     *
     * @param input complete deadline command
     * @throws KafkaException if parsing or saving fails
     */
    private String addDeadline(String input) throws KafkaException {
        return addAndGetResponse(TaskParser.parseDeadline(input));
    }

    /**
     * Parses and adds an event.
     *
     * @param input complete event command
     * @throws KafkaException if parsing or saving fails
     */
    private String addEvent(String input) throws KafkaException {
        return addAndGetResponse(TaskParser.parseEvent(input));
    }

    /**
     * Adds a task, saves the updated list, and displays a confirmation.
     *
     * @param task parsed task to add
     * @throws KafkaException if the updated list cannot be saved
     */
    private String addAndGetResponse(Task task) throws KafkaException {
        tasks.addTask(task);
        taskStorage.save(tasks);
        return ui.formatTaskAdded(task, tasks.size());
    }

    /**
     * Marks the task number supplied by the user.
     *
     * @param input complete mark command
     * @throws KafkaException if the number is invalid or saving fails
     */
    private String markTask(String input) throws KafkaException {
        int taskNumber = TaskParser.parseTaskNumber(input, CommandType.MARK.keyword());
        String markedTask = tasks.markTask(taskNumber);
        taskStorage.save(tasks);
        return ui.formatTaskMarked(markedTask);
    }

    /**
     * Unmarks the task number supplied by the user.
     *
     * @param input complete unmark command
     * @throws KafkaException if the number is invalid or saving fails
     */
    private String unmarkTask(String input) throws KafkaException {
        int taskNumber = TaskParser.parseTaskNumber(input, CommandType.UNMARK.keyword());
        String unmarkedTask = tasks.unmarkTask(taskNumber);
        taskStorage.save(tasks);
        return ui.formatTaskUnmarked(unmarkedTask);
    }

    /**
     * Deletes the task number supplied by the user.
     *
     * @param input complete delete command
     * @throws KafkaException if the number is invalid or saving fails
     */
    private String deleteTask(String input) throws KafkaException {
        int taskNumber = TaskParser.parseTaskNumber(input, CommandType.DELETE.keyword());
        Task deletedTask = tasks.deleteTask(taskNumber);
        taskStorage.save(tasks);
        return ui.formatTaskDeleted(deletedTask, tasks.size());
    }

    /**
     * Finds and displays tasks containing the keyword supplied by the user.
     * Searching does not change the task list, so no save is needed.
     *
     * @param input complete find command
     * @throws KafkaException if no search keyword was supplied
     */
    private String findTasks(String input) throws KafkaException {
        String keyword = TaskParser.parseFindKeyword(input);
        return ui.formatMatchingTasks(tasks.findTasks(keyword));
    }

    /**
     * Tells the user that Kafka did not recognize their command.
     */
    private String handleUnknownCommand() {
        return ui.formatUnknownCommand();
    }

    /**
     * Processes one command and returns its response for any user interface.
     * Saved tasks are loaded lazily when a GUI submits its first command.
     *
     * @param input complete command entered by the user
     * @return response ready to display to the user
     */
    public String getResponse(String input) {
        CommandType command = CommandType.fromInput(input);
        wasLastResponseError = command == CommandType.UNKNOWN;
        if (command == CommandType.BYE) {
            return ui.formatFarewell();
        }

        try {
            ensureTasksLoaded();
            return processCommand(command, input);
        } catch (KafkaException exception) {
            wasLastResponseError = true;
            return ui.formatError(exception.getMessage());
        }
    }

    /**
     * Returns whether the most recently generated response reports an error.
     *
     * @return {@code true} if the latest response is an error
     */
    public boolean wasLastResponseError() {
        return wasLastResponseError;
    }

    /**
     * Loads saved tasks once before processing GUI commands.
     *
     * @throws KafkaException if the saved tasks cannot be loaded
     */
    private void ensureTasksLoaded() throws KafkaException {
        if (!isLoaded) {
            tasks = taskStorage.load();
            isLoaded = true;
        }
    }

    public String greet() {
        return ui.formatGreeting();
    }
}
