package it.unito.prog3progetto.Client;

import javafx.scene.control.Label;

public class MailDetailController {
  public Label senderLabel;
  public Label subjectLabel;
  public Label contentLabel;

  public void setMailDetails(String sender, String subject, String content) {
    senderLabel.setText(sender);
    subjectLabel.setText(subject);
    contentLabel.setText(content);
  }
  
}
