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
            if (input.equals("bye")) {
                break;
            }
            try {
                processCommand(input);
            } catch (KafkaException exception) {
                ui.showError(exception.getMessage());
            }
        }

        ui.sayBye();
        ui.close();
    }

    // Sends each command to the method responsible for handling it.
    private void processCommand(String input) throws KafkaException {
        if (input.equals("list")) {
            showList();
        } else if (input.equals("todo") || input.startsWith("todo ")) {
            addTodo(input);
        } else if (input.equals("deadline") || input.startsWith("deadline ")) {
            addDeadline(input);
        } else if (input.equals("event") || input.startsWith("event ")) {
            addEvent(input);
        } else if (input.equals("mark") || input.startsWith("mark ")) {
            markTask(input);
        } else if (input.equals("unmark") || input.startsWith("unmark ")) {
            unmarkTask(input);
        } else {
            showUnknownCommand();
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
        int taskNumber = TaskParser.parseTaskNumber(input, "mark");
        String markedTask = tasks.markTask(taskNumber);
        ui.showTaskMarked(markedTask);
    }

    // Unmarks the task number supplied by the user.
    private void unmarkTask(String input) throws KafkaException {
        int taskNumber = TaskParser.parseTaskNumber(input, "unmark");
        String unmarkedTask = tasks.unmarkTask(taskNumber);
        ui.showTaskUnmarked(unmarkedTask);
    }

    // Tells the user that their input was not a recognized command.
    private void showUnknownCommand() {
        ui.showUnknownCommand();
    }
}
