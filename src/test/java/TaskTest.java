import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

// Tests a task's name, checkbox, and completion state.
class TaskTest {
    @Test
    void newTaskIsUnmarked() {
        // A newly created task should start incomplete.
        Task task = new Task("read book");

        assertEquals("[ ] read book", task.display());
    }

    @Test
    void markReturnsMarkedTask() {
        Task task = new Task("read book");

        // Marking changes the checkbox and returns the updated task text.
        assertEquals("[X] read book", task.mark());
        assertEquals("[X] read book", task.display());
    }

    @Test
    void unmarkReturnsUnmarkedTask() {
        Task task = new Task("read book");
        task.mark();

        // Unmarking restores the incomplete checkbox.
        assertEquals("[ ] read book", task.unmark());
        assertEquals("[ ] read book", task.display());
    }
}
