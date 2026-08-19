import java.util.Scanner;

//Runs the Kafka chatbot and echoes messages in an uwu style.
public class Kafka {
    private static final String DIVIDER = "____________________________________________________________";
    private static final String BANNER = "        /\\_/\\\n"
            + "       ( o.o )     K A F K A\n"
            + "        > ^ <        \n";


    // Runs the chatbot's greeting and farewell sequence.
    public static void main(String[] args) {
        greet();
        Scanner scanner = new Scanner(System.in);
        TaskList tasks = new TaskList();

        while (true) {
            String input = scanner.nextLine();

            if (input.equals("bye")) {
                break;
            } else if (input.equals("list")) {
                System.out.println(DIVIDER);
                System.out.println("\u001B[4mHere's your to-dos, my fav hustler >////<\u001B[0m");
                for (int i = 0; i < tasks.size(); i++) {
                    System.out.println((i + 1) + ".[ ] " + tasks.getTask(i));
                }
                System.out.println(DIVIDER);
            } else {
                String task = makeUwu(input);
                tasks.addTask(task);

                System.out.println(DIVIDER);
                System.out.println("added: " + task);
                System.out.println(DIVIDER);
            }
        }

        sayBye();
        scanner.close();
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


    //Converts a message to Kafka's uwu style.
    static String makeUwu(String input) {
        return input.replace('l', 'w').replace('L', 'W') + " uwu~";
    }



}
