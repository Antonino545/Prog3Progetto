package it.unito.prog3progetto.Client;

import it.unito.prog3progetto.Lib.Email;
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
import java.util.Objects;

public class ClientController {
  public Label email;
  @FXML
  private ListView<Email> mailListView;
  private Stage primaryStage;
  private Client client;

  public void initialize(Client client) {
    this.client = client;
    if(client != null) {
      email.setText(client.getUserId());
      String host= "127.0.0.1";
      int port= 4445;
      if(client.connectToServer(host, port)){
        System.out.println("Connessione al server riuscita");

      ObservableList<Email> items = FXCollections.observableArrayList(client.receiveEmail( host, port, client.getUserId()));
      mailListView.setItems(items);
      System.out.println("Email ricevute");
      }else {
        System.out.println("Connessione al server non riuscita");
      }
    }


    // Creazione della lista di oggetti MailItem
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




}
