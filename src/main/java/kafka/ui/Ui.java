package kafka.ui;

import java.nio.file.Path;
import java.util.Scanner;

import kafka.task.Task;
import kafka.task.TaskList;

/**
 * Handles console input and output for the Kafka chatbot.
 */
public class Ui {
    private static final String DIVIDER = "____________________________________________________________";
    private static final String BANNER = "        /\\_/\\\n"
            + "       ( o.o )     K A F K A\n"
            + "        > 0 <        \n";
    private final Scanner scanner;

    public Ui() {
        this.scanner = new Scanner(System.in);
    }

    // Reads the user's next command.
    public String readCommand() {
        return scanner.nextLine();
    }

    // Displays all tasks in the list.
    public void showTaskList(TaskList tasks) {
        System.out.println(DIVIDER);
        System.out.println("\u001B[4mHere's your to-dos, my fav hustler >////<\u001B[0m");
        if (tasks.size() == 0) {
            System.out.println("\nYou have no tasks lined up king >0<");
        }
        tasks.showTasks();
    }

    // Confirms that a typed task was added and displays the new task count.
    public void showTaskAdded(Task task, int taskCount) {
        System.out.println(DIVIDER);
        System.out.println("Yippee!!! I've added this task:");
        System.out.println("  " + task.display());
        String taskWord = taskCount == 1 ? "task" : "tasks";
        System.out.println("Now you have " + taskCount + " " + taskWord
                + " in the list. What a legend.");
        System.out.println(DIVIDER);
    }

    // Confirms that a task was marked as done.
    public void showTaskMarked(String task) {
        System.out.println(DIVIDER);
        System.out.println("Ur such a baddie!! I've marked this task as done:");
        System.out.println("  " + task);
        System.out.println(DIVIDER);
    }

    // Confirms that a task was marked as not done.
    public void showTaskUnmarked(String task) {
        System.out.println(DIVIDER);
        System.out.println("Awww issok my g, I've marked this task as not done yet:");
        System.out.println("  " + task);
        System.out.println(DIVIDER);
    }

    // Confirms that a task was deleted and displays the new task count.
    public void showTaskDeleted(Task task, int taskCount) {
        System.out.println(DIVIDER);
        System.out.println("Aight. I've yeeted this task:");
        System.out.println("  " + task.display());
        String taskWord = taskCount == 1 ? "task" : "tasks";
        System.out.println("Now you have " + taskCount + " " + taskWord + " in the list.");
        System.out.println(DIVIDER);
    }

    // Explains that unrecognized input cannot be added as a task.
    public void showUnknownCommand() {
        System.out.println(DIVIDER);
        System.out.println("Sowwy I don't know that command... pwease try todo, deadline, event, list, mark, unmark, delete, or bye.");
        System.out.println(DIVIDER);
    }

    // Displays an expected error without ending the chatbot session.
    public void showError(String message) {
        System.out.println(DIVIDER);
        System.out.println(message);
        System.out.println(DIVIDER);
    }

    /**
     * Asks for explicit permission before replacing a corrupted task file.
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
     */
    public void showStorageFileLocation(Path filePath) {
        System.out.println("Your task data was not changed.");
        System.out.println("Please inspect or repair this file before restarting Kafka:");
        System.out.println("  " + filePath);
        System.out.println(DIVIDER);
    }

    // Confirms that the user-approved corrupted file was replaced.
    public void showStorageOverwritten() {
        System.out.println("The corrupted task file was replaced. Starting with an empty list.");
        System.out.println(DIVIDER);
    }

    // Prints the chatbot banner and welcome message.
    public void greet() {
        System.out.println(DIVIDER);
        System.out.print(BANNER);
        System.out.println("Heyy skinny legend! (⊃✿ ･ิω･ิ)⊃ I'm Kafka.");
        System.out.println("What can ur kitten do for you meow? (≧◡≦)");
        System.out.println(DIVIDER);
    }

    // Prints the farewell message.
    public void sayBye() {
        System.out.println("Bye babe~ Hope we bump into each other soon!");
        System.out.println(DIVIDER);
    }

    // Releases the scanner when the chatbot session ends.
    public void close() {
        scanner.close();
    }
}
