package kafka.javafx.components;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.scene.text.Text;

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
     * Returns text segments with task type colors and a red check mark for completed tasks.
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
            segments.add(createText(" " + matcher.group(4), taskStyle));
            previousEnd = matcher.end();
        }
        segments.add(new Text(message.substring(previousEnd)));
        return segments;
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
