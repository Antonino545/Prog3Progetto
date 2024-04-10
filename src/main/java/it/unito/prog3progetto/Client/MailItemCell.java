package it.unito.prog3progetto.Client;

import it.unito.prog3progetto.Client.Controller.ClientController;
import it.unito.prog3progetto.Client.Controller.MailDetailController;
import it.unito.prog3progetto.Model.Email;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class MailItemCell extends ListCell<Email> {
  private static ClientController clientController;
  private final ClientModel client;
  private static final String host = "127.0.0.1";
  private static final int port = 4445;

  public MailItemCell(ClientController clientController, ClientModel client) {
    this.client = client;
    MailItemCell.clientController = clientController;
  }

  @Override
  protected void updateItem(Email email, boolean empty) {
    super.updateItem(email, empty);
    if (email != null && !empty) {
      HBox hbox = new HBox();
      VBox vbox = new VBox();
      Label senderLabel = new Label("Da: " + email.getSender());
      senderLabel.setStyle("-fx-font-weight: bold;");
      Label toLabel = new Label("A: " + email.getDestinations());
      Label subjectLabel = new Label("Oggetto: " + email.getSubject());
      Label dateLabel = new Label("Data: " + email.getItalianDate());
      Label contentLabel = new Label(email.getContent().split("\n")[0]); // Mostra solo la prima riga del contenuto
      vbox.getChildren().addAll(senderLabel, dateLabel, subjectLabel, contentLabel, toLabel);
      Button deleteButton = new Button("Cancella");
      deleteButton.getStyleClass().add("button-primary");
      deleteButton.setOnAction(event -> deleteEmail(email));
      hbox.getChildren().addAll(vbox, deleteButton);
      HBox.setHgrow(vbox, Priority.ALWAYS);
      setGraphic(hbox);
      getStyleClass().add("emailitem");
      setOnMouseClicked(event -> openMailDetailView(email));
    } else {
      setText(null);
      setGraphic(null);
    }
  }

  private void deleteEmail(Email email) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Conferma Cancella Email");
    alert.setHeaderText("Cancellazione Email");
    alert.setContentText("Sei sicuro di voler cancellare l'email selezionata?");
    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      clientController.deleteEmail(email); // Rimuovi l'email tramite il ClientController
    }
  }

  private void openMailDetailView(Email email) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("MailDetailView.fxml"));
      Parent root = loader.load();

      MailDetailController controller = loader.getController();
      controller.initialize(client, email);
      Scene scene = new Scene(root);
      scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
      Stage stage = new Stage();
      stage.setScene(scene);
      stage.setTitle("Dettagli Email");
      stage.setMinHeight(400);
      stage.setMinWidth(600);
      stage.show();
    } catch (IOException e) {
      System.out.println("Errore durante l'apertura della finestra di dettaglio email: " + e.getMessage());
    }
  }
}
