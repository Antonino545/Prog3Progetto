package it.unito.prog3progetto.Server;

import javafx.application.Platform;
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
      System.out.println("Server in avvio");
      // Avvia il server sulla porta 4445 in un thread separato
      Thread serverThread = new Thread(() -> server.listen(4445));
      serverThread.setDaemon(true);
      serverThread.start();
      serverAvviato = true;
    } else {
      // Se il server è già avviato, segnala che è già in esecuzione
      server.appendToLog("Il server è già attivo.");
    }
  }
  public void closeServer(){
    if(serverAvviato){
      serverAvviato = false;
      server.close();
      Platform.runLater(() -> server.appendToLog("Server chiuso."));
    } else {
      server.appendToLog("Il server non è attivo.");
    }
  }



}
