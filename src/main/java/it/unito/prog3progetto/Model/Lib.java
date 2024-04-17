package it.unito.prog3progetto.Model;

import javafx.scene.control.Alert;


public class Lib {

  public static void alert(String message, Alert.AlertType type) {
    Alert alert = new Alert(type);
    alert.setTitle("Avviso");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }


}
