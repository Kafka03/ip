import java.util.Scanner;

//Runs the Kafka chatbot and echoes messages in an uwu style.
public class Kafka {
    private static final String DIVIDER = "____________________________________________________________";
    private static final String BANNER = "        /\\_/\\\n"
            + "       ( o.o )     K A F K A\n"
            + "        > 0 <        \n";


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
                System.out.println("\u001B[4mHere's your to-dos, my fav hustler >////<\u001B[0m");
                tasks.showTasks();
            } else {
                String task = makeUwu(input);
                tasks.addTask(task);

                System.out.println(DIVIDER);
                System.out.println("(ꈍ ω ꈍ) added: " + task);
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
        System.out.println("Heyy skinny legend! (⊃✿ ･ิω･ิ)⊃ I'm Kafka.");
        System.out.println("What can ur kitten do for you meow? (≧◡≦)");
        System.out.println(DIVIDER);
    }


    // Prints the farewell message before the program exits.
    private static void sayBye() {
        System.out.println("Bye babe~ Hope we bump into each other soon!");
        System.out.println(DIVIDER);
    }


    //Converts a message to Kafka's uwu style.
    static String makeUwu(String input) {
        return input.replace('l', 'w').replace('L', 'W') + " uwu~";
    }



}
