package it.unito.prog3progetto.Client;

import it.unito.prog3progetto.Lib.Email;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;



public class ClientController implements MailListObserver {
  public Label email;
  @FXML
  public Label indexLengthLabel;
  public HBox inbox;
  @FXML
  private ListView<Email> mailListView;
  public HBox sendemail;

  private Stage primaryStage;
  private Client client;
  private MailListModel mailListModel;
  private final String host = "127.0.0.1";
  private final int port = 4445;

  public void initialize(Client client) throws IOException {
    this.client = client;
    if (client != null) {
      email.setText(client.getUserId());
      mailListModel = new MailListModel();
      mailListModel.addObserver(this); // Registra il controller come osservatore
      mailListView.setItems(mailListModel.getEmails());
      FullRefresh();
    }
  }

  // Implementazione del metodo dell'interfaccia MailListObserver per gestire l'aggiunta di email
  @Override
  public void onEmailAdded(Email email) {
    mailListView.getItems().add(email); // Aggiorna la ListView aggiungendo l'email
    if (indexLengthLabel != null) {
      indexLengthLabel.setText(String.valueOf(mailListView.getItems().size())); // Aggiorna la lunghezza dell'indice
    }
  }

  // Implementazione del metodo dell'interfaccia MailListObserver per gestire la rimozione di email
  @Override
  public void onEmailRemoved(Email email) {
    mailListView.getItems().remove(email); // Rimuovi l'email dalla ListView
    if (indexLengthLabel != null) {
      indexLengthLabel.setText(String.valueOf(mailListView.getItems().size())); // Aggiorna la lunghezza dell'indice
    }
  }

  public void setPrimaryStage(Stage primaryStage) {
    this.primaryStage = primaryStage;
  }

  public void NewEmail(ActionEvent actionEvent) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("Newemail.fxml"));
      Parent root = loader.load();
      NewMailController controller = loader.getController();
      controller.initialize("sendmail",client);
      Stage stage = new Stage();
      stage.setScene(new Scene(root));
      stage.setTitle("Dettagli Email");
      stage.setResizable(false);
      stage.show();
    } catch (IOException e) {
      e.printStackTrace();
      // Gestisci l'eccezione in modo appropriato
    }
  }

  public void logout(ActionEvent actionEvent) throws IOException {
    Librerie lib = new Librerie();
    client = null;
    lib.loadLogin(primaryStage);
  }

  public void Refresh() throws IOException {
    if (client != null && client.connectToServer(host, port)) {
      Email lastEmail = mailListModel.getEmails().isEmpty() ? null : mailListModel.getEmails().getFirst();
      if(lastEmail == null) {
        FullRefresh();
        return;
      }
      mailListModel.addEmails(client.receiveEmail(host, port, client.getUserId(), lastEmail.getDatesendMail()));
      System.out.println("Email ricevute");
    } else {
      System.out.println("Connessione al server non riuscita");
    }
  }

  public void FullRefresh() throws IOException {
    String host= "127.0.0.1";
    int port= 4445;
    if(client.connectToServer(host, port)){
      ArrayList<Email> receivedEmails = client.receiveEmail(host, port, client.getUserId(), null);
      ObservableList<Email> items = FXCollections.observableArrayList(receivedEmails);
      items.sort((o1, o2) -> o2.getDatesendMail().compareTo(o1.getDatesendMail()));

      // Pulisce la ListView e imposta i nuovi elementi
      mailListView.getItems().clear();
      mailListView.setItems(items);

      // Aggiorna la lunghezza dell'indice
      if(indexLengthLabel != null)
      indexLengthLabel.setText(String.valueOf(items.size()));
      mailListView.refresh(); // Aggiorna la visualizzazione nella ListView
      mailListView.setCellFactory(param -> new MailItemCell(primaryStage, this,client));

      System.out.println("Email ricevute");
    } else {
      System.out.println("Connessione al server non riuscita");
    }
  }


  public void deleteEmail(Email email) {
    System.out.println("Prova di eliminazione email");
    if (client.connectToServer(host, port)) {
      if (client.DeleteMail(host, port, email)) {
        mailListModel.removeEmail(email); // Rimuovi l'email dalla lista
        mailListView.refresh(); // Aggiorna la visualizzazione nella ListView
        Librerie.alert("Email eliminata", Alert.AlertType.INFORMATION);
      } else {
        Librerie.alert("Errore durante l'eliminazione dell'email", Alert.AlertType.ERROR);
      }
    } else {
      Librerie.alert("Connessione al server non riuscita", Alert.AlertType.ERROR);
    }
  }

  @FXML
  public void sendemails() {
    // Qui inserisci il codice per gestire l'invio delle email

    // Dopo aver gestito l'evento, modifica lo stile della HBox
    sendemail.getStyleClass().remove("not-selectable");
    sendemail.getStyleClass().add("selectable");
    inbox.getStyleClass().remove("selectable");
    inbox.getStyleClass().add("not-selectable");

  }

  public void inboxemail(MouseEvent mouseEvent) {
    inbox.getStyleClass().remove("not-selectable");
    inbox.getStyleClass().add("selectable");
    sendemail.getStyleClass().remove("selectable");
    sendemail.getStyleClass().add("not-selectable");
  }
}
