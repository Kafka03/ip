package kafka.exception;

/**
 * Represents an expected hiccup that Kafka can explain without crashing.
 */
public class KafkaException extends Exception {
    /**
     * Creates an expected error with a user-facing explanation.
     *
     * @param message explanation Kafka should show the user
     */
    public KafkaException(String message) {
        super(message);
    }

    /**
     * Creates an expected error while preserving the technical cause for debugging.
     *
     * @param message explanation Kafka should show the user
     * @param cause lower-level problem that caused this error
     */
    public KafkaException(String message, Throwable cause) {
        super(message, cause);
    }
}
