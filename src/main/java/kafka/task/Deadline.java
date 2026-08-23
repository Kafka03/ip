package kafka.task;

/**
 * Represents a task that needs to be done by a particular date or time—clock's
 * ticking, alpha. The deadline is stored as display-ready text.
 */
public class Deadline extends Task {
    private final String by;

    /**
     * Creates an unfinished deadline.
     *
     * @param description work that needs doing
     * @param by display-ready date or time by which it should be done
     */
    public Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    /**
     * Adds the deadline marker and due value to the common task display.
     *
     * @return display text beginning with {@code [D]}
     */
    @Override
    public String display() {
        return "[D]" + super.display() + " (by: " + by + ")";
    }

    /**
     * Serializes this deadline for the task data file.
     *
     * @return deadline data including its due value
     */
    @Override
    public String toDataString() {
        return super.toDataString("D") + " | " + by;
    }
}
