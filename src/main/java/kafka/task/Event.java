package kafka.task;

/**
 * Represents an event with specified start and end values.
 * Both values are stored as display-ready text.
 */
public class Event extends Task {
    private String from;
    private String to;

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

    void reschedule(String newFrom, String newTo) {
        from = newFrom;
        to = newTo;
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

    void rescheduleFrom(String newFrom) {
        from = newFrom;
    }

    void rescheduleTo(String newTo) {
        to = newTo;
    }
}
