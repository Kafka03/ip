package kafka.task;

/**
 * Represents a task that must be completed by a specified date or time.
 * The deadline is stored as display-ready text.
 */
public class Deadline extends Task {
    private final String by;

    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    @Override
    public String display() {
        return "[D]" + super.display() + " (by: " + by + ")";
    }

    @Override
    public String toDataString() {
        return super.toDataString("D") + " | " + by;
    }
}
