package kafka;

/**
 * Represents the result of processing one user command.
 *
 * @param message formatted response to display
 * @param isError whether the response represents an error
 */
public record KafkaResponse(String message, boolean isError) {
}
