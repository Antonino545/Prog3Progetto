package it.unito.prog3progetto.Client;

import it.unito.prog3progetto.Lib.Email;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MailDetailController {
  public Label senderLabel;
  public Label subjectLabel;
  public Label contentLabel;
  public Label destinationslabel;
  ArrayList<String> destinations;
  public Label datalabel;
  UUID id;
  private Stage primaryStage;
  private Client client;

  public void initialize() throws IOException {
    this.client = readUserEmailFromFile();
  }

  public void setMailDetails(String sender, String subject, String content, ArrayList<String> Destinations, String data, UUID id) {
    senderLabel.setText(sender);
    subjectLabel.setText(subject);
    contentLabel.setText( content);
    this.destinations=Destinations;
    destinationslabel.setText(Destinations.toString());
    datalabel.setText(data);
    this.id=id;


  }

  public void setPrimaryStage(Stage primaryStage) {
    this.primaryStage = primaryStage;
  }



  public void handleReply(ActionEvent actionEvent) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("Newemail.fxml"));

      Parent root = loader.load();
      NewMailController controller = loader.getController();
      controller.setUser(senderLabel.getText());
      controller.initialize("reply", senderLabel.getText(), subjectLabel.getText(), contentLabel.getText(), datalabel.getText());
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
  }

  public void handleForward(ActionEvent actionEvent) {
  }

  public void handleDelete(ActionEvent actionEvent) throws IOException {
    String host= "127.0.0.1";
    int port= 4445;
    client.connectToServer(host, port);
    client.DeleteMail(host, port, new Email(senderLabel.getText(), destinations, null, null,null,id));
    loader(client);

  }

  public void indietro(ActionEvent actionEvent) {
   loader(client);
  }
  public void loader(Client client) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("Client.fxml"));
      Parent root = loader.load();
      ClientController controller = loader.getController();
      controller.setPrimaryStage(primaryStage);
      controller.initialize(client);
      Scene scene = new Scene(root);
      scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
      primaryStage.setScene(scene);
      primaryStage.setTitle("Client");
      primaryStage.setResizable(true);
      primaryStage.setMinWidth(300); // Imposta la larghezza minima della finestra
      primaryStage.setMinHeight(400); // Imposta l'altezza minima della finestra
      primaryStage.show(); // Mostra la finestra
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  private static Client readUserEmailFromFile() throws IOException {
    String fileName = "user_email.txt";
    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
      System.out.println("Contenuto del file " + fileName + ":");
      String line;
      while ((line = reader.readLine()) != null) {
        return new Client(line);
      }
    }
    return null;
  }

}
