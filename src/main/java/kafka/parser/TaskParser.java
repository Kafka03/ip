package kafka.parser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import kafka.command.CommandType;
import kafka.exception.ParserException;
import kafka.task.Deadline;
import kafka.task.Event;
import kafka.task.Todo;

/**
 * Turns typed commands into proper task objects and clean task numbers, because
 * raw input can be a little chaotic, bestie.
 */
public final class TaskParser {
    private static final String BY_MARKER = "/by";
    private static final String FROM_MARKER = "/from";
    private static final String TO_MARKER = "/to";
    private static final String STORAGE_DELIMITER_ERROR =
            "Task details cannot contain | sorryyy";
    /** Accepted date patterns, ordered from machine-friendly to human-friendly. */
    private static final List<String> DATE_PATTERNS = List.of(
            "uuuu-MM-dd", "d/M/uuuu", "d MMM uuuu");
    /** Accepted time patterns covering 24-hour and AM/PM input. */
    private static final List<String> TIME_PATTERNS = List.of(
            "HHmm", "H:mm", "h:mma", "ha");
    private static final List<DateTimeFormatter> DATE_INPUT_FORMATTERS =
            createFormatters(DATE_PATTERNS);
    private static final List<DateTimeFormatter> TIME_INPUT_FORMATTERS =
            createFormatters(TIME_PATTERNS);
    private static final List<DateTimeFormatter> DATE_TIME_INPUT_FORMATTERS =
            createDateTimeFormatters();
    private static final DateTimeFormatter DATE_OUTPUT_FORMATTER =
            DateTimeFormatter.ofPattern("d MMM uuuu", Locale.ENGLISH);
    private static final DateTimeFormatter TIME_OUTPUT_FORMATTER =
            DateTimeFormatter.ofPattern("HHmm", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_TIME_OUTPUT_FORMATTER =
            DateTimeFormatter.ofPattern("d MMM uuuu HHmm", Locale.ENGLISH);

    /**
     * Prevents creation of this utility-only class.
     */
    private TaskParser() {
    }

    /**
     * Parses a todo command into an unfinished todo.
     *
     * @param input complete todo command entered by the user
     * @return parsed todo, ready to join the task list
     * @throws ParserException if the description is empty or unsafe to store
     */
    public static Todo parseTodo(String input) throws ParserException {
        String description = input.substring(CommandType.TODO.keyword().length()).trim();
        if (description.isEmpty()) {
            throw new ParserException("toodaloo. todo needs to hv a description alpha");
        }
        rejectStorageDelimiter(description);
        return new Todo(description);
    }

    /**
     * Parses a deadline command and normalizes any recognized date or time.
     *
     * @param input complete deadline command entered by the user
     * @return parsed deadline with display-ready timing text
     * @throws ParserException if its description, marker, or deadline is invalid
     */
    public static Deadline parseDeadline(String input) throws ParserException {
        String taskDetails = input.substring(CommandType.DEADLINE.keyword().length()).trim();
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
        rejectStorageDelimiter(description, by);
        return new Deadline(description, normalizeDateTime(by));
    }

    /**
     * Parses an event command into its description, start, and end values.
     *
     * @param input complete event command entered by the user
     * @return parsed event with normalized timing text where possible
     * @throws ParserException if required details or markers are invalid
     */
    public static Event parseEvent(String input) throws ParserException {
        String taskDetails = input.substring(CommandType.EVENT.keyword().length()).trim();
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
        rejectStorageDelimiter(description, from, to);
        return new Event(description, normalizeDateTime(from), normalizeDateTime(to));
    }

    /**
     * Rejects the pipe reserved as Kafka's storage separator—no data chaos today.
     *
     * @param values user-provided values that will be written to storage
     * @throws ParserException if any value contains the reserved pipe character
     */
    private static void rejectStorageDelimiter(String... values) throws ParserException {
        for (String value : values) {
            if (value.contains("|")) {
                throw new ParserException(STORAGE_DELIMITER_ERROR);
            }
        }
    }

    /**
     * Converts a recognized date, time, or date-time into the standard display
     * format. Free-form text is preserved so values such as "Sunday" remain valid.
     *
     * @param value raw date, time, date-time, or free-form timing text
     * @return normalized timing text, or the original value when it is free-form
     */
    private static String normalizeDateTime(String value) {
        String normalizedWhitespace = value.trim().replaceAll("\\s+", " ");

        for (DateTimeFormatter formatter : DATE_TIME_INPUT_FORMATTERS) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(normalizedWhitespace, formatter);
                return dateTime.format(DATE_TIME_OUTPUT_FORMATTER);
            } catch (DateTimeParseException ignored) {
                // Try the next supported date-time format.
            }
        }

        for (DateTimeFormatter formatter : DATE_INPUT_FORMATTERS) {
            try {
                LocalDate date = LocalDate.parse(normalizedWhitespace, formatter);
                return date.format(DATE_OUTPUT_FORMATTER);
            } catch (DateTimeParseException ignored) {
                // Try the next supported date format.
            }
        }

        for (DateTimeFormatter formatter : TIME_INPUT_FORMATTERS) {
            try {
                LocalTime time = LocalTime.parse(normalizedWhitespace, formatter);
                return time.format(TIME_OUTPUT_FORMATTER);
            } catch (DateTimeParseException ignored) {
                // Try the next supported time format.
            }
        }

        return value;
    }

    /**
     * Creates strict, case-insensitive formatters for the supplied patterns.
     *
     * @param patterns date or time patterns to compile
     * @return immutable list of ready-to-use formatters
     */
    private static List<DateTimeFormatter> createFormatters(List<String> patterns) {
        return patterns.stream()
                .map(TaskParser::createFormatter)
                .toList();
    }

    /**
     * Creates every supported pairing of a date pattern and a time pattern.
     *
     * @return immutable list of supported date-time formatters
     */
    private static List<DateTimeFormatter> createDateTimeFormatters() {
        List<DateTimeFormatter> formatters = new ArrayList<>();
        for (String datePattern : DATE_PATTERNS) {
            for (String timePattern : TIME_PATTERNS) {
                formatters.add(createFormatter(datePattern + " " + timePattern));
            }
        }
        return List.copyOf(formatters);
    }

    /**
     * Creates one strict, English, case-insensitive formatter.
     *
     * @param pattern pattern understood by {@link DateTimeFormatter}
     * @return formatter configured for reliable input validation
     */
    private static DateTimeFormatter createFormatter(String pattern) {
        return new DateTimeFormatterBuilder()
                .parseCaseInsensitive()
                .appendPattern(pattern)
                .toFormatter(Locale.ENGLISH)
                .withResolverStyle(ResolverStyle.STRICT);
    }

    /**
     * Parses a positive one-based task number from a task-selection command.
     *
     * @param input complete mark, unmark, or delete command
     * @param command command keyword to remove before reading the number
     * @return positive task number supplied by the user
     * @throws ParserException if the value is not a positive whole number
     */
    public static int parseTaskNumber(String input, String command) throws ParserException {
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
