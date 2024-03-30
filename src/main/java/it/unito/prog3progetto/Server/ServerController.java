package it.unito.prog3progetto.Server;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class ServerController {

  @FXML
  public TextArea logTextArea;

  private ServerModel server;

  @FXML
  public void initialize() {
    server = new ServerModel(logTextArea);
  }

  // Dichiarazione della variabile di stato del server
  private boolean serverAvviato = false;

  @FXML
  public void startServer(ActionEvent event) {
    if (!serverAvviato) {
      // Avvia il server sulla porta 4445 in un thread separato
      Thread serverThread = new Thread(() -> server.listen(4445));
      serverThread.setDaemon(true);
      serverThread.start();

      // Aggiungi un messaggio di avvio alla TextArea

      // Imposta lo stato del server come avviato
      serverAvviato = true;
    } else {
      // Se il server è già avviato, segnala che è già in esecuzione
      logTextArea.appendText("Il server è già avviato.\n");
    }
  }


}
