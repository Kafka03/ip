package kafka.javafx;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import kafka.Kafka;

/**
 * Displays the JavaFX user interface for Kafka.
 */
public class Main extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("/view/MainWindow.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        MainWindow mainWindow = fxmlLoader.getController();
        mainWindow.setKafka(new Kafka());

        stage.setTitle("Kafka");
        stage.setResizable(false);
        stage.setMinHeight(220.0);
        stage.setMinWidth(417.0);
        stage.setScene(scene);
        stage.show();
    }
}
