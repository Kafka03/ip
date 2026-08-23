package kafka.task;

/**
 * Represents a task that has no associated date or time.
 */
public class Todo extends Task {
    public Todo(String description) {
        super(description);
    }

    @Override
    public String display() {
        return "[T]" + super.display();
    }

    @Override
    public String toDataString() {
        return super.toDataString("T");
    }
}
