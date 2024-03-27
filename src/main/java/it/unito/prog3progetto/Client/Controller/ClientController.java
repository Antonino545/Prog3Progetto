package it.unito.prog3progetto.Client.Controller;

import it.unito.prog3progetto.Client.*;
import it.unito.prog3progetto.Model.Email;
import it.unito.prog3progetto.Model.MailListModel;
import it.unito.prog3progetto.Model.MailListObserver;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;


public class ClientController implements MailListObserver {
  public Label email;
  @FXML
  public Label indexLengthLabel;
  public HBox inbox;
  public Label sendmaillabel;
  @FXML
  private ListView<Email> mailListView;
  public HBox sendemail;

  private Stage primaryStage;
  private Client client;
  private MailListModel mailReceivedListModel;
  private MailListModel mailSendListModel;
  private ArrayList<Email> previousSentEmails = new ArrayList<>();
  private ArrayList<Email> previousReceivedEmails = new ArrayList<>();

  private final String host = "127.0.0.1";
  private final int port = 4445;

  public void initialize(Client client) throws IOException {
    this.client = client;
    if (client != null) {
      email.setText(client.getUserId());
      mailReceivedListModel = new MailListModel();
      mailSendListModel=new MailListModel();
      mailReceivedListModel.addObserver(this); // Registra il controller come osservatore
      mailSendListModel.addObserver(this);
      indexLengthLabel.setText(String.valueOf(mailReceivedListModel.getEmails().size())); // Aggiorna la lunghezza dell'indice
      sendmaillabel.setText(String.valueOf(mailSendListModel.getEmails().size())); // Aggiorna la lunghezza dell'indice
      sendemails();
      inboxemail();
    }
  }

  // Implementazione del metodo dell'interfaccia MailListObserver per gestire l'aggiunta di email
  @Override
  public void onEmailAdded(Email email) {
    mailListView.getItems().add(email); // Aggiorna la ListView aggiungendo l'email
    mailListView.setCellFactory(param -> new MailItemCell(primaryStage, this,client));
     indexLengthLabel.setText(String.valueOf(mailReceivedListModel.size()));
      sendmaillabel.setText(String.valueOf(mailSendListModel.size()));


  }

  // Implementazione del metodo dell'interfaccia MailListObserver per gestire la rimozione di email
  @Override
  public void onEmailRemoved(Email email) {
    mailListView.getItems().remove(email); // Rimuovi l'email dalla ListView
      indexLengthLabel.setText(String.valueOf(mailReceivedListModel.size()));
      sendmaillabel.setText(String.valueOf(mailSendListModel.size()));
  }

  @Override
  public void onAllEmailsRemoved() {
    mailListView.getItems().clear(); // Rimuovi tutti gli email dalla ListView
    if (indexLengthLabel != null && sendmaillabel != null) {
      indexLengthLabel.setText("0"); // Imposta la lunghezza dell'indice a 0
      sendmaillabel.setText("0"); // Imposta la lunghezza dell'indice delle email inviate a 0
    }
  }


  public void setPrimaryStage(Stage primaryStage) {
    this.primaryStage = primaryStage;
  }

  public void NewEmail(ActionEvent actionEvent) {
    try {
      FXMLLoader loader = new FXMLLoader(new File("src/main/resources/it/unito/prog3progetto/Client/Newemail.fxml").toURI().toURL());
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
    loadLogin(primaryStage);
  }
  public  void loadLogin(Stage primaryStage) throws IOException {
    FXMLLoader loader = new FXMLLoader(new File("src/main/resources/it/unito/prog3progetto/Client/Login.fxml").toURI().toURL());
    Parent root = loader.load();
    LoginController controller = loader.getController();
    controller.setPrimaryStage(primaryStage);
    Scene scene = new Scene(root);
    scene.getStylesheets().add(new File("src/main/resources/it/unito/prog3progetto/Client/style.css").toURI().toURL().toExternalForm());
    primaryStage.setScene(scene);
    primaryStage.setTitle("Email Client - Progetto di Programmazione 3");
    primaryStage.show();
  }
  public void Refresh() throws IOException {
    if (client != null && client.connectToServer(host, port)) {
      Email lastEmail = mailReceivedListModel.getEmails().isEmpty() ? null : mailReceivedListModel.getEmails().getFirst();
      if(lastEmail == null) {
        FullRefresh();
        return;
      }
      mailReceivedListModel.addEmails(client.receiveEmail(host, port, client.getUserId(), lastEmail.getDatesendMail()));
      System.out.println("Email ricevute");
    } else {
      System.out.println("Connessione al server non riuscita");
    }
  }

  public void FullRefresh()  {

    if(client.connectToServer(host, port)){
      mailReceivedListModel.clear();
      mailReceivedListModel.addEmails(client.receiveEmail(host, port, client.getUserId(), null));

      System.out.println("Email ricevute");
    } else {
      System.out.println("Connessione al server non riuscita");
    }
  }


  public void deleteEmail(Email email) {
    System.out.println("Prova di eliminazione email");
    if (client.connectToServer(host, port)) {
      if (client.DeleteMail(host, port, email)) {
        mailReceivedListModel.removeEmail(email); // Rimuovi l'email dalla lista
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
    sendemail.getStyleClass().remove("not-selectable");
    sendemail.getStyleClass().add("selectable");
    inbox.getStyleClass().remove("selectable");
    inbox.getStyleClass().add("not-selectable");
    if (!previousSentEmails.isEmpty()) {
      mailListView.getItems().setAll(previousSentEmails);
      return;
    }

    if (client.connectToServer(host, port)) {
      mailSendListModel.clear();
      mailSendListModel.addEmails(client.receivesendEmail(host, port, client.getUserId(), null));
      previousSentEmails.addAll(mailSendListModel.getEmails());
      System.out.println("Email ricevute");
    } else {
      System.out.println("Connessione al server non riuscita");
    }


  }


  public void inboxemail() {
    inbox.getStyleClass().remove("not-selectable");
    inbox.getStyleClass().add("selectable");
    sendemail.getStyleClass().remove("selectable");
    sendemail.getStyleClass().add("not-selectable");
    // Se ci sono email ricevute precedentemente, non richiederle nuovamente al server
    if (!previousReceivedEmails.isEmpty()) {
      mailListView.getItems().setAll(previousReceivedEmails);
      return;
    }

    // Se non ci sono email ricevute precedentemente, richiedile al server e memorizzale
    FullRefresh();

    // Memorizza le email ricevute precedentemente
    previousReceivedEmails.clear();
    previousReceivedEmails.addAll(mailReceivedListModel.getEmails());

    // Modifica lo stile della HBox

  }

}
