import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

// Tests storing, displaying, marking, and unmarking tasks in a task list.
class TaskListTest {
    @Test
    void showTasksDisplaysTasksInOrder() {
        TaskList tasks = new TaskList();
        tasks.addTask("read book");
        tasks.addTask("return book");
        ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();
        PrintStream originalOutput = System.out;

        // Capture the console output produced by showTasks().
        try {
            System.setOut(new PrintStream(capturedOutput, true, StandardCharsets.UTF_8));
            tasks.showTasks();
        } finally {
            System.setOut(originalOutput);
        }

        String output = capturedOutput.toString(StandardCharsets.UTF_8);
        int firstTaskPosition = output.indexOf("1.[ ] read book");
        int secondTaskPosition = output.indexOf("2.[ ] return book");
        assertTrue(firstTaskPosition >= 0, "The first task should be displayed");
        assertTrue(secondTaskPosition > firstTaskPosition,
                "The second task should be displayed after the first task");
    }

    @Test
    void markTaskMarksSelectedTask() {
        TaskList tasks = new TaskList();
        tasks.addTask("read book");
        tasks.addTask("return book");

        // Task numbers are one-based, so task 2 should be the second task.
        assertEquals("[X] return book", tasks.markTask(2));
    }

    @Test
    void unmarkTaskUnmarksSelectedTask() {
        TaskList tasks = new TaskList();
        tasks.addTask("read book");
        tasks.markTask(1);

        // The returned text should contain the restored unchecked state.
        assertEquals("[ ] read book", tasks.unmarkTask(1));
    }
}
