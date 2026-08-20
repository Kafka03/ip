import java.util.ArrayList;

// Stores and displays the tasks entered during a KafkaBot session
class TaskList {
    private static final String DIVIDER = "____________________________________________________________";
    private final ArrayList<Task> taskList;

    TaskList() {
        taskList = new ArrayList<>();
    }

    // Adds a task
    void addTask(Task task) {
        taskList.add(task);
    }

    // Deletes the numbered task and returns the removed task
    Task deleteTask(int taskNumber) {
        return taskList.remove(taskNumber - 1);
    }

    // Returns the number of tasks currently stored in this list
    int size() {
        return taskList.size();
    }

    // Marks the numbered task and returns its updated display text
    String markTask(int taskNumber) throws KafkaException {
        return getTask(taskNumber).mark();
    }

    // Unmarks the numbered task and returns its updated display text
    String unmarkTask(int taskNumber) throws KafkaException {
        return getTask(taskNumber).unmark();
    }

    // Returns a numbered task or reports that the number is unavailable.
    private Task getTask(int taskNumber) throws KafkaException {
        if (taskNumber < 1 || taskNumber > taskList.size()) {
            throw new KafkaException("Internal: There is no task with that number");
        }
        return taskList.get(taskNumber - 1);
    }

    void showTasks() {
        for (int i = 0; i < taskList.size(); i++) {
            System.out.println((i + 1) + "." + taskList.get(i).display());
        }
        System.out.println(DIVIDER);
    }
}
