package kafka.javafx.components;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.text.Text;
import kafka.parser.TaskParser;

/**
 * Styles task lines in GUI replies without changing console output or stored task data.
 */
final class MessageTextFormatter {
    /** Matches task lines in lists and confirmations, leaving the task description intact. */
    private static final Pattern TASK_LINE = Pattern.compile(
            "(?m)^([\\t ]*(?:\\d+\\.)?)\\[([TDE])\\]\\[([ X])\\] ([^\\r\\n]*)");

    private MessageTextFormatter() {
    }

    /**
     * Returns task colors, red check marks for completed tasks, and red invalid dates or times.
     * Only formatted task lines are interpreted; other reply text is preserved.
     */
    static List<Text> formatResponse(String message) {
        List<Text> segments = new ArrayList<>();
        Matcher matcher = TASK_LINE.matcher(message);
        int previousEnd = 0;
        while (matcher.find()) {
            segments.add(new Text(message.substring(previousEnd, matcher.start())));
            segments.add(createText(matcher.group(1), "task-secondary"));
            String taskStyle = switch (matcher.group(2)) {
                case "T" -> "task-todo";
                case "D" -> "task-deadline";
                case "E" -> "task-event";
                default -> throw new IllegalStateException("Unrecognized task type");
            };
            segments.add(createText("[" + matcher.group(2) + "]", taskStyle));
            boolean isDone = matcher.group(3).equals("X");
            segments.add(createText(isDone ? "[\u2713]" : "[ ]", isDone ? "task-done" : "task-secondary"));
            appendTaskDetails(segments, " " + matcher.group(4), matcher.group(2), taskStyle);
            previousEnd = matcher.end();
        }
        segments.add(new Text(message.substring(previousEnd)));
        return segments;
    }

    /**
     * Highlights invalid timing tokens only in the task's schedule, preserving its description.
     * Recomputing the style from the timing text keeps it consistent after edits and reloads.
     */
    private static void appendTaskDetails(List<Text> segments, String details, String taskType, String taskStyle) {
        String scheduleMarker = switch (taskType) {
            case "D" -> " (by: ";
            case "E" -> " (from: ";
            default -> "";
        };
        int markerPosition = scheduleMarker.isEmpty() ? -1 : details.lastIndexOf(scheduleMarker);
        if (markerPosition < 0) {
            segments.add(createText(details, taskStyle));
            return;
        }

        int scheduleStart = markerPosition + scheduleMarker.length();
        String timingText = details.substring(scheduleStart);
        int previousEnd = 0;
        for (MatchResult invalidValue : TaskParser.findInvalidDateTimes(timingText)) {
            int start = scheduleStart + invalidValue.start();
            int end = scheduleStart + invalidValue.end();
            segments.add(createText(details.substring(previousEnd, start), taskStyle));
            segments.add(createText(details.substring(start, end), "task-invalid-date"));
            previousEnd = end;
        }
        segments.add(createText(details.substring(previousEnd), taskStyle));
    }

    /**
     * Creates a text segment whose color is defined by the dialog stylesheet.
     */
    private static Text createText(String content, String styleClass) {
        Text text = new Text(content);
        text.getStyleClass().add(styleClass);
        return text;
    }
}
