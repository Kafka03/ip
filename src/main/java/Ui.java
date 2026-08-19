import java.util.Scanner;

/**
 * Handles all console input and output for the Kafka chatbot.
 */
class Ui {
    private static final String DIVIDER = "____________________________________________________________";
    private static final String BANNER = "        /\\_/\\\n"
            + "       ( o.o )     K A F K A\n"
            + "        > 0 <        \n";
    private final Scanner scanner;

    Ui() {
        this.scanner = new Scanner(System.in);
    }

    // Reads the user's next command.
    String readCommand() {
        return scanner.nextLine();
    }

    // Displays all tasks in the list.
    void showTaskList(TaskList tasks) {
        System.out.println(DIVIDER);
        System.out.println("\u001B[4mHere's your to-dos, my fav hustler >////<\u001B[0m");
        tasks.showTasks();
    }

    // Confirms that a typed task was added and displays the new task count.
    void showTaskAdded(Task task, int taskCount) {
        System.out.println(DIVIDER);
        System.out.println("Yippee!!! I've added this task:");
        System.out.println("  " + task.display());
        System.out.println("Now you have " + taskCount + " tasks in the list. What a legend.");
        System.out.println(DIVIDER);
    }

    // Confirms that a task was marked as done.
    void showTaskMarked(String task) {
        System.out.println(DIVIDER);
        System.out.println("Ur such a baddie!! I've marked this task as done:");
        System.out.println("  " + task);
        System.out.println(DIVIDER);
    }

    // Confirms that a task was marked as not done.
    void showTaskUnmarked(String task) {
        System.out.println(DIVIDER);
        System.out.println("Awww issok my g, I've marked this task as not done yet:");
        System.out.println("  " + task);
        System.out.println(DIVIDER);
    }

    // Displays the original response for a task without a typed command.
    void showPlainTaskAdded(String task) {
        System.out.println(DIVIDER);
        System.out.println("(ꈍ ω ꈍ) added: " + task);
        System.out.println(DIVIDER);
    }

    // Prints the chatbot banner and welcome message.
    void greet() {
        System.out.println(DIVIDER);
        System.out.print(BANNER);
        System.out.println("Heyy skinny legend! (⊃✿ ･ิω･ิ)⊃ I'm Kafka.");
        System.out.println("What can ur kitten do for you meow? (≧◡≦)");
        System.out.println(DIVIDER);
    }

    // Prints the farewell message.
    void sayBye() {
        System.out.println("Bye babe~ Hope we bump into each other soon!");
        System.out.println(DIVIDER);
    }

    // Releases the scanner when the chatbot session ends.
    void close() {
        scanner.close();
    }
}
