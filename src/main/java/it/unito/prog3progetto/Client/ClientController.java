package it.unito.prog3progetto.Client;

import it.unito.prog3progetto.Lib.Email;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class ClientController {
  public Label email;
  @FXML
  public Label indexlenght;
  @FXML
  private ListView<Email> mailListView;
  private Stage primaryStage;
  private Client client;
  private Timeline timeline;


  public void initialize(Client client) {
    this.client = client;
    if(client != null) {
      email.setText(client.getUserId());
      FullRefresh(null);
    }
    timeline = new Timeline(new KeyFrame(Duration.minutes(10), this::Refresh));
    // Imposta il ciclo infinito
    timeline.setCycleCount(Timeline.INDEFINITE);
    // Avvia il Timeline
    timeline.play();


  }

  public void setPrimaryStage(Stage primaryStage) {
    this.primaryStage = primaryStage;
  }

  public void NewEmail(ActionEvent actionEvent) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("Newemail.fxml"));

      Parent root = loader.load();
      NewMailController controller = loader.getController();
      controller.initialize("sendmail");
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


  public void Refresh(ActionEvent actionEvent) {
    if (client != null) {
      email.setText(client.getUserId());
      String host = "127.0.0.1";
      int port = 4445;
      if (client.connectToServer(host, port)) {
        System.out.println("Connessione al server riuscita");
        ObservableList<Email> lastsitems = mailListView.getItems();
        Email lastEmail = lastsitems.isEmpty() ? null : lastsitems.get(0); // Controlla se la lista è vuota
        ArrayList<Email> receivedEmails = client.receiveEmail(host, port, client.getUserId(), lastEmail != null ? lastEmail.getDatesendMail() : null);

        ObservableList<Email> items = FXCollections.observableArrayList(receivedEmails);
        items.sort((o1, o2) -> o2.getDatesendMail().compareTo(o1.getDatesendMail()));

        if (!items.isEmpty()) {
          // Aggiunge le nuove email in cima alla ListView
          mailListView.getItems().addAll(0, items);
          indexlenght.setText(String.valueOf(items.size()));
          System.out.println("Email ricevute");
        }else {
          FullRefresh(null);
        }
      } else {
        System.out.println("Connessione al server non riuscita");
      }
    } else {
      System.out.println("Client is null. Cannot refresh.");
    }
  }
  public Client getClient() {
    return client;
  }



  public void FullRefresh(ActionEvent actionEvent) {
    String host= "127.0.0.1";
    int port= 4445;
    if(client.connectToServer(host, port)){
      ObservableList<Email> items = FXCollections.observableArrayList(client.receiveEmail(host, port, client.getUserId(), null));
      items.sort((o1, o2) -> o2.getDatesendMail().compareTo(o1.getDatesendMail()));
      // Cancella gli elementi precedenti dalla lista
      mailListView.getItems().clear();


      // Aggiunge gli elementi ricevuti dalla lista
      mailListView.setItems(items);

      // Imposta il conteggio degli elementi
      indexlenght.setText(String.valueOf(items.size()));

      // Imposta il cell factory
      mailListView.setCellFactory(param -> new MailItemCell(primaryStage));

      System.out.println("Email ricevute");
    } else {
      System.out.println("Connessione al server non riuscita");
    }
  }

}
