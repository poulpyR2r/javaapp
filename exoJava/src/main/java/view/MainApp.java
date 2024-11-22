package view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/MainView.fxml")); // Charger le fichier FXML
        primaryStage.setTitle("JavaFX Application");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }
}
