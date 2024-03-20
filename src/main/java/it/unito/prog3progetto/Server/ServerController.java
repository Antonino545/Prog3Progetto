package it.unito.prog3progetto.Server;

import javafx.scene.control.TextArea;

public class ServerController {

  public TextArea logTextArea;

  public void initialize() {
  setLogTextArea("Server started\n");

  }

  public void setLogTextArea(String log) {
    logTextArea.appendText(log);
  }
}
