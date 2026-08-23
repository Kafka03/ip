package kafka.exception;

/**
 * Represents an error caused by incomplete or malformed command input.
 */
public class ParserException extends KafkaException {
    public ParserException(String message) {
        super(message);
    }
}
