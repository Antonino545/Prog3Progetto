package it.unito.prog3progetto.Server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ServerApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Server.fxml"));

        Parent root = loader.load();

        // Get the controller instance
        ServerController controller = loader.getController();

        // Set the primaryStage reference

        Scene scene = new Scene(root, 500, 500);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("server.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Email Server - Progetto di Programmazione 3");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
