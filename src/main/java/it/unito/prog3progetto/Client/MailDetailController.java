package it.unito.prog3progetto.Client;

import it.unito.prog3progetto.Lib.Email;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MailDetailController {
  public Label senderLabel;
  public Label subjectLabel;
  public Label contentLabel;
  public Label destinationslabel;
  ArrayList<String> destinations;
  public Label datalabel;
  UUID id;

  public void setMailDetails(String sender, String subject, String content, ArrayList<String> Destinations, String data, UUID id) {
    senderLabel.setText(sender);
    subjectLabel.setText(subject);
    contentLabel.setText( content);
    this.destinations=Destinations;
    destinationslabel.setText(Destinations.toString());
    datalabel.setText(data);
    this.id=id;


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

  public void handleDelete(ActionEvent actionEvent) {
    Client client = new Client(senderLabel.getText());
    String host= "127.0.0.1";
    int port= 4445;
    client.connectToServer(host, port);
    client.DeleteMail(host, port, new Email(senderLabel.getText(), destinations, null, null,null,id));
  }
}
