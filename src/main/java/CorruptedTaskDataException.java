/**
 * Indicates that a task data file was read successfully but contains a record
 * that cannot be converted into a task.
 */
class CorruptedTaskDataException extends KafkaException {
    CorruptedTaskDataException(String message) {
        super(message);
    }
}
