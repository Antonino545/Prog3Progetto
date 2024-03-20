package it.unito.prog3progetto;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ClientApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));

        Parent root = loader.load();

        // Get the controller instance
        ClientController controller = loader.getController();

        // Set the primaryStage reference
        controller.setPrimaryStage(primaryStage);

        Scene scene = new Scene(root, 500, 500);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("Email Client - Progetto di Programmazione 3");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
