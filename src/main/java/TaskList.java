import java.util.ArrayList;

// Stores and displays the tasks entered during a KafkaBot session
class TaskList {
    private static final String DIVIDER = "____________________________________________________________";
    private final ArrayList<Task> taskList;

    TaskList() {
        taskList = new ArrayList<>();
    }

    void addTask(String description) {
        addTask(new Task(description));
    }

    // Adds a task
    void addTask(Task task) {
        taskList.add(task);
    }

    // Returns the number of tasks currently stored in this list
    int size() {
        return taskList.size();
    }

    // Marks the numbered task and returns its updated display text
    String markTask(int taskNumber) {
        return taskList.get(taskNumber - 1).mark();
    }

    // Unmarks the numbered task and returns its updated display text
    String unmarkTask(int taskNumber) {
        return taskList.get(taskNumber - 1).unmark();
    }

    void showTasks() {
        for (int i = 0; i < taskList.size(); i++) {
            System.out.println((i + 1) + "." + taskList.get(i).display());
        }
        System.out.println(DIVIDER);
    }
}
