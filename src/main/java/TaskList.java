import java.util.ArrayList;

// Stores and displays the tasks entered during a KafkaBot session
public class TaskList {
    private static final String DIVIDER = "____________________________________________________________";
    private final ArrayList<String> taskList;

    // Creates an empty task list.
    public TaskList() {
        this.taskList = new ArrayList<>();
    }

    // Adds a task to the end of the private task list.
    public void addTask(String task) {
        taskList.add(task);
    }

    // Displays the private task list with numbered unchecked markers.
    public void showTasks() {
        for (int i = 0; i < taskList.size(); i++) {
            System.out.println((i + 1) + ".[ ] " + taskList.get(i));
        }
        System.out.println(DIVIDER);
    }
}
