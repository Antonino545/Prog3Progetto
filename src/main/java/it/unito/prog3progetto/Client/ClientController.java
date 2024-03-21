package it.unito.prog3progetto.Client;

import it.unito.prog3progetto.Lib.Mail;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class ClientController {
  @FXML
  private ListView<Mail> mailListView;
  private Stage primaryStage;
  private Client c;

  public void initialize() {
    // Creazione della lista di oggetti MailItem
    ObservableList<Mail> items = FXCollections.observableArrayList(
            new Mail("sender1@example.com","mario.rossi@progmail.com", "Subject 1", "Ciao come stai?\nIo bene ragazzi! Voi? Spero bene!"),
            new Mail("sender2@example.com","mario.rossi@progmail.com", "Subject 2", "Content 2"),
            new Mail("sender3@example.com","mario.rossi@progmail.com", "Subject 3", "Content 3"),
            new Mail("sender4@example.com","mario.rossi@progmail.com", "Subject 4", "Content 4"),
            new Mail("sender5@example.com","mario.rossi@progmail.com", "Subject 5", "Content 5")

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

  public void logout(ActionEvent actionEvent) {
    try {
      c.closeConnections();
      FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
      Parent root = loader.load();

      LoginController controller = loader.getController();
      // Assuming primaryStage is properly initialized before calling logout
      if (primaryStage != null) {
        controller.setPrimaryStage(primaryStage);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
        primaryStage.setScene(scene);


        primaryStage.setTitle("Login");
        primaryStage.setResizable(false);
        primaryStage.show();
      } else {
        System.out.println("Primary stage is null. Cannot set scene.");
      }
    } catch (IOException e) {
      System.out.println("Error opening login window: " + e.getMessage());
    }
  }

  public void setCredentials(Client c) {
    this.c =c;
  }
}
