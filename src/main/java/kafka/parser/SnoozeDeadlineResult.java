package kafka.parser;

/**
 * Stores a parsed change to a deadline's schedule.
 *
 * @param taskNumber one-based number of the deadline to snooze
 * @param newBy replacement deadline
 */
public record SnoozeDeadlineResult(int taskNumber, String newBy)
        implements SnoozeRequest {
}
