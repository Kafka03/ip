/**
 * Represents a task that must be completed by a specified date or time.
 * The deadline is stored as entered ie strings.
 */
class Deadline extends Task {
    private final String by;

    Deadline(String description, String by) {
        super(description);
        this.by = by;
    }

    @Override
    String display() {
        return "[D]" + super.display() + " (by: " + by + ")";
    }

    @Override
    String toDataString() {
        return super.toDataString("D") + " | " + by;
    }
}
