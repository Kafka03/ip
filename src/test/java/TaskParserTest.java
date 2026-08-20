import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Tests conversion of command text into tasks and task numbers.
 */
class TaskParserTest {
    @Test
    void parseTodoReturnsTodoAndTrimsDescription() throws ParserException {
        Task task = TaskParser.parseTodo("todo   read book   ");

        assertInstanceOf(Todo.class, task);
        assertEquals("[T][ ] read book", task.display());
    }

    @Test
    void parseTodoRejectsEmptyDescription() {
        assertThrows(ParserException.class, () -> TaskParser.parseTodo("todo   "));
    }

    @Test
    void parseDeadlinePreservesDeadlineAsText() throws ParserException {
        Task task = TaskParser.parseDeadline("deadline do homework /by no idea :-p");

        assertInstanceOf(Deadline.class, task);
        assertEquals("[D][ ] do homework (by: no idea :-p)", task.display());
    }

    @Test
    void parseDeadlineRejectsMissingByMarker() {
        assertThrows(ParserException.class,
                () -> TaskParser.parseDeadline("deadline return book"));
    }

    @Test
    void parseDeadlineRejectsEmptyDeadline() {
        assertThrows(ParserException.class,
                () -> TaskParser.parseDeadline("deadline return book /by"));
    }

    @Test
    void parseEventReturnsEvent() throws ParserException {
        Task task = TaskParser.parseEvent(
                "event project meeting /from Mon 2pm /to 4pm");

        assertInstanceOf(Event.class, task);
        assertEquals("[E][ ] project meeting (from: Mon 2pm to: 4pm)",
                task.display());
    }

    @Test
    void parseEventRejectsMissingMarkers() {
        assertThrows(ParserException.class,
                () -> TaskParser.parseEvent("event project meeting"));
    }

    @Test
    void parseEventRejectsEmptyStartOrEnd() {
        assertThrows(ParserException.class,
                () -> TaskParser.parseEvent("event project meeting /from /to 4pm"));
        assertThrows(ParserException.class,
                () -> TaskParser.parseEvent("event project meeting /from 2pm /to"));
    }

    @Test
    void parseTaskNumberReturnsPositiveWholeNumber() throws ParserException {
        assertEquals(12, TaskParser.parseTaskNumber("delete 12", "delete"));
    }

    @Test
    void parseTaskNumberRejectsInvalidNumbers() {
        assertThrows(ParserException.class,
                () -> TaskParser.parseTaskNumber("mark abc", "mark"));
        assertThrows(ParserException.class,
                () -> TaskParser.parseTaskNumber("mark 0", "mark"));
    }
}
