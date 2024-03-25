package it.unito.prog3progetto.Client;

import it.unito.prog3progetto.Lib.Email;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static it.unito.prog3progetto.Client.Librerie.alert;
import static it.unito.prog3progetto.Client.Librerie.readUserEmailFromFile;

public class MailDetailController {
  public Label senderLabel;
  public Label subjectLabel;
  public Label contentLabel;
  public Label destinationslabel;
  ArrayList<String> destinations;
  public Label datalabel;
  UUID id;
  private Stage primaryStage;
  private Client client;

  public void initialize() throws IOException {
    this.client = readUserEmailFromFile();
  }

  public void setMailDetails(String sender, String subject, String content, ArrayList<String> Destinations, String data, UUID id) {
    senderLabel.setText(sender);
    subjectLabel.setText(subject);
    contentLabel.setText( content);
    this.destinations=Destinations;
    destinationslabel.setText(Destinations.toString());
    datalabel.setText(data);
    this.id=id;


  }

  public void setPrimaryStage(Stage primaryStage) {
    this.primaryStage = primaryStage;
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



  public void indietro(ActionEvent actionEvent) {
   loader(client);
  }
  public void loader(Client client) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("Client.fxml"));
      Parent root = loader.load();
      ClientController controller = loader.getController();
      controller.setPrimaryStage(primaryStage);
      controller.initialize(client);
      Scene scene = new Scene(root);
      scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
      primaryStage.setScene(scene);
      primaryStage.setTitle("Client");
      primaryStage.setResizable(true);
      primaryStage.setMinWidth(300); // Imposta la larghezza minima della finestra
      primaryStage.setMinHeight(400); // Imposta l'altezza minima della finestra
      primaryStage.show(); // Mostra la finestra
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


}
