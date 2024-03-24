package it.unito.prog3progetto.Client;

import javafx.application.Application;
import javafx.stage.Stage;
import java.io.IOException;

public class ClientApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        Librerie lib = new Librerie();
        lib.loadLogin(primaryStage);
    }

    public static void main(String[] args) {
        launch();
    }
}
