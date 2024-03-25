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

import static it.unito.prog3progetto.Client.Librerie.writeEmails;

public class ClientController {
  public Label email;
  @FXML
  public Label indexlenght;
  @FXML
  private ListView<Email> mailListView;
  private Stage primaryStage;
  private Client client;
  private Timeline timeline;


  public void initialize(Client client) throws IOException {
    this.client = client;
    if(client != null) {
      email.setText(client.getUserId());
      FullRefresh(null);
    }
    timeline = new Timeline(new KeyFrame(Duration.minutes(10), event -> {
      try {
        Refresh(null);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }));
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

  public void logout(ActionEvent actionEvent) throws IOException {
    Librerie lib = new Librerie();
    lib.loadLogin(primaryStage);
  }


  public void Refresh(ActionEvent actionEvent) throws IOException {
    if (client != null) {
      email.setText(client.getUserId());
      String host = "127.0.0.1";
      int port = 4445;
      if (client.connectToServer(host, port)) {
        System.out.println("Connessione al server riuscita");
        ObservableList<Email> lastsitems = mailListView.getItems();
        Email lastEmail = lastsitems.isEmpty() ? null : lastsitems.getFirst(); // Controlla se la lista è vuota
        ArrayList<Email> receivedEmails = client.receiveEmail(host, port, client.getUserId(), lastEmail != null ? lastEmail.getDatesendMail() : null);
        ObservableList<Email> items = FXCollections.observableArrayList(receivedEmails);
        items.sort((o1, o2) -> o2.getDatesendMail().compareTo(o1.getDatesendMail()));
        if (!items.isEmpty()) {
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



  public void FullRefresh(ActionEvent actionEvent) throws IOException {
    String host= "127.0.0.1";
    int port= 4445;
    if(client.connectToServer(host, port)){
      ArrayList<Email> receivedEmails = client.receiveEmail(host, port, client.getUserId(), null);
      writeEmails(receivedEmails);
      ObservableList<Email> items = FXCollections.observableArrayList(receivedEmails);
      items.sort((o1, o2) -> o2.getDatesendMail().compareTo(o1.getDatesendMail()));
      mailListView.getItems().clear();
      mailListView.setItems(items);
      indexlenght.setText(String.valueOf(items.size()));
      mailListView.setCellFactory(param -> new MailItemCell(primaryStage));
      System.out.println("Email ricevute");
    } else {
      System.out.println("Connessione al server non riuscita");
    }
  }

}
