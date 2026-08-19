/**
 * Converts typed task commands into Todo, Deadline, and Event objects while
 * leaving their date and time values as strings.
 */
class TaskParser {
    private static final String TODO_PREFIX = "todo ";
    private static final String DEADLINE_PREFIX = "deadline ";
    private static final String EVENT_PREFIX = "event ";
    private static final String BY_MARKER = " /by ";
    private static final String FROM_MARKER = " /from ";
    private static final String TO_MARKER = " /to ";

    // Parses a todo command and returns its task object.
    static Todo parseTodo(String input) {
        String description = input.substring(TODO_PREFIX.length());
        return new Todo(description);
    }

    // Parses a deadline command and separates its description and deadline.
    static Deadline parseDeadline(String input) {
        String taskDetails = input.substring(DEADLINE_PREFIX.length());
        int byMarkerPosition = taskDetails.indexOf(BY_MARKER);
        String description = taskDetails.substring(0, byMarkerPosition);
        String by = taskDetails.substring(byMarkerPosition + BY_MARKER.length());
        return new Deadline(description, by);
    }

    // Parses an event command and separates its description, start, and end.
    static Event parseEvent(String input) {
        String taskDetails = input.substring(EVENT_PREFIX.length());
        int fromMarkerPosition = taskDetails.indexOf(FROM_MARKER);
        int toMarkerPosition = taskDetails.indexOf(TO_MARKER,
                fromMarkerPosition + FROM_MARKER.length());
        String description = taskDetails.substring(0, fromMarkerPosition);
        String from = taskDetails.substring(fromMarkerPosition + FROM_MARKER.length(),
                toMarkerPosition);
        String to = taskDetails.substring(toMarkerPosition + TO_MARKER.length());
        return new Event(description, from, to);
    }
}
