module it.unito.prog3progetto.Client {
  requires javafx.controls;
  requires javafx.fxml;


  opens it.unito.prog3progetto.Client to javafx.fxml;
  exports it.unito.prog3progetto.Client;
  exports it.unito.prog3progetto.Server;

}