package kafka.exception;

/**
 * Represents an expected error that Kafka can display without terminating.
 */
public class KafkaException extends Exception {
    public KafkaException(String message) {
        super(message);
    }

    public KafkaException(String message, Throwable cause) {
        super(message, cause);
    }
}
