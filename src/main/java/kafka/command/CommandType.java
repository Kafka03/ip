package kafka.command;

/**
 * Lists every command Kafka understands, plus {@link #UNKNOWN} for mystery input.
 */
public enum CommandType {
    /** Adds a task with no date or time attached. */
    TODO("todo", true),
    /** Adds a task that needs to be done by a date or time. */
    DEADLINE("deadline", true),
    /** Adds a task happening between start and end values. */
    EVENT("event", true),
    /** Marks a numbered task as completed—slay. */
    MARK("mark", true),
    /** Returns a numbered task to its unfinished era. */
    UNMARK("unmark", true),
    /** Removes a numbered task from the list. */
    DELETE("delete", true),
    /** Finds tasks containing a supplied keyword. */
    FIND("find", true),
    /** Displays the full task squad in order. */
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
     * Returns the keyword users type to summon this command.
     *
     * @return this command's lowercase keyword
     */
    public String keyword() {
        return keyword;
    }

    /**
     * Identifies the command at the front of a user's input, meow.
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
