package kafka.javafx;

import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import kafka.Kafka;
import kafka.KafkaResponse;
import kafka.javafx.components.DialogBox;

/**
 * Controller for the main GUI.
 */
public class MainWindow extends AnchorPane {
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private VBox dialogContainer;
    @FXML
    private TextField userInput;

    private Kafka kafka;

    private Image userImage = new Image(this.getClass().getResourceAsStream("/images/gigachad.png"));
    private Image kafkaImage = new Image(this.getClass().getResourceAsStream("/images/franzkafka.jpg"));

    @FXML
    public void initialize() {
        scrollPane.vvalueProperty().bind(dialogContainer.heightProperty());
    }

    /**
     * Sets the Kafka instance used to process input.
     *
     * @param kafka Kafka instance
     */
    public void setKafka(Kafka kafka) {
        this.kafka = kafka;
        greetUponStart();
    }

    /**
     * Creates two dialog boxes, one echoing user input and the other containing Kafka's reply and then appends them to
     * the dialog container. Clears the user input after processing.
     */
    @FXML
    private void handleUserInput() {
        assert kafka != null : "Kafka must be set before processing user input";
        String input = userInput.getText();
        KafkaResponse response = kafka.getResponse(input);

        DialogBox responseDialog = response.isError()
                ? DialogBox.getErrorDialog(response.message(), kafkaImage)
                : DialogBox.getKafkaDialog(response.message(), kafkaImage);
        dialogContainer.getChildren().addAll(
                DialogBox.getUserDialog(input, userImage),
                responseDialog
        );
        userInput.clear();
    }

    private void greetUponStart() {
        String greeting = kafka.greet();
        dialogContainer.getChildren().addAll(
                DialogBox.getKafkaDialog(greeting, kafkaImage)
        );
    }
}

