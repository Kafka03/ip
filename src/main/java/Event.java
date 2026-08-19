/**
 * Represents a task that takes place between specified start and end times.
 * Both values are stored as entered ie strings rather than parsed as dates or times.
 */
class Event extends Task {
    private final String from;
    private final String to;

    Event(String description, String from, String to) {
        super(description);
        this.from = from;
        this.to = to;
    }

    @Override
    String display() {
        return "[E]" + super.display() + " (from: " + from + " to: " + to + ")";
    }
}
