package it.unito.prog3progetto.Client;

import javafx.scene.control.Alert;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class libController {
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
}
