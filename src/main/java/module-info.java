module it.unito.prog3progetto.Client {
  requires javafx.controls;
  requires javafx.fxml;


  opens it.unito.prog3progetto.Client to javafx.fxml;
  exports it.unito.prog3progetto.Client;
  exports it.unito.prog3progetto.Server;
  exports it.unito.prog3progetto.Model;
  opens it.unito.prog3progetto.Model to javafx.fxml;
  exports it.unito.prog3progetto.Client.Controller;
  opens it.unito.prog3progetto.Client.Controller to javafx.fxml;


}