/**
 * Runs the Kafka chatbot and coordinates commands between the UI, parser,
 * and task list.
 */
public class Kafka {
    private final Ui ui;
    private final TaskList tasks;

    Kafka() {
        this.ui = new Ui();
        this.tasks = new TaskList();
    }

    // Starts a Kafka chatbot session.
    public static void main(String[] args) {
        new Kafka().run();
    }

    // Reads and processes commands until the user exits.
    private void run() {
        ui.greet();

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

    // Sends each command to the method responsible for handling it.
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

    // Displays all tasks in their current order.
    private void showList() {
        ui.showTaskList(tasks);
    }

    // Parses and adds a todo.
    private void addTodo(String input) throws ParserException {
        addAndShow(TaskParser.parseTodo(input));
    }

    // Parses and adds a deadline.
    private void addDeadline(String input) throws ParserException {
        addAndShow(TaskParser.parseDeadline(input));
    }

    // Parses and adds an event.
    private void addEvent(String input) throws ParserException {
        addAndShow(TaskParser.parseEvent(input));
    }

    // Stores a typed task and displays the result.
    private void addAndShow(Task task) {
        tasks.addTask(task);
        ui.showTaskAdded(task, tasks.size());
    }

    // Marks the task number supplied by the user.
    private void markTask(String input) throws KafkaException {
        int taskNumber = TaskParser.parseTaskNumber(input, CommandType.MARK.keyword());
        String markedTask = tasks.markTask(taskNumber);
        ui.showTaskMarked(markedTask);
    }

    // Unmarks the task number supplied by the user.
    private void unmarkTask(String input) throws KafkaException {
        int taskNumber = TaskParser.parseTaskNumber(input, CommandType.UNMARK.keyword());
        String unmarkedTask = tasks.unmarkTask(taskNumber);
        ui.showTaskUnmarked(unmarkedTask);
    }

    // Deletes the task number supplied by the user.
    private void deleteTask(String input) throws ParserException {
        int taskNumber = TaskParser.parseTaskNumber(input, CommandType.DELETE.keyword());
        Task deletedTask = tasks.deleteTask(taskNumber);
        ui.showTaskDeleted(deletedTask, tasks.size());
    }

    // Tells the user that their input was not a recognized command.
    private void showUnknownCommand() {
        ui.showUnknownCommand();
    }
}
