package kafka.command;

/**
 * Lists every supported command and {@link #UNKNOWN} for unrecognized input.
 */
public enum CommandType {
    /** Adds a task with no date or time attached. */
    TODO("todo", true),
    /** Adds a task that needs to be done by a date or time. */
    DEADLINE("deadline", true),
    /** Adds a task happening between start and end values. */
    EVENT("event", true),
    /** Marks a numbered task as completed. */
    MARK("mark", true),
    /** Marks a numbered task as incomplete. */
    UNMARK("unmark", true),
    /** Removes a numbered task from the list. */
    DELETE("delete", true),
    /** Replaces the name of a numbered task. */
    RENAME("rename", true),
    /** Reschedules a numbered deadline or event. */
    SNOOZE("snooze", true),
    /** Finds tasks containing a supplied keyword. */
    FIND("find", true),
    /** Displays all tasks in list order. */
    LIST("list", false),
    /** Ends the current Kafka session. */
    BYE("bye", false),
    /** Represents input that matches none of Kafka's commands. */
    UNKNOWN("", false);

    private final String keyword;
    private final boolean acceptsArguments;

    CommandType(String keyword, boolean acceptsArguments) {
        this.keyword = keyword;
        this.acceptsArguments = acceptsArguments;
    }

    /**
     * Returns the keyword used to invoke this command.
     *
     * @return this command's lowercase keyword
     */
    public String keyword() {
        return keyword;
    }

    /**
     * Identifies the command at the start of the user's input.
     * Argument-free commands must appear alone, while the others may be followed
     * by a space and their details.
     *
     * @param input complete command entered by the user
     * @return the matching command, or {@link #UNKNOWN} when nothing matches
     */
    public static CommandType fromInput(String input) {
        for (CommandType command : values()) {
            if (command == UNKNOWN) {
                continue;
            }
            boolean matchesKeyword = input.equals(command.keyword);
            boolean matchesCommandWithArguments = command.acceptsArguments
                    && input.startsWith(command.keyword + " ");
            if (matchesKeyword || matchesCommandWithArguments) {
                return command;
            }
        }
        return UNKNOWN;
    }
}
