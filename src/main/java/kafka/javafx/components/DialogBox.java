package kafka.javafx.components;

import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * Displays a message together with its associated profile image.
 */
public class DialogBox extends HBox {
    private static final String LAYOUT_LOAD_ERROR =
            "Unable to load the dialog box layout.";

    @FXML
    private TextFlow dialog;
    @FXML
    private ImageView displayPicture;

    /**
     * Creates a dialog box containing the specified message and image.
     *
     * @param message message to display
     * @param image profile image to display beside the message
     * @throws IllegalStateException if the dialog box layout cannot be loaded
     */
    public DialogBox(String message, Image image) {
        FXMLLoader fxmlLoader = new FXMLLoader(DialogBox.class.getResource("/view/DialogBox.fxml"));
        fxmlLoader.setController(this);
        fxmlLoader.setRoot(this);
        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new IllegalStateException(LAYOUT_LOAD_ERROR, exception);
        }

        dialog.getChildren().add(new Text(message));
        displayPicture.setImage(image);
        // Center-crop portraits to a square so the rounded clip fits without stretching the image.
        double imageSize = Math.min(image.getWidth(), image.getHeight());
        double cropX = (image.getWidth() - imageSize) / 2;
        double cropY = (image.getHeight() - imageSize) / 2;
        displayPicture.setViewport(new Rectangle2D(cropX, cropY, imageSize, imageSize));
    }

    /**
     * Styles and aligns this dialog box as a reply from Kafka.
     */
    private void styleAsKafkaReply() {
        this.setAlignment(Pos.TOP_LEFT);
        dialog.getStyleClass().add("reply-label");
        ObservableList<Node> children = FXCollections.observableArrayList(this.getChildren());
        FXCollections.reverse(children);
        this.getChildren().setAll(children);
    }

    /**
     * Returns a right-aligned dialog box for a message from the user.
     *
     * @param message message to display
     * @param image user image to display
     * @return right-aligned user dialog box
     */
    public static DialogBox getUserDialog(String message, Image image) {
        return new DialogBox(message, image);
    }

    /**
     * Returns a left-aligned dialog box for a response from Kafka.
     *
     * @param message message to display
     * @param image Kafka image to display
     * @return left-aligned Kafka dialog box
     */
    public static DialogBox getKafkaDialog(String message, Image image) {
        String displayMessage = removeConsoleDividers(message);
        DialogBox dialogBox = new DialogBox(displayMessage, image);
        dialogBox.dialog.getChildren().setAll(MessageTextFormatter.formatResponse(displayMessage));
        dialogBox.styleAsKafkaReply();
        return dialogBox;
    }

    /**
     * Removes the console's outer divider lines while preserving the message contents.
     */
    private static String removeConsoleDividers(String message) {
        String divider = "_".repeat(60);
        if (message.startsWith(divider + "\n")) {
            message = message.substring(divider.length() + 1);
        }
        if (message.endsWith("\n" + divider)) {
            message = message.substring(0, message.length() - divider.length() - 1);
        }
        return message;
    }

    /**
     * Returns a left-aligned dialog box styled as an error response from Kafka.
     *
     * @param message error message to display
     * @param image Kafka image to display
     * @return left-aligned error dialog box
     */
    public static DialogBox getErrorDialog(String message, Image image) {
        DialogBox dialogBox = new DialogBox(removeConsoleDividers(message), image);
        dialogBox.styleAsKafkaReply();
        dialogBox.dialog.getStyleClass().add("error-label");
        return dialogBox;
    }
}

