package kafka.task;

import java.util.ArrayList;
import java.util.List;

import kafka.exception.KafkaException;

/**
 * Keeps the user's task squad together in its current order.
 */
public class TaskList {
    private static final String DIVIDER = "____________________________________________________________";
    private final ArrayList<Task> taskList;

    /**
     * Creates an empty task list, ready for the grind.
     */
    public TaskList() {
        taskList = new ArrayList<>();
    }

    /**
     * Adds a task to the end of the list.
     *
     * @param task task to remember
     */
    public void addTask(Task task) {
        taskList.add(task);
    }

    /**
     * Deletes the task at the user-facing one-based position.
     *
     * @param taskNumber one-based number of the task to yeet
     * @return task removed from the list
     * @throws KafkaException if no task has that number
     */
    public Task deleteTask(int taskNumber) throws KafkaException {
        if (taskNumber < 1 || taskNumber > taskList.size()) {
            throw new KafkaException("There is no task with that number");
        }
        return taskList.remove(taskNumber - 1);
    }

    /**
     * Returns how many tasks are currently serving in the list.
     *
     * @return number of stored tasks
     */
    public int size() {
        return taskList.size();
    }

    /**
     * Returns a read-only snapshot for storage without exposing the mutable list.
     *
     * @return immutable copy of the tasks in list order
     */
    public List<Task> getTasks() {
        return List.copyOf(taskList);
    }

    /**
     * Marks the task at the user-facing one-based position as completed.
     *
     * @param taskNumber one-based number of the task to mark
     * @return updated display text for the marked task
     * @throws KafkaException if no task has that number
     */
    public String markTask(int taskNumber) throws KafkaException {
        return getTask(taskNumber).mark();
    }

    /**
     * Marks the task at the user-facing one-based position as incomplete.
     *
     * @param taskNumber one-based number of the task to unmark
     * @return updated display text for the unmarked task
     * @throws KafkaException if no task has that number
     */
    public String unmarkTask(int taskNumber) throws KafkaException {
        return getTask(taskNumber).unmark();
    }

    /**
     * Finds the task at a user-facing one-based position.
     *
     * @param taskNumber one-based number of the requested task
     * @return matching task
     * @throws KafkaException if no task has that number
     */
    private Task getTask(int taskNumber) throws KafkaException {
        if (taskNumber < 1 || taskNumber > taskList.size()) {
            throw new KafkaException("There is no task with that number");
        }
        return taskList.get(taskNumber - 1);
    }

    /**
     * Prints every task with its one-based list number.
     */
    public void showTasks() {
        for (int i = 0; i < taskList.size(); i++) {
            System.out.println((i + 1) + "." + taskList.get(i).display());
        }
        System.out.println(DIVIDER);
    }
}
