/**
 * Greets the user when the chatbot starts and says goodbye before it exits.
 */
public class Kafka {
    private static final String DIVIDER = "____________________________________________________________";
    private static final String BANNER = " _  __      __ _         \n"
            + "| |/ /__ _ / _| | ____ _ \n"
            + "| ' // _` | |_| |/ / _` |\n"
            + "| . \\ (_| |  _|   < (_| |\n"
            + "|_|\\_\\__,_|_| |_|\\_\\__,_|\n";

    // Runs the chatbot's greeting and farewell sequence.
    public static void main(String[] args) {
        greet();
        sayBye();
    }

    // Prints the chatbot banner and welcome message
    private static void greet() {
        System.out.println(DIVIDER);
        System.out.print(BANNER);
        System.out.println("Hello! I'm Kafka.");
        System.out.println("What can I do for you?");
        System.out.println(DIVIDER);
    }

    // Prints the farewell message before the program exits.
    private static void sayBye() {
        System.out.println("Bye. Hope to see you again soon!");
        System.out.println(DIVIDER);
    }
}
