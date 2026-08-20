// Converts typed task commands into Todo, Deadline, and Event objects
class TaskParser {
    private static final String TODO_COMMAND = "todo";
    private static final String DEADLINE_COMMAND = "deadline";
    private static final String EVENT_COMMAND = "event";
    private static final String BY_MARKER = "/by";
    private static final String FROM_MARKER = "/from";
    private static final String TO_MARKER = "/to";

    // Parses a todo command and returns its task object.
    static Todo parseTodo(String input) throws ParserException {
        String description = input.substring(TODO_COMMAND.length()).trim();
        if (description.isEmpty()) {
            throw new ParserException("toodaloo. todo needs to hv a description alpha");
        }
        return new Todo(description);
    }

    // Parses a deadline command and separates its description and deadline.
    static Deadline parseDeadline(String input) throws ParserException {
        String taskDetails = input.substring(DEADLINE_COMMAND.length()).trim();
        int byMarkerPosition = taskDetails.indexOf(BY_MARKER);
        String description = byMarkerPosition < 0
                ? taskDetails
                : taskDetails.substring(0, byMarkerPosition).trim();

        if (description.isEmpty()) {
            throw new ParserException("im deaddd. The deadline description cannot be empty.");
        }
        if (byMarkerPosition < 0) {
            throw new ParserException("A deadline must include /by. Do you hate me?");
        }

        String by = taskDetails.substring(byMarkerPosition + BY_MARKER.length()).trim();
        if (by.isEmpty()) {
            throw new ParserException("The deadline date or time cannot be empty alpha.");
        }
        return new Deadline(description, by);
    }

    // Parses an event command and separates its description, start, and end.
    static Event parseEvent(String input) throws ParserException {
        String taskDetails = input.substring(EVENT_COMMAND.length()).trim();
        int fromMarkerPosition = taskDetails.indexOf(FROM_MARKER);
        int toMarkerPosition = taskDetails.indexOf(TO_MARKER);
        int descriptionEnd = taskDetails.length();

        // end the code at whichever valid marker appears first
        if (fromMarkerPosition >= 0) {
            descriptionEnd = Math.min(descriptionEnd, fromMarkerPosition);
        }
        if (toMarkerPosition >= 0) {
            descriptionEnd = Math.min(descriptionEnd, toMarkerPosition);
        }

        String description = taskDetails.substring(0, descriptionEnd).trim();
        if (description.isEmpty()) {
            throw new ParserException("are u event-ing new ways to tease me? The event description cannot be empty.");
        }
        if (fromMarkerPosition < 0 || toMarkerPosition < 0
                || fromMarkerPosition >= toMarkerPosition) {
            throw new ParserException("An event must include /from followed by /to. Do you hate me?");
        }

        String from = taskDetails.substring(fromMarkerPosition + FROM_MARKER.length(),
                toMarkerPosition).trim();
        String to = taskDetails.substring(toMarkerPosition + TO_MARKER.length()).trim();
        if (from.isEmpty() || to.isEmpty()) {
            throw new ParserException("The event start or end cannot be empty my forbidden alpha~");
        }
        return new Event(description, from, to);
    }

    // Parses a positive task number from a mark, unmark, or delete command.
    static int parseTaskNumber(String input, String command) throws ParserException {
        String numberText = input.substring(command.length()).trim();
        try {
            int taskNumber = Integer.parseInt(numberText);
            if (taskNumber < 1) {
                throw new ParserException("The task number must be at least 1 meow.");
            }
            return taskNumber;
        } catch (NumberFormatException exception) {
            throw new ParserException("please gimme just a whole numberrr");
        }
    }
}
