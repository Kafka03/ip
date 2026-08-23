package kafka.exception;

/**
 * Indicates that a task data file was read successfully but contains a record
 * that cannot be converted into a task.
 */
public class CorruptedTaskDataException extends KafkaException {
    public CorruptedTaskDataException(String message) {
        super(message);
    }
}
