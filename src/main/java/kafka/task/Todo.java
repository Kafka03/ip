package kafka.task;

/**
 * Represents a task without an associated date or time.
 */
public class Todo extends Task {
    /**
     * Creates an unfinished todo.
     *
     * @param description work the user wants Kafka to remember
     */
    public Todo(String description) {
        super(description);
    }

    /**
     * Adds the todo marker to the common task display.
     *
     * @return display text beginning with {@code [T]}
     */
    @Override
    public String display() {
        return "[T]" + super.display();
    }

    /**
     * Serializes this todo for the task data file.
     *
     * @return todo data in Kafka's storage format
     */
    @Override
    public String toDataString() {
        return super.toDataString("T");
    }
}
