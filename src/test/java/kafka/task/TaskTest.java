package kafka.task;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Checks that every concrete task looks right and inherits its completion glow-up.
 */
class TaskTest {
    @Test
    void todoStartsUnmarkedAndCanBeMarkedAndUnmarked() {
        Task task = new Todo("read book");

        assertEquals("[T][ ] read book", task.display());
        assertEquals("[T][X] read book", task.mark());
        assertEquals("[T][ ] read book", task.unmark());
    }

    @Test
    void deadlineDisplaysDeadlineText() {
        Task task = new Deadline("return book", "Sunday");

        assertEquals("[D][ ] return book (by: Sunday)", task.display());
    }

    @Test
    void eventDisplaysStartAndEndText() {
        Task task = new Event("project meeting", "Mon 2pm", "4pm");

        assertEquals("[E][ ] project meeting (from: Mon 2pm to: 4pm)", task.display());
    }
}
