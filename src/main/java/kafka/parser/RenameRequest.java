package kafka.parser;

/**
 * Stores the arguments supplied to a rename command.
 *
 * @param taskNumber one-based number of the task to rename
 * @param newName replacement task name
 */
public record RenameRequest(int taskNumber, String newName) {
}
