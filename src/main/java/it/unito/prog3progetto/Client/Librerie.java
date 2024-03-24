package it.unito.prog3progetto.Client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

public class Librerie {
  public static Client readUserEmailFromFile() throws IOException {
    String fileName = "user_email.txt";
    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
      System.out.println("Contenuto del file " + fileName + ":");
      String line;
      while ((line = reader.readLine()) != null) {
        return new Client(line);
      }
    }
    return null;
  }
  public static void alert(String message, Alert.AlertType type) {
    Alert alert = new Alert(type);
    alert.setTitle("Avviso");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
  public void loadLogin(Stage primaryStage) throws IOException {
deleteEmailFile();
    FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
    Parent root = loader.load();
    LoginController controller = loader.getController();
    controller.setPrimaryStage(primaryStage);
    Scene scene = new Scene(root);
    scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
    primaryStage.setScene(scene);
    primaryStage.setTitle("Email Client - Progetto di Programmazione 3");
    primaryStage.show();
  }


  private void deleteEmailFile() {
    try {
      // Percorso del file user_email.txt
      String filePath = "user_email.txt";
      File file = new File(filePath);

      // Controlla se il file esiste e cancellalo
      if (file.exists()) {
        if (file.delete()) {
          System.out.println("Il file user_email.txt è stato eliminato con successo.");
        } else {
          System.out.println("Impossibile eliminare il file user_email.txt.");
        }
      } else {
        System.out.println("Il file user_email.txt non esiste.");
      }
    } catch (Exception e) {
      // Gestisci le eccezioni, ad esempio mostrando un messaggio di errore all'utente
      e.printStackTrace();
    }
  }



}
