package kafka.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import kafka.exception.KafkaException;

/**
 * Stores and manages tasks in list order.
 */
public class TaskList {
    private static final String TASK_NOT_FOUND_ERROR =
            "There is no task with that number";
    private static final String NOT_DEADLINE_ERROR =
            "Only a deadline can be snoozed with /by.";
    private static final String NOT_EVENT_ERROR =
            "Only an event can be snoozed with /from or /to.";
    private static final String EMPTY_EVENT_SNOOZE_ASSERTION =
            "A parsed event snooze must change at least one endpoint";

    private final List<Task> taskList;

    /**
     * Creates an empty task list.
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
     * @param taskNumber one-based number of the task to delete
     * @return task removed from the list
     * @throws KafkaException if no task has that number
     */
    public Task deleteTask(int taskNumber) throws KafkaException {
        return taskList.remove(getTaskIndex(taskNumber));
    }

    /**
     * Returns the number of tasks in the list.
     *
     * @return number of stored tasks
     */
    public int size() {
        return taskList.size();
    }

    /**
     * Reports whether the list has no tasks waiting in it.
     *
     * @return {@code true} when the list contains no tasks
     */
    public boolean isEmpty() {
        return taskList.isEmpty();
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
     * Finds tasks whose displayed text contains the supplied keyword.
     * Matching ignores letter case and preserves the tasks' original order.
     *
     * @param keyword text to search for
     * @return immutable list of matching tasks in their original order
     */
    public List<Task> findTasks(String keyword) {
        String normalizedKeyword = keyword.toLowerCase(Locale.ROOT);
        return taskList.stream()
                .filter(task -> task.display().toLowerCase(Locale.ROOT)
                        .contains(normalizedKeyword))
                .toList();
    }

    /**
     * Marks the task at the user-facing one-based position as completed.
     *
     * @param taskNumber one-based number of the task to mark
     * @return task that was marked
     * @throws KafkaException if no task has that number
     */
    public Task markTask(int taskNumber) throws KafkaException {
        Task task = getTask(taskNumber);
        task.mark();
        return task;
    }

    /**
     * Marks the task at the user-facing one-based position as incomplete.
     *
     * @param taskNumber one-based number of the task to unmark
     * @return task that was unmarked
     * @throws KafkaException if no task has that number
     */
    public Task unmarkTask(int taskNumber) throws KafkaException {
        Task task = getTask(taskNumber);
        task.unmark();
        return task;
    }

    /**
     * Renames the task at the specified one-based position.
     *
     * @param taskNumber one-based task number
     * @param newName replacement task name
     * @return display snapshots from before and after the rename
     * @throws KafkaException if no task has that number
     */
    public RenameResult renameTask(int taskNumber, String newName) throws KafkaException {
        Task task = getTask(taskNumber);
        String oldDisplay = task.display();
        task.rename(newName);
        return new RenameResult(oldDisplay, task.display());
    }

    /**
     * Reschedules the deadline at the specified one-based position.
     *
     * @param taskNumber one-based task number
     * @param newBy replacement deadline
     * @return display snapshots from before and after rescheduling
     * @throws KafkaException if the selected task is absent or is not a deadline
     */
    public SnoozeResult snoozeDeadline(int taskNumber, String newBy)
            throws KafkaException {
        Task task = getTask(taskNumber);
        if (!(task instanceof Deadline deadline)) {
            throw new KafkaException(NOT_DEADLINE_ERROR);
        }

        String oldDisplay = deadline.display();
        deadline.reschedule(newBy);
        return new SnoozeResult(oldDisplay, deadline.display());
    }

    /**
     * Reschedules one or both endpoints of the event at the specified position.
     *
     * @param taskNumber one-based task number
     * @param newFrom replacement start, if supplied
     * @param newTo replacement end, if supplied
     * @return display snapshots from before and after rescheduling
     * @throws KafkaException if the selected task is absent or is not an event
     */
    public SnoozeResult snoozeEvent(int taskNumber, Optional<String> newFrom,
            Optional<String> newTo) throws KafkaException {
        assert newFrom.isPresent() || newTo.isPresent()
                : EMPTY_EVENT_SNOOZE_ASSERTION;

        Task task = getTask(taskNumber);
        if (!(task instanceof Event event)) {
            throw new KafkaException(NOT_EVENT_ERROR);
        }

        String oldDisplay = event.display();
        newFrom.ifPresent(event::rescheduleFrom);
        newTo.ifPresent(event::rescheduleTo);
        return new SnoozeResult(oldDisplay, event.display());
    }

    /**
     * Finds the task at a user-facing one-based position.
     *
     * @param taskNumber one-based number of the requested task
     * @return matching task
     * @throws KafkaException if no task has that number
     */
    private Task getTask(int taskNumber) throws KafkaException {
        return taskList.get(getTaskIndex(taskNumber));
    }

    /**
     * Validates a user-facing task number and converts it to a zero-based index.
     *
     * @param taskNumber one-based number supplied by the user
     * @return matching zero-based list index
     * @throws KafkaException if no task has that number
     */
    private int getTaskIndex(int taskNumber) throws KafkaException {
        if (taskNumber < 1 || taskNumber > taskList.size()) {
            throw new KafkaException(TASK_NOT_FOUND_ERROR);
        }
        return taskNumber - 1;
    }

}
