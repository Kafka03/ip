package kafka.ui;

import java.nio.file.Path;
import java.util.List;
import java.util.Scanner;

import kafka.task.Task;
import kafka.task.TaskList;

/**
 * Handles console input and displays messages to the user.
 */
public class Ui {
    private static final String DIVIDER = "_".repeat(60);
    private static final String UNKNOWN_COMMAND_MESSAGE =
            "Sowwy I don't know that command... pwease try todo, deadline, event, "
            + "list, find, mark, unmark, delete, or bye.";
    private static final String BANNER = "        /\\_/\\\n"
            + "       ( o.o )     K A F K A\n"
            + "        > 0 <        \n";
    private final Scanner scanner;

    /**
     * Creates a console UI that reads from standard input.
     */
    public Ui() {
        this.scanner = new Scanner(System.in);
    }

    /**
     * Reads the user's next command from the console.
     *
     * @return complete line entered by the user
     */
    public String readCommand() {
        return scanner.nextLine();
    }

    /**
     * Displays every task or an empty-list message.
     *
     * @param tasks task list to display
     */
    public void showTaskList(TaskList tasks) {
        System.out.println(formatTaskList(tasks));
    }

    /**
     * Returns the formatted task list for display by any user interface.
     *
     * @param tasks task list to display
     * @return formatted task-list response
     */
    public String formatTaskList(TaskList tasks) {
        StringBuilder response = new StringBuilder(DIVIDER)
                .append('\n')
                .append("Here's your to-dos, my fav hustler >////<")
                .append('\n');
        if (tasks.isEmpty()) {
            response.append('\n').append("You have no tasks lined up king >0<").append('\n');
        }
        for (int i = 0; i < tasks.size(); i++) {
            response.append(i + 1)
                    .append('.')
                    .append(tasks.getTasks().get(i).display())
                    .append('\n');
        }
        return response.append(DIVIDER).toString();
    }

    /**
     * Displays tasks returned by a keyword search, numbered from one.
     *
     * @param matchingTasks tasks whose displayed text contains the keyword
     */
    public void showMatchingTasks(List<Task> matchingTasks) {
        System.out.println(formatMatchingTasks(matchingTasks));
    }

    /**
     * Returns matching tasks with one-based result numbers.
     *
     * @param matchingTasks tasks whose displayed text contains the keyword
     * @return formatted matching-task response
     */
    public String formatMatchingTasks(List<Task> matchingTasks) {
        StringBuilder response = new StringBuilder(DIVIDER)
                .append('\n')
                .append("I worked hard to find the matching tasks in your list king:")
                .append('\n');
        for (int i = 0; i < matchingTasks.size(); i++) {
            response.append(i + 1)
                    .append('.')
                    .append(matchingTasks.get(i).display())
                    .append('\n');
        }
        return response.append(DIVIDER).toString();
    }

    /**
     * Confirms that a task was added and displays the updated count.
     *
     * @param task task that joined the list
     * @param taskCount number of tasks now stored
     */
    public void showTaskAdded(Task task, int taskCount) {
        System.out.println(formatTaskAdded(task, taskCount));
    }

    /**
     * Returns confirmation that a task was added.
     *
     * @param task task that joined the list
     * @param taskCount number of tasks now stored
     * @return formatted task-added response
     */
    public String formatTaskAdded(Task task, int taskCount) {
        String taskWord = taskCount == 1 ? "task" : "tasks";
        return DIVIDER + "\n"
                + "Yippee!!! I've added this task:\n"
                + "  " + task.display() + "\n"
                + "Now you have " + taskCount + " " + taskWord
                + " in the list. What a legend.\n"
                + DIVIDER;
    }

    /**
     * Confirms that a task was marked as completed.
     *
     * @param task updated display text for the completed task
     */
    public void showTaskMarked(String task) {
        System.out.println(formatTaskMarked(task));
    }

    /**
     * Returns confirmation that a task was marked as completed.
     *
     * @param task updated display text for the completed task
     * @return formatted task-marked response
     */
    public String formatTaskMarked(String task) {
        return DIVIDER + "\n"
                + "Ur such a baddie!! I've marked this task as done:\n"
                + "  " + task + "\n"
                + DIVIDER;
    }

    /**
     * Confirms that a task was marked as incomplete.
     *
     * @param task updated display text for the incomplete task
     */
    public void showTaskUnmarked(String task) {
        System.out.println(formatTaskUnmarked(task));
    }

    /**
     * Returns confirmation that a task was marked as incomplete.
     *
     * @param task updated display text for the incomplete task
     * @return formatted task-unmarked response
     */
    public String formatTaskUnmarked(String task) {
        return DIVIDER + "\n"
                + "Awww issok my g, I've marked this task as not done yet:\n"
                + "  " + task + "\n"
                + DIVIDER;
    }

    /**
     * Confirms that a task was deleted and displays the updated count.
     *
     * @param task task removed from the list
     * @param taskCount number of tasks still stored
     */
    public void showTaskDeleted(Task task, int taskCount) {
        System.out.println(formatTaskDeleted(task, taskCount));
    }

    /**
     * Returns confirmation that a task was deleted.
     *
     * @param task task removed from the list
     * @param taskCount number of tasks still stored
     * @return formatted task-deleted response
     */
    public String formatTaskDeleted(Task task, int taskCount) {
        String taskWord = taskCount == 1 ? "task" : "tasks";
        return DIVIDER + "\n"
                + "Aight. I've yeeted this task:\n"
                + "  " + task.display() + "\n"
                + "Now you have " + taskCount + " " + taskWord + " in the list.\n"
                + DIVIDER;
    }

    /**
     * Informs the user that the command is not recognized.
     */
    public void showUnknownCommand() {
        System.out.println(formatUnknownCommand());
    }

    /**
     * Returns the response for an unrecognized command.
     *
     * @return formatted unknown-command response
     */
    public String formatUnknownCommand() {
        return DIVIDER + "\n" + UNKNOWN_COMMAND_MESSAGE + "\n" + DIVIDER;
    }

    /**
     * Displays an expected error without ending the chatbot session.
     *
     * @param message user-facing explanation of the problem
     */
    public void showError(String message) {
        System.out.println(formatError(message));
    }

    /**
     * Returns an expected error for display by any user interface.
     *
     * @param message user-facing explanation of the problem
     * @return formatted error response
     */
    public String formatError(String message) {
        return DIVIDER + "\n" + message + "\n" + DIVIDER;
    }

    /**
     * Asks for explicit permission before replacing a corrupted task file.
     * Repeats the prompt until the user enters yes or no.
     *
     * @param filePath corrupted task file that would be replaced
     * @return {@code true} only when the user approves the overwrite
     */
    public boolean confirmStorageOverwrite(Path filePath) {
        while (true) {
            System.out.println("The task data file may be corrupted:");
            System.out.println("  " + filePath);
            System.out.print("Overwrite it with an empty task list and continue? (yes/no): ");

            if (!scanner.hasNextLine()) {
                return false;
            }
            String response = scanner.nextLine().trim();
            if (response.equalsIgnoreCase("yes") || response.equalsIgnoreCase("y")) {
                return true;
            }
            if (response.equalsIgnoreCase("no") || response.equalsIgnoreCase("n")) {
                return false;
            }
            System.out.println("Please enter yes or no.");
        }
    }

    /**
     * Directs the user to the file that needs to be inspected or repaired.
     *
     * @param filePath task file the user should inspect
     */
    public void showStorageFileLocation(Path filePath) {
        System.out.println("Your task data was not changed.");
        System.out.println("Please inspect or repair this file before restarting Kafka:");
        System.out.println("  " + filePath);
        System.out.println(DIVIDER);
    }

    /**
     * Confirms that the user-approved corrupted file was replaced.
     */
    public void showStorageOverwritten() {
        System.out.println("The corrupted task file was replaced. Starting with an empty list.");
        System.out.println(DIVIDER);
    }

    /**
     * Prints Kafka's banner and welcome message.
     */
    public void greet() {
        System.out.println(DIVIDER);
        System.out.print(BANNER);
        System.out.println("Heyy skinny legend! (⊃✿ ･ิω･ิ)⊃ I'm Kafka.");
        System.out.println("What can ur kitten do for you meow? (≧◡≦)");
        System.out.println(DIVIDER);
    }

    /**
     * Prints Kafka's farewell message.
     */
    public void sayBye() {
        System.out.println(formatFarewell());
    }

    /**
     * Returns Kafka's farewell response.
     *
     * @return formatted farewell response
     */
    public String formatFarewell() {
        return "Bye babe~ Hope we bump into each other soon!\n" + DIVIDER;
    }

    /**
     * Releases the console scanner when the chatbot session ends.
     */
    public void close() {
        scanner.close();
    }
}
