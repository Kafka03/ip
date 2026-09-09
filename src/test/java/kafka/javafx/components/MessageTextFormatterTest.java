package kafka.javafx.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import javafx.scene.text.Text;
import kafka.task.Deadline;
import kafka.task.Event;
import kafka.task.Task;
import kafka.task.TaskList;
import kafka.task.Todo;
import kafka.ui.Ui;

/**
 * Checks task styling against actual console messages and protects literal task descriptions.
 */
class MessageTextFormatterTest {
    @Test
    void formatResponse_addedTasks_preservesTextAndStylesEachType() {
        List<Task> tasks = List.of(new Todo("read book"), new Deadline("submit report", "Sunday"),
                new Event("meeting", "2pm", "4pm"));
        List<String> styles = List.of("task-todo", "task-deadline", "task-event");
        Ui ui = new Ui();
        for (int i = 0; i < tasks.size(); i++) {
            String message = ui.formatTaskAdded(tasks.get(i), i + 1);
            List<Text> segments = MessageTextFormatter.formatResponse(message);
            assertEquals(message, joinText(segments));
            String expectedStyle = styles.get(i);
            assertTrue(segments.stream().anyMatch(text -> text.getStyleClass().contains(expectedStyle)));
        }
    }

    @Test
    void formatResponse_mixedList_marksOnlyCompletedTask() {
        TaskList tasks = new TaskList();
        Task todo = new Todo("read book");
        todo.mark();
        tasks.addTask(todo);
        tasks.addTask(new Deadline("submit report", "Sunday"));
        tasks.addTask(new Event("meeting", "2pm", "4pm"));
        String message = new Ui().formatTaskList(tasks);

        List<Text> segments = MessageTextFormatter.formatResponse(message);

        assertEquals(message.replace("[X]", "[\u2713]"), joinText(segments));
        List<Text> completedMarkers = segments.stream()
                .filter(text -> text.getStyleClass().contains("task-done")).toList();
        assertEquals(1, completedMarkers.size());
        assertEquals("[\u2713]", completedMarkers.getFirst().getText());
        for (String style : List.of("task-todo", "task-deadline", "task-event")) {
            assertTrue(segments.stream().anyMatch(text -> text.getStyleClass().contains(style)));
        }
    }

    @Test
    void formatResponse_unmarkedTask_hasNoCompletedMarker() {
        Task task = new Todo("read book");
        task.mark();
        task.unmark();
        String message = new Ui().formatTaskUnmarked(task.display());

        List<Text> segments = MessageTextFormatter.formatResponse(message);

        assertEquals(message, joinText(segments));
        assertFalse(segments.stream().anyMatch(text -> text.getStyleClass().contains("task-done")));
    }

    @Test
    void formatResponse_literalMarkersAndLineBreaks_preservesContents() {
        String message = "Example [D][X] stays literal\r\n"
                + "1.[T][ ] read about [E][X] and underscores ___\r\n";

        List<Text> segments = MessageTextFormatter.formatResponse(message);

        assertEquals(message, joinText(segments));
        assertFalse(segments.stream().anyMatch(text -> text.getStyleClass().contains("task-done")));
        assertFalse(segments.stream().anyMatch(text -> text.getStyleClass().contains("task-event")));
    }

    private String joinText(List<Text> segments) {
        return segments.stream().map(Text::getText).collect(Collectors.joining());
    }
}
