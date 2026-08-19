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
            processCommand(input);
        }

        ui.sayBye();
        ui.close();
    }

    // Sends each command to the method responsible for handling it.
    private void processCommand(String input) {
        if (input.equals("list")) {
            showList();
        } else if (input.startsWith("todo ")) {
            addTodo(input);
        } else if (input.startsWith("deadline ")) {
            addDeadline(input);
        } else if (input.startsWith("event ")) {
            addEvent(input);
        } else if (input.startsWith("mark ")) {
            markTask(input);
        } else if (input.startsWith("unmark ")) {
            unmarkTask(input);
        } else {
            addPlainTask(input);
        }
    }

    // Displays all tasks in their current order.
    private void showList() {
        ui.showTaskList(tasks);
    }

    // Parses and adds a todo.
    private void addTodo(String input) {
        addAndShow(TaskParser.parseTodo(input));
    }

    // Parses and adds a deadline.
    private void addDeadline(String input) {
        addAndShow(TaskParser.parseDeadline(input));
    }

    // Parses and adds an event.
    private void addEvent(String input) {
        addAndShow(TaskParser.parseEvent(input));
    }

    // Stores a typed task and displays the result.
    private void addAndShow(Task task) {
        tasks.addTask(task);
        ui.showTaskAdded(task, tasks.size());
    }

    // Marks the task number supplied by the user.
    private void markTask(String input) {
        int taskNumber = Integer.parseInt(input.substring("mark ".length()));
        String markedTask = tasks.markTask(taskNumber);
        ui.showTaskMarked(markedTask);
    }

    // Unmarks the task number supplied by the user.
    private void unmarkTask(String input) {
        int taskNumber = Integer.parseInt(input.substring("unmark ".length()));
        String unmarkedTask = tasks.unmarkTask(taskNumber);
        ui.showTaskUnmarked(unmarkedTask);
    }

    // Preserves the original behavior for input without a recognized command.
    private void addPlainTask(String input) {
        String task = makeUwu(input);
        tasks.addTask(task);
        ui.showPlainTaskAdded(task);
    }

    // Converts a message to Kafka's uwu style.
    static String makeUwu(String input) {
        return input.replace('l', 'w').replace('L', 'W') + " uwu~";
    }
}
