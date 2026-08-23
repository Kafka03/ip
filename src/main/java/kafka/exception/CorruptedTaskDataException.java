package kafka.exception;

/**
 * Reports a task-data line that Kafka cannot safely turn back into a task.
 */
public class CorruptedTaskDataException extends KafkaException {
    /**
     * Creates a corruption error that identifies the troublesome saved data.
     *
     * @param message explanation of where the corrupted data was found
     */
    public CorruptedTaskDataException(String message) {
        super(message);
    }
}
