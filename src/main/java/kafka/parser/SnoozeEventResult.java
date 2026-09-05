package kafka.parser;

import java.util.Optional;

/**
 * Stores parsed changes to an event's schedule.
 *
 * @param taskNumber one-based number of the event to snooze
 * @param newFrom replacement start, if supplied
 * @param newTo replacement end, if supplied
 */
public record SnoozeEventResult(
        int taskNumber, Optional<String> newFrom, Optional<String> newTo)
        implements SnoozeRequest {
}
