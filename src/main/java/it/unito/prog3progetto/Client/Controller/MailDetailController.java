package it.unito.prog3progetto.Client.Controller;

import it.unito.prog3progetto.Client.ClientModel;
import it.unito.prog3progetto.Model.Email;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.UUID;

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

  private SimpleStringProperty senderProperty = new SimpleStringProperty();
  private SimpleStringProperty subjectProperty = new SimpleStringProperty();
  private SimpleStringProperty contentProperty = new SimpleStringProperty();
  private SimpleListProperty<String> destinationsProperty = new SimpleListProperty<>();
  private SimpleStringProperty dateProperty = new SimpleStringProperty();

  private UUID id;
  private ClientModel clientModel;
  ArrayList<String> destinations;

  public void initialize(ClientModel clientModel, Email email) {
    this.clientModel = clientModel;
    id = email.getId();
    senderProperty.set(email.getSender());
    subjectProperty.set(email.getSubject());
    contentProperty.set(email.getContent());
    dateProperty.set(email.getItalianDate());
    ObservableList<String> destinationsObservable = FXCollections.observableArrayList(email.getDestinations());
    destinationsProperty.set(FXCollections.observableArrayList(email.getDestinations()));
    destinationsLabel.textProperty().bind(destinationsProperty.asString());
    senderLabel.textProperty().bind(senderProperty);
    subjectLabel.textProperty().bind(subjectProperty);
    contentLabel.textProperty().bind(contentProperty);
    destinationsLabel.textProperty().bind(destinationsProperty.asString());
    dateLabel.textProperty().bind(dateProperty);
    destinations=email.getDestinations();

  }

  public void handleReply(ActionEvent actionEvent) {
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
      e.printStackTrace();
    }
  }

  public void handleReplyAll(ActionEvent actionEvent) {
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
      e.printStackTrace();
    }
  }

  public void handleForward(ActionEvent actionEvent) {
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
      e.printStackTrace();
    }
  }







}