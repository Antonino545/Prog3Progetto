package it.unito.prog3progetto.Client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientController {
  @FXML
  private ListView<Mail> mailListView;
  private Stage primaryStage;

  public void initialize() {
    // Creazione della lista di oggetti MailItem
    ObservableList<Mail> items = FXCollections.observableArrayList(
            new Mail("sender1@example.com", "Subject 1", "Ciao come stai?\nIo bene ragazzi! Voi? Spero bene!"),
            new Mail("sender2@example.com", "Subject 2", "Content 2"),
            new Mail("sender3@example.com", "Subject 3", "Content 3"),
            new Mail("sender4@example.com", "Subject 4", "Content 4"),
            new Mail("sender5@example.com", "Subject 5", "Content 5")
    );

    mailListView.setItems(items);

    // Personalizzazione della visualizzazione delle celle
    mailListView.setCellFactory(param -> new MailItemCell(primaryStage));
  }

  public void setPrimaryStage(Stage primaryStage) {
    this.primaryStage = primaryStage;
  }

  public void NewEmail(ActionEvent actionEvent) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("Newemail.fxml"));
      Parent root = loader.load();

      NewMailController controller = loader.getController();
      Stage stage = new Stage();
      stage.setScene(new Scene(root));
      stage.setTitle("Dettagli Email");
      stage.setResizable(false);
      stage.show();
    } catch (IOException e) {
      System.out.println("Errore durante l'apertura della finestra di dettaglio email");
    }
  }
}
