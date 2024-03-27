package it.unito.prog3progetto.Client.Controller;

import it.unito.prog3progetto.Client.Client;
import it.unito.prog3progetto.Model.Email;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class MailDetailController {
  public Label senderLabel;
  public Label subjectLabel;
  public Label contentLabel;
  public Label destinationslabel;
  ArrayList<String> destinations;
  public Label datalabel;
  UUID id;
  private Client client;

  public void initialize(Client client,Email email) throws IOException {
    this.client = client;
    senderLabel.setText(email.getSender());
    subjectLabel.setText(email.getSubject());
    contentLabel.setText(email.getContent());
    destinationslabel.setText(email.getDestinations().toString());
    datalabel.setText(email.getItalianDate());
    id=email.getId();
  }




  public void handleReply(ActionEvent actionEvent) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("Newemail.fxml"));

      Parent root = loader.load();
      NewMailController controller = loader.getController();
      controller.initialize("reply", senderLabel.getText(),destinations, subjectLabel.getText(), contentLabel.getText(), datalabel.getText());
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
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("Newemail.fxml"));

      Parent root = loader.load();
      NewMailController controller = loader.getController();
      controller.initialize("replyall", senderLabel.getText(),destinations, subjectLabel.getText(), contentLabel.getText(), datalabel.getText());
      Stage stage = new Stage();
      stage.setScene(new Scene(root));
      stage.setTitle("Reply All Email");
      stage.setResizable(false);
      stage.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void handleForward(ActionEvent actionEvent) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("Newemail.fxml"));

      Parent root = loader.load();
      NewMailController controller = loader.getController();
      controller.initialize("forward", senderLabel.getText(),destinations, subjectLabel.getText(), contentLabel.getText(), datalabel.getText());
      Stage stage = new Stage();
      stage.setScene(new Scene(root));
      stage.setTitle("Forward Email");
      stage.setResizable(false);
      stage.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }







}
