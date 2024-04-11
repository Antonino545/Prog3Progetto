package it.unito.prog3progetto.Client.Controller;

import it.unito.prog3progetto.Client.ClientModel;
import it.unito.prog3progetto.Model.Email;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static it.unito.prog3progetto.Model.Lib.alert;

public class MailDetailController {
  @FXML
  private Label senderLabel;
  @FXML
  private Label subjectLabel;
  @FXML
  private Label contentLabel;
  @FXML
  private Label destinationsLabel;
  @FXML
  private Label dateLabel;

  private final SimpleStringProperty senderProperty = new SimpleStringProperty();
  private final SimpleStringProperty subjectProperty = new SimpleStringProperty();
  private final SimpleStringProperty contentProperty = new SimpleStringProperty();
  private final SimpleListProperty<String> destinationsProperty = new SimpleListProperty<>();
  private final SimpleStringProperty dateProperty = new SimpleStringProperty();

  private ClientModel clientModel;
  ArrayList<String> destinations;

  /**
   * Metodo per inizializzare la finestra di dettaglio di una email
   * @param clientModel Modello del client
   * @param email Email da visualizzare
   */
  public void initialize(ClientModel clientModel, Email email) {
    this.clientModel = clientModel;
    senderProperty.set(email.getSender());
    subjectProperty.set(email.getSubject());
    contentProperty.set(email.getContent());
    dateProperty.set(email.getItalianDate());
    destinationsProperty.set(FXCollections.observableArrayList(email.getDestinations()));
    destinationsLabel.textProperty().bind(destinationsProperty.asString());
    senderLabel.textProperty().bind(senderProperty);
    subjectLabel.textProperty().bind(subjectProperty);
    contentLabel.textProperty().bind(contentProperty);
    destinationsLabel.textProperty().bind(destinationsProperty.asString());
    dateLabel.textProperty().bind(dateProperty);
    destinations=email.getDestinations();

  }

  /**
   *  Metodo per rispondere ad una email
   */
  public void handleReply() {
    try {
      FXMLLoader loader = new FXMLLoader(new File("src/main/resources/it/unito/prog3progetto/Client/Newemail.fxml").toURI().toURL());

      Parent root = loader.load();
      NewMailController controller = loader.getController();
      controller.initialize("reply", senderLabel.getText(),destinations, subjectLabel.getText(), contentLabel.getText(), dateLabel.getText(),clientModel);
      Stage stage = new Stage();
      stage.setScene(new Scene(root));
      stage.setTitle("Reply Email");
      stage.setResizable(false);
      stage.show();
    } catch (IOException e) {
      alert("Errore durante l'apertura della finestra di REPLY :"+e.getMessage() , Alert.AlertType.ERROR);
    }
  }

  /**
   * Metodo per rispondere a tutti i destinatari di una email
   */
  public void handleReplyAll() {
    try {
      FXMLLoader loader = new FXMLLoader(new File("src/main/resources/it/unito/prog3progetto/Client/Newemail.fxml").toURI().toURL());

      Parent root = loader.load();
      NewMailController controller = loader.getController();
      controller.initialize("replyall", senderLabel.getText(),destinations, subjectLabel.getText(), contentLabel.getText(), dateLabel.getText(),clientModel);
      Stage stage = new Stage();
      stage.setScene(new Scene(root));
      stage.setTitle("Reply All Email");
      stage.setResizable(false);
      stage.show();
    } catch (IOException e) {
     alert("Errore durante l'apertura della finestra di REPLY ALL:"+e.getMessage() , Alert.AlertType.ERROR);
    }
  }

  /**
   * Metodo per inoltrare una email ad un altro destinatario
   */
  public void handleForward() {
    try {
      FXMLLoader loader = new FXMLLoader(new File("src/main/resources/it/unito/prog3progetto/Client/Newemail.fxml").toURI().toURL());
      Parent root = loader.load();
      NewMailController controller = loader.getController();
      controller.initialize("forward", senderLabel.getText(),null, subjectLabel.getText(), contentLabel.getText(), dateLabel.getText(),clientModel);
      Stage stage = new Stage();
      stage.setScene(new Scene(root));
      stage.setTitle("Forward Email");
      stage.setResizable(false);
      stage.show();
    } catch (IOException e) {
      alert("Errore durante l'apertura della finestra di Forward:"+e.getMessage() , Alert.AlertType.ERROR);
    }
  }

}