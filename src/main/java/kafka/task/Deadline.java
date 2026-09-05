package kafka.task;

/**
 * Represents a task that must be completed by a specified date or time.
 * The deadline is stored as display-ready text.
 */
public class Deadline extends Task {
    private String by;

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

    void reschedule(String newBy) {
        by = newBy;
    }
}
