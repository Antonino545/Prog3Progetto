package it.unito.prog3progetto.Client;

import it.unito.prog3progetto.Client.Controller.LoginController;
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
        loadLogin(primaryStage);
    }
    public  void loadLogin(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
        Parent root = loader.load();
        LoginController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setWidth(500);
        primaryStage.setHeight(600);
        primaryStage.setTitle("Login - Progetto di Programmazione 3");
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch();
    }
}
