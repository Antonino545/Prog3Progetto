package it.unito.prog3progetto.Client;

import it.unito.prog3progetto.Lib.Mail;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class ClientController {
  public Label email;
  @FXML
  private ListView<Mail> mailListView;
  private Stage primaryStage;
  private Client client;

  public void initialize(Client client) {
    this.client = client;
    System.out.println("ClientController initialized");
    if(client != null) {
      email.setText(client.getUserId());
    }
    ArrayList<String> destinations = new ArrayList<String>();
    destinations.add("mario.rossi@progmail.com");
    ObservableList<Mail> items = FXCollections.observableArrayList(
            new Mail("sender1@example.com", destinations , "Subject 1", "Ciao come stai?\nIo bene ragazzi! Voi? Spero bene!", Date.from(Instant.now())),
            new Mail("sender2@example.com",destinations, "Subject 2", "Content 2", Date.from(Instant.now())),
            new Mail("sender3@example.com",destinations, "Subject 3", "Content 3", Date.from(Instant.now())),
            new Mail("sender4@example.com",destinations, "Subject 4", "Content 4", Date.from(Instant.now())),
            new Mail("sender5@example.com",destinations, "Subject 5", "Content 5", Date.from(Instant.now()))
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
      controller.setUser(client.getUserId());
      Stage stage = new Stage();
      stage.setScene(new Scene(root));
      stage.setTitle("Dettagli Email");
      stage.setResizable(false);
      stage.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void logout(ActionEvent actionEvent) {
    try {
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


}
