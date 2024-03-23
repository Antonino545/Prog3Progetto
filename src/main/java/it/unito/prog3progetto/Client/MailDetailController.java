package it.unito.prog3progetto.Client;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class MailDetailController {
  public Label senderLabel;
  public Label subjectLabel;
  public Label contentLabel;
  public Label destinationslabel;
  public Label datalabel;

  public void setMailDetails(String sender, String subject, String content, String string, String data) {
    senderLabel.setText(sender);
    subjectLabel.setText(subject);
    contentLabel.setText( content);
    destinationslabel.setText(string);
    datalabel.setText(data);

  }

  public void handleReply(ActionEvent actionEvent) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("Newemail.fxml"));

      Parent root = loader.load();
      NewMailController controller = loader.getController();
      controller.setUser(senderLabel.getText());
      controller.initialize("reply", senderLabel.getText(), subjectLabel.getText(), contentLabel.getText(), datalabel.getText());
      Stage stage = new Stage();
      stage.setScene(new Scene(root));
      stage.setTitle("Reply Email");
      stage.setResizable(false);
      stage.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void handleReplyAll(ActionEvent actionEvent) {
  }

  public void handleForward(ActionEvent actionEvent) {
  }
}
