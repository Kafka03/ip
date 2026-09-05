package kafka.task;

/**
 * Stores display snapshots from before and after a task is snoozed.
 *
 * @param oldDisplay task display before rescheduling
 * @param newDisplay task display after rescheduling
 */
public record SnoozeResult(String oldDisplay, String newDisplay) {
}
