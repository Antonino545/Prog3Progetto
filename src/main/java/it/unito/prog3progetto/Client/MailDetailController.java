package it.unito.prog3progetto.Client;

import javafx.event.ActionEvent;
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

  public void handleReply(ActionEvent actionEvent) {
  }

  public void handleReplyAll(ActionEvent actionEvent) {
  }

  public void handleForward(ActionEvent actionEvent) {
  }
}
