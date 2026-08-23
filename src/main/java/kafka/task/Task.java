package kafka.task;

/**
 * Represents a task with a description and completion state.
 */
public abstract class Task {
    private final String name;
    private boolean isDone;

    protected Task(String name) {
        this.name = name;
        this.isDone = false;
    }

    // Marks this task as completed and returns its updated display text
    public String mark() {
        isDone = true;
        return display();
    }

    // Marks this task as incomplete and returns its updated display text
    public String unmark() {
        isDone = false;
        return display();
    }

    // Returns the checkbox and name used when displaying this task
    public String display() {
        String status = isDone ? "[X]" : "[ ]";
        return status + " " + name;
    }

    // Returns the fields shared by every task in the storage format
    protected String toDataString(String taskType) {
        String status = isDone ? "1" : "0";
        return taskType + " | " + status + " | " + name;
    }

    // Converts this task into one line suitable for the task data file
    public abstract String toDataString();
}
