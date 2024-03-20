module it.unito.prog3progetto {
  requires javafx.controls;
  requires javafx.fxml;


  opens it.unito.prog3progetto to javafx.fxml;
  exports it.unito.prog3progetto;
}