package it.unito.prog3progetto.Client;

import it.unito.prog3progetto.Client.Controller.ClientController;
import it.unito.prog3progetto.Client.Controller.MailDetailController;
import it.unito.prog3progetto.Model.Email;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
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

import static it.unito.prog3progetto.Model.Lib.Alert;

/**
 * Classe per la rappresentazione grafica di una singola email nella lista delle email
 */
public class MailItemCell extends ListCell<Email> {
  private static ClientController clientController;
  private final ClientModel client;

  /**
   * Costruttore della classe MailItemCell
   * @param clientController Controller del client
   * @param client Modello del client
   */
  public MailItemCell(ClientController clientController, ClientModel client) {
    this.client = client;
    MailItemCell.clientController = clientController;
  }

  /**
   * Metodo per aggiornare la grafica di una singola email
   * @param email Email da visualizzare
   * @param empty Flag per indicare se la cella è vuota
   */
  @Override
  protected void updateItem(Email email, boolean empty) {// viene chiamato ogni volta che viene aggiornata la cella con una nuova email
    super.updateItem(email, empty);
    if (email != null && !empty) {
      HBox hbox = new HBox();
      VBox vbox = new VBox();
      Label senderLabel = new Label();
      senderLabel.setStyle("-fx-font-weight: bold;");
      Label toLabel = new Label();
      Label subjectLabel = new Label();
      Label dateLabel = new Label();
      Label contentLabel = new Label();
      // Bind dei dati dell'email alle label
      senderLabel.textProperty().bind(Bindings.concat("Da: ", email.getsenderProprierty()));
      toLabel.textProperty().bind(Bindings.concat("A: ", email.getDestinationsProprierty()));
      dateLabel.textProperty().bind(Bindings.concat("Data: ", email.getDatesendMailProprierty()));
      subjectLabel.textProperty().bind( Bindings.concat("Oggetto: ", email.getSubjectProprierty()));
      contentLabel.textProperty().bind(Bindings.concat("Contenuto: ", email.getContentProprierty()));
      vbox.getChildren().addAll(senderLabel,toLabel, dateLabel, subjectLabel, contentLabel );
      Button deleteButton = new Button("Cancella");
      deleteButton.getStyleClass().add("button-primary");
      deleteButton.setOnAction(event -> deleteEmail(email));
      hbox.getChildren().addAll(vbox, deleteButton);
      HBox.setHgrow(vbox, Priority.ALWAYS);
      setGraphic(hbox);
      getStyleClass().add("emailitem");
      setOnMouseClicked(event -> openMailDetailView(email));
    } else {
      if (!Platform.isFxApplicationThread()) {
        Platform.runLater(() -> {
          setGraphic(null);
          setText(null);
        });
      } else {
        setGraphic(null);
        setText(null);
        }
    }
  }

  /**
   * Metodo per cancellare un'email dalla lista delle email
   * @param email Email da cancellare
   */
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

  /**
   * Metodo per aprire la finestra di dettaglio di una email
   * @param email Email da visualizzare
   */
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
      Alert("Errore durante l'apertura della finestra di dettaglio email", Alert.AlertType.ERROR);
    }
  }
}
