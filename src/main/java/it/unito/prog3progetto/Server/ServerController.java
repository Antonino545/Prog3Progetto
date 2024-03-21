package it.unito.prog3progetto.Server;

import it.unito.prog3progetto.Server.Server;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class ServerController {

  @FXML
  public TextArea logTextArea;

  private Server server;

  @FXML
  public void initialize() {
    server = new Server(logTextArea);
  }

  @FXML
  public void startServer(ActionEvent event) {
    // Avvia il server sulla porta 4445 in un thread separato
    Thread serverThread = new Thread(() -> server.listen(4445));
    serverThread.setDaemon(true);
    serverThread.start();

    // Aggiungi un messaggio di avvio alla TextArea
    logTextArea.appendText("Server avviato su porta 4445\n");
  }
}
