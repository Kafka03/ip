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
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import kafka.command.CommandType;
import kafka.exception.ParserException;
import kafka.task.Deadline;
import kafka.task.Event;
import kafka.task.Todo;

/**
 * Parses user commands into task objects, task numbers, and search keywords.
 */
public final class TaskParser {
    private static final String BY_MARKER = "/by";
    private static final String FROM_MARKER = "/from";
    private static final String TO_MARKER = "/to";
    private static final String TODO_DESCRIPTION_ERROR =
            "toodaloo. todo needs to hv a description alpha";
    private static final String EMPTY_DEADLINE_DESCRIPTION_ERROR =
            "im deaddd. The deadline description cannot be empty.";
    private static final String MISSING_BY_MARKER_ERROR =
            "A deadline must include /by. Do you hate me?";
    private static final String EMPTY_DEADLINE_TIME_ERROR =
            "The deadline date or time cannot be empty alpha.";
    private static final String STORAGE_DELIMITER_ERROR =
            "Task details cannot contain | sorryyy";
    private static final String MULTILINE_DETAILS_ERROR =
            "Task details must stay on one line.";
    private static final String EMPTY_EVENT_DESCRIPTION_ERROR =
            "are u event-ing new ways to tease me? "
            + "The event description cannot be empty.";
    private static final String INVALID_EVENT_MARKERS_ERROR =
            "An event must include /from followed by /to. Do you hate me?";
    private static final String EMPTY_EVENT_TIME_ERROR =
            "The event start or end cannot be empty my forbidden alpha~";
    private static final String RENAME_ARGUMENTS_ERROR =
            "Please provide (for my livelihood) a task number and a new name.";
    private static final String MINIMUM_TASK_NUMBER_ERROR =
            "The task number must be at least 1 meow.";
    private static final String WHOLE_NUMBER_ERROR =
            "please gimme just a whole numberrr";
    private static final String EMPTY_FIND_KEYWORD_ERROR =
            "Please provide a keyword to find that taskkk.";
    private static final String SNOOZE_ARGUMENTS_ERROR =
            "Use snooze TASK_NUMBER followed by /by, /from, or /to. ZZZZzzzzZZZZ";
    private static final String INVALID_SNOOZE_MARKERS_ERROR =
            "Use /by for a deadline, or /from and/or /to for an event. Sorry for being formal meow it's important!";
    private static final String EMPTY_SNOOZE_TIME_ERROR =
            "A snooze date or time cannot be empty sowwy";
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
    /** Finds date and time tokens without interpreting ordinary words such as "Sunday". */
    private static final Pattern DATE_TIME_TOKEN = Pattern.compile(
            "(?<![\\p{L}\\p{N}/:-])(?:\\d{4}-\\d{2}-\\d{2}|\\d{1,2}/\\d{1,2}/\\d{4}"
                    + "|\\d{1,2}\\s+(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)\\s+\\d{4}"
                    + "|\\d{1,2}:\\d{2}(?:am|pm)?|\\d{1,2}(?:am|pm)|\\d{4})(?![\\p{L}\\p{N}/:-])",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CHARACTER_CLASS);
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
     * Parses a todo command into an unfinished todo, preserving all description whitespace.
     *
     * @param input complete todo command entered by the user
     * @return parsed todo, ready to join the task list
     * @throws ParserException if the description is empty or unsafe to store
     */
    public static Todo parseTodo(String input) throws ParserException {
        String description = input.stripLeading().substring(CommandType.TODO.keyword().length());
        if (description.isBlank()) {
            throw new ParserException(TODO_DESCRIPTION_ERROR);
        }
        rejectStorageDelimiter(description);
        return new Todo(description);
    }

    /**
     * Preserves deadline description whitespace and normalizes any recognized date or time.
     *
     * @param input complete deadline command entered by the user
     * @return parsed deadline with display-ready timing text
     * @throws ParserException if its description, marker, or deadline is invalid
     */
    public static Deadline parseDeadline(String input) throws ParserException {
        String taskDetails = input.stripLeading().substring(CommandType.DEADLINE.keyword().length());
        int byMarkerPosition = findUniqueMarker(taskDetails, BY_MARKER);
        String description = byMarkerPosition < 0
                ? taskDetails
                : taskDetails.substring(0, byMarkerPosition);

        if (description.isBlank()) {
            throw new ParserException(EMPTY_DEADLINE_DESCRIPTION_ERROR);
        }
        if (byMarkerPosition < 0) {
            throw new ParserException(MISSING_BY_MARKER_ERROR);
        }

        String by = taskDetails.substring(byMarkerPosition + BY_MARKER.length()).strip();
        if (by.isEmpty()) {
            throw new ParserException(EMPTY_DEADLINE_TIME_ERROR);
        }
        rejectStorageDelimiter(description, by);
        return new Deadline(description, normalizeDateTime(by));
    }

    /**
     * Preserves event description whitespace and parses its start and end values.
     *
     * @param input complete event command entered by the user
     * @return parsed event with normalized timing text where possible
     * @throws ParserException if required details or markers are invalid
     */
    public static Event parseEvent(String input) throws ParserException {
        String taskDetails = input.stripLeading().substring(CommandType.EVENT.keyword().length());
        int fromMarkerPosition = findUniqueMarker(taskDetails, FROM_MARKER);
        int toMarkerPosition = findUniqueMarker(taskDetails, TO_MARKER);
        int descriptionEnd = taskDetails.length();

        // End the description at whichever valid marker appears first.
        if (fromMarkerPosition >= 0) {
            descriptionEnd = Math.min(descriptionEnd, fromMarkerPosition);
        }
        if (toMarkerPosition >= 0) {
            descriptionEnd = Math.min(descriptionEnd, toMarkerPosition);
        }

        String description = taskDetails.substring(0, descriptionEnd);
        if (description.isBlank()) {
            throw new ParserException(EMPTY_EVENT_DESCRIPTION_ERROR);
        }
        if (fromMarkerPosition < 0 || toMarkerPosition < 0
                || fromMarkerPosition >= toMarkerPosition) {
            throw new ParserException(INVALID_EVENT_MARKERS_ERROR);
        }
        String from = taskDetails.substring(fromMarkerPosition + FROM_MARKER.length(),
                toMarkerPosition).strip();
        String to = taskDetails.substring(toMarkerPosition + TO_MARKER.length()).strip();
        if (from.isEmpty() || to.isEmpty()) {
            throw new ParserException(EMPTY_EVENT_TIME_ERROR);
        }
        rejectStorageDelimiter(description, from, to);
        return new Event(description, normalizeDateTime(from), normalizeDateTime(to));
    }

    /**
     * Rejects characters that would split a saved field or task record.
     *
     * @param values user-provided values that will be written to storage
     * @throws ParserException if a value contains a pipe or line break
     */
    private static void rejectStorageDelimiter(String... values) throws ParserException {
        for (String value : values) {
            if (value.contains("|")) {
                throw new ParserException(STORAGE_DELIMITER_ERROR);
            }
            if (value.contains("\n") || value.contains("\r")) {
                throw new ParserException(MULTILINE_DETAILS_ERROR);
            }
        }
    }

    /**
     * Finds a whitespace-delimited marker and rejects a second occurrence.
     * Substrings such as {@code /bytes} and {@code docs/from} remain ordinary text.
     *
     * @param text command details to inspect
     * @param marker exact marker token to locate
     * @return marker position, or {@code -1} when absent
     * @throws ParserException if the marker occurs more than once
     */
    private static int findUniqueMarker(String text, String marker) throws ParserException {
        Pattern pattern = Pattern.compile("(?<!\\S)" + Pattern.quote(marker) + "(?!\\S)",
                Pattern.UNICODE_CHARACTER_CLASS);
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return -1;
        }
        int position = matcher.start();
        if (matcher.find()) {
            throw new ParserException("Use " + marker + " only once per command.");
        }
        return position;
    }

    /**
     * Converts a recognized date, time, or date-time into the standard display
     * format. Free-form text is preserved so values such as "Sunday" remain valid.
     *
     * @param value raw date, time, date-time, or free-form timing text
     * @return normalized timing text, or the original value when it is free-form
     */
    private static String normalizeDateTime(String value) {
        String normalizedWhitespace = value.strip().replaceAll("\\p{javaWhitespace}+", " ");

        Optional<String> normalizedDateTime = findNormalizedDateTime(normalizedWhitespace);
        if (normalizedDateTime.isPresent()) {
            return normalizedDateTime.get();
        }

        Optional<String> normalizedDate = findNormalizedDate(normalizedWhitespace);
        if (normalizedDate.isPresent()) {
            return normalizedDate.get();
        }

        Optional<String> normalizedTime = findNormalizedTime(normalizedWhitespace);
        if (normalizedTime.isPresent()) {
            return normalizedTime.get();
        }

        return value;
    }

    /**
     * Finds impossible dates and times written in the supported numeric or English formats.
     * Free-form words are left alone, and matches retain their original positions for GUI styling.
     *
     * @param timingText displayed deadline or event timing text.
     * @return immutable matches identifying invalid date or time tokens.
     */
    public static List<MatchResult> findInvalidDateTimes(String timingText) {
        return DATE_TIME_TOKEN.matcher(timingText).results()
                .filter(match -> {
                    String value = match.group().replaceAll("\\p{javaWhitespace}+", " ");
                    return findNormalizedDate(value).isEmpty() && findNormalizedTime(value).isEmpty();
                })
                .toList();
    }

    /**
     * Returns the normalized date-time accepted by the first matching format.
     *
     * @param value Date-time text to parse.
     * @return Normalized date-time, or an empty result if no format matches.
     */
    private static Optional<String> findNormalizedDateTime(String value) {
        for (DateTimeFormatter formatter : DATE_TIME_INPUT_FORMATTERS) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(value, formatter);
                return Optional.of(dateTime.format(DATE_TIME_OUTPUT_FORMATTER));
            } catch (DateTimeParseException ignored) {
                continue;
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the normalized date accepted by the first matching format.
     *
     * @param value Date text to parse.
     * @return Normalized date, or an empty result if no format matches.
     */
    private static Optional<String> findNormalizedDate(String value) {
        for (DateTimeFormatter formatter : DATE_INPUT_FORMATTERS) {
            try {
                LocalDate date = LocalDate.parse(value, formatter);
                return Optional.of(date.format(DATE_OUTPUT_FORMATTER));
            } catch (DateTimeParseException ignored) {
                continue;
            }
        }
        return Optional.empty();
    }

    /**
     * Returns the normalized time accepted by the first matching format.
     *
     * @param value Time text to parse.
     * @return Normalized time, or an empty result if no format matches.
     */
    private static Optional<String> findNormalizedTime(String value) {
        for (DateTimeFormatter formatter : TIME_INPUT_FORMATTERS) {
            try {
                LocalTime time = LocalTime.parse(value, formatter);
                return Optional.of(time.format(TIME_OUTPUT_FORMATTER));
            } catch (DateTimeParseException ignored) {
                continue;
            }
        }
        return Optional.empty();
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
        String numberText = input.substring(command.length()).strip();
        return parsePositiveTaskNumber(numberText);
    }

    /**
     * Parses a rename command, preserving all whitespace after the task number.
     *
     * @param input complete rename command entered by the user
     * @return task number and replacement name
     * @throws ParserException if the number or replacement name is invalid
     */
    public static RenameRequest parseRename(String input) throws ParserException {
        String arguments = input.stripLeading().substring(CommandType.RENAME.keyword().length()).stripLeading();
        int nameStart = 0;
        while (nameStart < arguments.length() && !Character.isWhitespace(arguments.charAt(nameStart))) {
            nameStart++;
        }
        String newName = arguments.substring(nameStart);
        if (newName.isBlank()) {
            throw new ParserException(RENAME_ARGUMENTS_ERROR);
        }

        int taskNumber = parsePositiveTaskNumber(arguments.substring(0, nameStart));
        rejectStorageDelimiter(newName);
        return new RenameRequest(taskNumber, newName);
    }

    /**
     * Parses a positive task number from its text representation.
     *
     * @param numberText task number without its command keyword
     * @return positive task number
     * @throws ParserException if the text is not a positive whole number
     */
    private static int parsePositiveTaskNumber(String numberText) throws ParserException {
        try {
            int taskNumber = Integer.parseInt(numberText);
            if (taskNumber < 1) {
                throw new ParserException(MINIMUM_TASK_NUMBER_ERROR);
            }
            return taskNumber;
        } catch (NumberFormatException exception) {
            throw new ParserException(WHOLE_NUMBER_ERROR);
        }
    }

    /**
     * Parses a deadline or event snooze command.
     *
     * @param input complete snooze command entered by the user
     * @return parsed deadline or event schedule change
     * @throws ParserException if the task number, markers, or values are invalid
     */
    public static SnoozeRequest parseSnooze(String input) throws ParserException {
        String arguments = input.substring(CommandType.SNOOZE.keyword().length()).strip();
        String[] parts = arguments.split("\\p{javaWhitespace}+", 2);
        if (parts.length < 2) {
            throw new ParserException(SNOOZE_ARGUMENTS_ERROR);
        }

        int taskNumber = parsePositiveTaskNumber(parts[0]);
        String schedule = parts[1].strip();
        if (findUniqueMarker(schedule, BY_MARKER) == 0) {
            return parseDeadlineSnooze(taskNumber, schedule);
        }
        if (findUniqueMarker(schedule, FROM_MARKER) == 0 || findUniqueMarker(schedule, TO_MARKER) == 0) {
            return parseEventSnooze(taskNumber, schedule);
        }
        throw new ParserException(SNOOZE_ARGUMENTS_ERROR);
    }

    /**
     * Parses the replacement value from a deadline snooze.
     *
     * @param taskNumber one-based task number
     * @param schedule schedule portion beginning with {@code /by}
     * @return parsed deadline schedule change
     * @throws ParserException if the marker combination or value is invalid
     */
    private static SnoozeDeadlineResult parseDeadlineSnooze(
            int taskNumber, String schedule) throws ParserException {
        if (findUniqueMarker(schedule, FROM_MARKER) >= 0 || findUniqueMarker(schedule, TO_MARKER) >= 0) {
            throw new ParserException(INVALID_SNOOZE_MARKERS_ERROR);
        }

        String newBy = schedule.substring(BY_MARKER.length()).strip();
        validateSnoozeValue(newBy);
        return new SnoozeDeadlineResult(taskNumber, normalizeDateTime(newBy));
    }

    /**
     * Parses one or both replacement values from an event snooze.
     *
     * @param taskNumber one-based task number
     * @param schedule schedule portion beginning with {@code /from} or {@code /to}
     * @return parsed event schedule change
     * @throws ParserException if the marker combination or values are invalid
     */
    private static SnoozeEventResult parseEventSnooze(
            int taskNumber, String schedule) throws ParserException {
        if (findUniqueMarker(schedule, BY_MARKER) >= 0) {
            throw new ParserException(INVALID_SNOOZE_MARKERS_ERROR);
        }

        int fromPosition = findUniqueMarker(schedule, FROM_MARKER);
        int toPosition = findUniqueMarker(schedule, TO_MARKER);
        if (fromPosition > toPosition && toPosition >= 0) {
            throw new ParserException(INVALID_SNOOZE_MARKERS_ERROR);
        }

        Optional<String> newFrom = parseNewFrom(schedule, fromPosition, toPosition);
        Optional<String> newTo = parseNewTo(schedule, toPosition);
        assert newFrom.isPresent() || newTo.isPresent()
        : "A parsed event snooze must include at least one timestamp";
        return new SnoozeEventResult(taskNumber, newFrom, newTo);
    }

    /**
     * Extracts and normalizes a replacement event start when supplied.
     *
     * @param schedule event schedule arguments
     * @param fromPosition position of {@code /from}, or {@code -1}
     * @param toPosition position of {@code /to}, or {@code -1}
     * @return replacement start, if supplied
     * @throws ParserException if the replacement is empty or unsafe to store
     */
    private static Optional<String> parseNewFrom(
            String schedule, int fromPosition, int toPosition) throws ParserException {
        if (fromPosition < 0) {
            return Optional.empty();
        }

        int valueEnd = toPosition < 0 ? schedule.length() : toPosition;
        String newFrom = schedule.substring(fromPosition + FROM_MARKER.length(), valueEnd).strip();
        validateSnoozeValue(newFrom);
        return Optional.of(normalizeDateTime(newFrom));
    }

    /**
     * Extracts and normalizes a replacement event end when supplied.
     *
     * @param schedule event schedule arguments
     * @param toPosition position of {@code /to}, or {@code -1}
     * @return replacement end, if supplied
     * @throws ParserException if the replacement is empty or unsafe to store
     */
    private static Optional<String> parseNewTo(String schedule, int toPosition)
            throws ParserException {
        if (toPosition < 0) {
            return Optional.empty();
        }

        String newTo = schedule.substring(toPosition + TO_MARKER.length()).strip();
        validateSnoozeValue(newTo);
        return Optional.of(normalizeDateTime(newTo));
    }

    /**
     * Checks that a replacement schedule value is present and safe to store.
     *
     * @param value replacement date or time
     * @throws ParserException if the replacement is empty or unsafe to store
     */
    private static void validateSnoozeValue(String value) throws ParserException {
        if (value.isBlank()) {
            throw new ParserException(EMPTY_SNOOZE_TIME_ERROR);
        }
        rejectStorageDelimiter(value);
    }

    /**
     * Extracts the non-empty keyword from a find command.
     *
     * @param input complete find command entered by the user
     * @return keyword to look for in the task list
     * @throws ParserException if no keyword was supplied
     */
    public static String parseFindKeyword(String input) throws ParserException {
        String keyword = input.substring(CommandType.FIND.keyword().length()).strip();
        if (keyword.isEmpty()) {
            throw new ParserException(EMPTY_FIND_KEYWORD_ERROR);
        }
        return keyword;
    }
}
