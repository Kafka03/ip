package kafka.task;

/**
 * Stores display snapshots from before and after a task rename.
 *
 * @param oldDisplay task display before renaming
 * @param newDisplay task display after renaming
 */
public record RenameResult(String oldDisplay, String newDisplay) {
}
