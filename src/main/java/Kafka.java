import java.util.Scanner;

public class Kafka {
    private static final String DIVIDER = "____________________________________________________________";
    private static final String BANNER = "        /\\_/\\\n"
            + "       ( o.o )     K A F K A\n"
            + "        > ^ <        \n";

    // Runs the chatbot's greeting and farewell sequence.
    public static void main(String[] args) {
        greet();
        uwuEcho();
        sayBye();
    }

    // Prints the chatbot banner and welcome message
    private static void greet() {
        System.out.println(DIVIDER);
        System.out.print(BANNER);
        System.out.println("Heyy skinny legend! >///< I'm Kafka.");
        System.out.println("What can ur kitten do for you meow?");
        System.out.println(DIVIDER);
    }

    // Prints the farewell message before the program exits.
    private static void sayBye() {
        System.out.println("Bye babe~ Hope to bump we bump into each other soon! ;)");
        System.out.println(DIVIDER);
    }

    private static void uwuEcho() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            if (input.equals("bye")) {
                break;  
            }
            String uwuInput = input.replace('l', 'w').replace('L', 'W');
            System.out.println(uwuInput + " uwu~");
        }
        scanner.close();
    }
}
