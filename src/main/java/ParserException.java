/**
 * Represents an error caused by incomplete or malformed command input.
 */
class ParserException extends KafkaException {
    ParserException(String message) {
        super(message);
    }
}
