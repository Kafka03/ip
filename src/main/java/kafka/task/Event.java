package kafka.task;

/**
 * Represents something happening between a start and an end—very booked and
 * busy. Both values are stored as display-ready text.
 */
public class Event extends Task {
    private final String from;
    private final String to;

    /**
     * Creates an unfinished event.
     *
     * @param description activity taking place
     * @param from display-ready start date or time
     * @param to display-ready end date or time
     */
    public Event(String description, String from, String to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    /**
     * Adds the event marker and time range to the common task display.
     *
     * @return display text beginning with {@code [E]}
     */
    @Override
    public String display() {
        return "[E]" + super.display() + " (from: " + from + " to: " + to + ")";
    }

    /**
     * Serializes this event for the task data file.
     *
     * @return event data including its start and end values
     */
    @Override
    public String toDataString() {
        return super.toDataString("E") + " | " + from + " | " + to;
    }
}
