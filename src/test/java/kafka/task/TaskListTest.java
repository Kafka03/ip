package kafka.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import kafka.exception.KafkaException;

/**
 * Tests task ordering, status changes, and deletion in a task list.
 */
class TaskListTest {
    @Test
    void isEmptyReflectsWhetherTasksAreStored() {
        TaskList tasks = new TaskList();

        assertTrue(tasks.isEmpty());

        tasks.addTask(new Todo("read book"));

        assertFalse(tasks.isEmpty());
    }

    @Test
    void markAndUnmarkChangeSelectedTask() throws KafkaException {
        TaskList tasks = new TaskList();
        tasks.addTask(new Todo("read book"));
        tasks.addTask(new Todo("return book"));

        Task markedTask = tasks.markTask(2);
        assertEquals("[T][X] return book", markedTask.display());

        Task unmarkedTask = tasks.unmarkTask(2);
        assertEquals("[T][ ] return book", unmarkedTask.display());
    }

    @Test
    void unavailableTaskNumberDoesNotChangeExistingTask() {
        TaskList tasks = new TaskList();
        Task task = new Todo("read book");
        tasks.addTask(task);

        assertThrows(KafkaException.class, () -> tasks.markTask(2));
        assertEquals("[T][ ] read book", task.display());
    }

    @Test
    void deleteTaskReturnsRemovedTaskAndShiftsRemainingTasks() throws KafkaException {
        TaskList tasks = new TaskList();
        tasks.addTask(new Todo("first"));
        tasks.addTask(new Deadline("second", "Sunday"));
        tasks.addTask(new Event("third", "Monday", "Tuesday"));

        Task deletedTask = tasks.deleteTask(2);

        assertEquals("[D][ ] second (by: Sunday)", deletedTask.display());
        assertEquals(2, tasks.size());
        Task markedTask = tasks.markTask(2);
        assertEquals("[E][X] third (from: Monday to: Tuesday)", markedTask.display());
    }

    @Test
    void findTasksReturnsCaseInsensitiveMatchesInOriginalOrder() {
        TaskList tasks = new TaskList();
        tasks.addTask(new Todo("read book"));
        tasks.addTask(new Deadline("return BOOK", "June 6th"));
        tasks.addTask(new Todo("write essay"));

        java.util.List<Task> matches = tasks.findTasks("book");

        assertEquals(2, matches.size());
        assertEquals("[T][ ] read book", matches.get(0).display());
        assertEquals("[D][ ] return BOOK (by: June 6th)", matches.get(1).display());
    }

    @Test
    void renameTaskReturnsSnapshotsAndPreservesOtherDetails() throws KafkaException {
        TaskList tasks = new TaskList();
        tasks.addTask(new Deadline("submit draft", "Sunday"));
        tasks.markTask(1);

        RenameResult result = tasks.renameTask(1, "submit final report");

        assertEquals("[D][X] submit draft (by: Sunday)", result.oldDisplay());
        assertEquals("[D][X] submit final report (by: Sunday)", result.newDisplay());
        assertEquals("D | 1 | submit final report | Sunday",
                tasks.getTasks().get(0).toDataString());
    }
}
