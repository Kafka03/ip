package kafka.command;

/**
 * Represents the commands understood by the Kafka chatbot.
 */
public enum CommandType {
    TODO("todo", true),
    DEADLINE("deadline", true),
    EVENT("event", true),
    MARK("mark", true),
    UNMARK("unmark", true),
    DELETE("delete", true),
    LIST("list", false),
    BYE("bye", false),
    UNKNOWN("", false);

    private final String keyword;
    private final boolean acceptsArguments;

    CommandType(String keyword, boolean acceptsArguments) {
        this.keyword = keyword;
        this.acceptsArguments = acceptsArguments;
    }

    //Returns the word users type to invoke this command.
    public String keyword() {
        return keyword;
    }

    /**
     * Identifies a command while preserving the accepted input format: a
     * keyword by itself or a keyword followed by a space and arguments.
     */
    public static CommandType fromInput(String input) {
        for (CommandType command : values()) {
            if (command == UNKNOWN) {
                continue;
            }
            if (input.equals(command.keyword)
                    || command.acceptsArguments
                    && input.startsWith(command.keyword + " ")) {
                return command;
            }
        }
        return UNKNOWN;
    }
}
