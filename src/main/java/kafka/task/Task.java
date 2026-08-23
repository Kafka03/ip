package kafka.task;

/**
 * Represents one item in the user's hustle list, with a name and completion state.
 */
public abstract class Task {
    private final String name;
    private boolean isDone;

    /**
     * Creates an unfinished task with the supplied name.
     *
     * @param name description of the work to remember
     */
    protected Task(String name) {
        this.name = name;
        this.isDone = false;
    }

    /**
     * Marks this task as completed.
     *
     * @return display text showing the freshly completed task
     */
    public String mark() {
        isDone = true;
        return display();
    }

    /**
     * Marks this task as incomplete again—no judgment here.
     *
     * @return display text showing the incomplete task
     */
    public String unmark() {
        isDone = false;
        return display();
    }

    /**
     * Builds the common checkbox and name shown for every task type.
     *
     * @return checkbox followed by this task's name
     */
    public String display() {
        String status = isDone ? "[X]" : "[ ]";
        return status + " " + name;
    }

    /**
     * Builds the storage fields shared by every concrete task type.
     *
     * @param taskType one-letter code identifying the concrete task type
     * @return type, completion state, and name in the storage format
     */
    protected String toDataString(String taskType) {
        String status = isDone ? "1" : "0";
        return taskType + " | " + status + " | " + name;
    }

    /**
     * Converts this task into one save-file line so Kafka remembers it next time.
     *
     * @return complete serialized form of this task
     */
    public abstract String toDataString();
}
