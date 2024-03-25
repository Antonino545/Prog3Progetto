package it.unito.prog3progetto.Client;

import it.unito.prog3progetto.Lib.Email;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import it.unito.prog3progetto.Client.MailDetailController;
import it.unito.prog3progetto.Lib.Email;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import static it.unito.prog3progetto.Client.Librerie.readUserEmailFromFile;

public class MailItemCell extends ListCell<Email> {
  private static ClientController clientController;
  private final Stage primaryStage;
  private static final String host = "127.0.0.1";
  private static final int port = 4445;
  public MailItemCell(Stage primaryStage, ClientController clientController) {
    this.primaryStage = primaryStage;
    this.clientController = clientController;
  }


  @Override
  protected void updateItem(Email email, boolean empty) {
    super.updateItem(email, empty);
    if (email != null && !empty) {
      HBox hbox = new HBox();
      VBox vbox = new VBox();
      Label senderLabel = new Label("From: " + email.getSender());
      senderLabel.setStyle("-fx-font-weight: bold;");
      Label subjectLabel = new Label("Subject: " + email.getSubject());
      Label dateLabel = new Label("Date: " + email.getItalianDate());

      String content = email.getContent();
      String firstLine = content.substring(0, Math.min(content.length(), 50));
      int newlineIndex = firstLine.indexOf('\n');
      if (newlineIndex != -1) {
        firstLine = firstLine.substring(0, newlineIndex);
      }
      Label contentLabel = new Label(firstLine);

      vbox.getChildren().addAll(senderLabel, dateLabel, subjectLabel, contentLabel);
      Button deleteButton = getButton(email);
      hbox.getChildren().addAll(vbox, deleteButton);
      HBox.setHgrow(vbox, Priority.ALWAYS);

      setGraphic(hbox);
      getStyleClass().add("emailitem");
      setOnMouseClicked(event -> {
        try {
          FXMLLoader loader = new FXMLLoader(getClass().getResource("MailDetailView.fxml"));
          Parent root = loader.load();

          MailDetailController controller = loader.getController();
          controller.setMailDetails(email.getSender(), email.getSubject(), email.getContent(), email.getDestinations(), email.getDatesendMail().toString(),email.getId());
          controller.setPrimaryStage(primaryStage);

          Scene scene = new Scene(root);
          scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());

          primaryStage.setScene(scene);
          primaryStage.setTitle("Dettagli Email");
          primaryStage.setResizable(true);
          primaryStage.setMinWidth(300); // Imposta la larghezza minima della finestra
          primaryStage.setMinHeight(400); // Imposta l'altezza minima della finestra
          primaryStage.show(); // Mostra la finestra

        } catch (IOException e) {
          System.out.println("Errore durante l'apertura della finestra di dettaglio email");
        }
      });
    } else {
      setText(null);
      setGraphic(null);
    }
  }

  private Button getButton(Email email) {
    Button deleteButton = new Button("Delete");
    deleteButton.setOnAction(event -> {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle("Conferma Cancella Email");
      alert.setHeaderText("Cancellazione Email");
      alert.setContentText("Sei sicuro di voler cancellare l'email selezionata?");

      Optional<ButtonType> result = alert.showAndWait();
      if (result.isPresent() && result.get() == ButtonType.OK) {
        clientController.deleteEmail(email); // Rimuovi l'email tramite il ClientController
      }
    });
    return deleteButton;
  }


}

