import java.util.ArrayList;

// Stores the tasks entered during a KafkaBot session
public class TaskList {
    private final ArrayList<String> tasks;

    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    public void addTask(String task) {
        tasks.add(task);
    }

    public int size() {
        return tasks.size();
    }

    public String getTask(int index) {
        return tasks.get(index);
    }
}

