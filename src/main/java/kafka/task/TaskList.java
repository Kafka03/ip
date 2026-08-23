package kafka.task;

import java.util.ArrayList;
import java.util.List;

import kafka.exception.KafkaException;

/**
 * Stores and displays the tasks entered during a Kafka session.
 */
public class TaskList {
    private static final String DIVIDER = "____________________________________________________________";
    private final ArrayList<Task> taskList;

    public TaskList() {
        taskList = new ArrayList<>();
    }

    // Adds a task
    public void addTask(Task task) {
        taskList.add(task);
    }

    // Deletes the numbered task and returns the removed task
    public Task deleteTask(int taskNumber) throws KafkaException {
        if (taskNumber < 1 || taskNumber > taskList.size()) {
            throw new KafkaException("There is no task with that number");
        }
        return taskList.remove(taskNumber - 1);
    }

    // Returns the number of tasks currently stored in this list
    public int size() {
        return taskList.size();
    }

    // Returns a read-only snapshot of the tasks
    public List<Task> getTasks() {
        return List.copyOf(taskList);
    }

    // Marks the numbered task and returns its updated display text
    public String markTask(int taskNumber) throws KafkaException {
        return getTask(taskNumber).mark();
    }

    // Unmarks the numbered task and returns its updated display text
    public String unmarkTask(int taskNumber) throws KafkaException {
        return getTask(taskNumber).unmark();
    }

    // Returns a numbered task or reports that the number is unavailable.
    private Task getTask(int taskNumber) throws KafkaException {
        if (taskNumber < 1 || taskNumber > taskList.size()) {
            throw new KafkaException("There is no task with that number");
        }
        return taskList.get(taskNumber - 1);
    }

    public void showTasks() {
        for (int i = 0; i < taskList.size(); i++) {
            System.out.println((i + 1) + "." + taskList.get(i).display());
        }
        System.out.println(DIVIDER);
    }
}
