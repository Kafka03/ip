/**
 * Represents an expected error that Kafka can display without terminating.
 */
class KafkaException extends Exception {
    KafkaException(String message) {
        super(message);
    }
}
