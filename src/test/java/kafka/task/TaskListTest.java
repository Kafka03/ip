package kafka.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import kafka.exception.KafkaException;

import org.junit.jupiter.api.Test;

/**
 * Keeps the task list honest about ordering, status changes, and deletion.
 */
class TaskListTest {
    @Test
    void showTasksDisplaysConcreteTasksInOrder() {
        TaskList tasks = new TaskList();
        tasks.addTask(new Todo("read book"));
        tasks.addTask(new Deadline("return book", "Sunday"));
        ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;

        try {
            System.setOut(new PrintStream(capturedOutput, true, StandardCharsets.UTF_8));
            tasks.showTasks();
        } finally {
            System.setOut(originalOutput);
        }

        String output = capturedOutput.toString(StandardCharsets.UTF_8);
        int firstTaskPosition = output.indexOf("1.[T][ ] read book");
        int secondTaskPosition = output.indexOf(
                "2.[D][ ] return book (by: Sunday)");
        assertTrue(firstTaskPosition >= 0, "The first task should be displayed");
        assertTrue(secondTaskPosition > firstTaskPosition,
                "The second task should follow the first task");
    }

    @Test
    void markAndUnmarkChangeSelectedTask() throws KafkaException {
        TaskList tasks = new TaskList();
        tasks.addTask(new Todo("read book"));
        tasks.addTask(new Todo("return book"));

        assertEquals("[T][X] return book", tasks.markTask(2));
        assertEquals("[T][ ] return book", tasks.unmarkTask(2));
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
        assertEquals("[E][X] third (from: Monday to: Tuesday)", tasks.markTask(2));
    }
}
