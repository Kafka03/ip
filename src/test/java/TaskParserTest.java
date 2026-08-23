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
    void parseDeadlineFormatsRecognizedDate() throws ParserException {
        Task task = TaskParser.parseDeadline("deadline submit report /by 2020-01-18");

        assertEquals("[D][ ] submit report (by: 18 Jan 2020)", task.display());
    }

    @Test
    void parseDeadlineFormatsRecognizedTimeAsTwentyFourHourTime()
            throws ParserException {
        Task task = TaskParser.parseDeadline("deadline sleep /by 11:59pm");

        assertEquals("[D][ ] sleep (by: 2359)", task.display());
    }

    @Test
    void parseDeadlineFormatsRecognizedDateAndTime() throws ParserException {
        Task task = TaskParser.parseDeadline(
                "deadline celebrate /by 2020-01-18 11:59pm");

        assertEquals("[D][ ] celebrate (by: 18 Jan 2020 2359)", task.display());
    }

    @Test
    void parseDeadlineAcceptsSlashAndHumanReadableDates() throws ParserException {
        Task slashDate = TaskParser.parseDeadline("deadline first /by 18/1/2020");
        Task humanDate = TaskParser.parseDeadline("deadline second /by 18 jan 2020");

        assertEquals("[D][ ] first (by: 18 Jan 2020)", slashDate.display());
        assertEquals("[D][ ] second (by: 18 Jan 2020)", humanDate.display());
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
        assertEquals("[E][ ] project meeting (from: Mon 2pm to: 1600)",
                task.display());
    }

    @Test
    void parseEventFormatsDateAndTimeAtBothEndpoints() throws ParserException {
        Task task = TaskParser.parseEvent(
                "event launch /from 2020-01-18 9am /to 2020-01-18 23:59");

        assertEquals("[E][ ] launch (from: 18 Jan 2020 0900"
                + " to: 18 Jan 2020 2359)", task.display());
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
