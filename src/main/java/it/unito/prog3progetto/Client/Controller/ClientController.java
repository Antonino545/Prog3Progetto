package it.unito.prog3progetto.Client.Controller;

import it.unito.prog3progetto.Client.Client;
import it.unito.prog3progetto.Client.MailItemCell;
import it.unito.prog3progetto.Model.Email;
import it.unito.prog3progetto.Model.MailListModel;
import it.unito.prog3progetto.Model.MailListObserver;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static it.unito.prog3progetto.Model.Lib.alert;


public class ClientController implements MailListObserver {
  @FXML
  public Label indexLengthLabel,email,sendmaillabel;
  public HBox inbox;
  @FXML
  private ListView<Email> mailListView;
  public HBox sendemail;

  public boolean isInbox ;
  private Stage primaryStage;
  private Client Client;
  private MailListModel mailReceivedListModel,mailSendListModel;
  private ArrayList<Email> previousSentEmails = new ArrayList<>();
  private ArrayList<Email> previousReceivedEmails = new ArrayList<>();

  private final String host = "127.0.0.1";
  private final int port = 4445;

  private Timeline autoRefreshTimeline;

  public void initialize(Client clientModel) throws IOException {
    this.Client = clientModel;
    if (clientModel != null) {
      email.setText(clientModel.getUserMail());
      mailReceivedListModel = new MailListModel();
      mailSendListModel=new MailListModel();
      mailReceivedListModel.addObserver(this); // Registra il controller come osservatore
      mailSendListModel.addObserver(this);
      indexLengthLabel.textProperty().bind(mailReceivedListModel.sizeProperty().asString());
      sendmaillabel.textProperty().bind(mailSendListModel.sizeProperty().asString());
      sendemails();
      inboxemail();
    }
    autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(20), event -> Refresh()));
    autoRefreshTimeline.setCycleCount(Timeline.INDEFINITE);
    autoRefreshTimeline.play();
  }

  // Implementazione del metodo dell'interfaccia MailListObserver per gestire l'aggiunta di email
  @Override
  public void onEmailAdded(Email email) {
    // Aggiunge l'email all'inizio della ListView
    mailListView.getItems().addFirst(email);
    mailListView.setCellFactory(param -> new MailItemCell(primaryStage, this, Client));
  }


  // Implementazione del metodo dell'interfaccia MailListObserver per gestire la rimozione di email
  @Override
  public void onEmailRemoved(Email email) {
    mailListView.getItems().remove(email);
  }
  // Implementazione del metodo dell'interfaccia MailListObserver per gestire la rimozione di tutte le email
  @Override
  public void onAllEmailsRemoved() {
    mailListView.getItems().clear();
  }

  // Metodo per impostare il riferimento alla finestra principale
  public void setPrimaryStage(Stage primaryStage) {
    this.primaryStage = primaryStage;
  }

  /**
   * Metodo per aprire la finestra per la creazione di una nuova email
   */
  public void NewEmail() {
    try {
      FXMLLoader loader = new FXMLLoader(new File("src/main/resources/it/unito/prog3progetto/Client/Newemail.fxml").toURI().toURL());
      Parent root = loader.load();
      NewMailController controller = loader.getController();
      controller.initialize(Client); // Durante l'inizializzazione del controller, passa il riferimento al client
      Stage stage = new Stage();
      stage.setScene(new Scene(root));
      stage.setTitle("Nuova Email");
      stage.show();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
/*
  Esegue il logout dell'utente e torna alla schermata di login
 */
  public void logout() throws IOException {
    if(Client.connectToServer(host, port))
      if(Client.logout()){
        alert("Logout effettuato", Alert.AlertType.INFORMATION);
      }
    else {
      System.out.println("Errore durante il logout");
    }
    else {
      alert("Connessione al server non riuscita", Alert.AlertType.ERROR);
    }
    loadLogin(primaryStage);// Carica la schermata di login
    autoRefreshTimeline.stop();// Ferma il refresh automatico
    Client.closeConnections();// Chiude le connessioni con il server
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

  /**
   * Metodo per aggiornare la lista delle email ricevute o inviate
   */
  public void Refresh() {
    if (Client != null && Client.connectToServer(host, port) ) {
      if(isInbox){
      Email lastEmail = mailReceivedListModel.getEmails().isEmpty() ? null : mailReceivedListModel.getEmails().getLast();
      if(lastEmail == null) {
        FullRefresh();
        return;
      }
      mailReceivedListModel.addEmails(Client.receiveEmail( Client.getUserMail(), lastEmail.getDatesendMail(),false));

      }
      else {
        Email lastEmail = mailSendListModel.getEmails().isEmpty() ? null : mailSendListModel.getEmails().getLast();
        if(lastEmail == null) {
          FullRefresh();
          return;
        }
        mailSendListModel.addEmails(Client.receiveEmail( Client.getUserMail(), lastEmail.getDatesendMail(),true));}
    } else {
      alert("Connessione al server non riuscita", Alert.AlertType.ERROR);
    }
  }

  public void FullRefresh()  {
    if(Client.connectToServer(host, port)){
      if(isInbox){
        mailReceivedListModel.clear();
        mailReceivedListModel.addEmails(Client.receiveEmail(Client.getUserMail(), null,false));
      }
      else {
        mailSendListModel.clear();
        mailSendListModel.addEmails(Client.receiveEmail(Client.getUserMail(), null,true));
      }
      System.out.println("Email ricevute");
    } else {
      alert("Connessione al server non riuscita", Alert.AlertType.ERROR);
    }
  }


  public void deleteEmail(Email email) {
    if (Client.connectToServer(host, port)) {
      if (Client.DeleteMail(email,isInbox)) {
        System.out.println(isInbox);
        if(isInbox) {
          mailReceivedListModel.removeEmail(email); // Rimuovi l'email dalla lista
          previousReceivedEmails.remove(email); // Rimuovi l'email dalla lista delle email ricevute precedentemente
        } else{

          mailSendListModel.removeEmail(email);
          previousSentEmails.remove(email);

        }
        mailListView.refresh(); // Aggiorna la visualizzazione nella ListView

        alert("Email eliminata", Alert.AlertType.INFORMATION);
        System.out.println("Email eliminata");
      } else {
        alert("Errore durante l'eliminazione dell'email", Alert.AlertType.ERROR);
        System.out.println("Errore durante l'eliminazione dell'email");
      }
    } else {
      alert("Connessione al server non riuscita", Alert.AlertType.ERROR);
      System.out.println("Connessione al server non riuscita");
    }
  }

  @FXML
  public void sendemails() {
    isInbox = false;
    changeButton(sendemail,inbox);
    if (!previousSentEmails.isEmpty()) {
      mailListView.getItems().setAll(previousSentEmails);
      return;
    }
    if (Client.connectToServer(host, port)) {
      mailSendListModel.clear();
      mailSendListModel.addEmails(Client.receiveEmail(Client.getUserMail(), null,true));
      previousSentEmails.addAll(mailSendListModel.getEmails());

    } else {
      alert("Connessione al server non riuscita", Alert.AlertType.ERROR);
    }


  }


  public void inboxemail() {
    changeButton(inbox,sendemail);
    isInbox = true;
    if (!previousReceivedEmails.isEmpty()) {
      mailListView.getItems().setAll(previousReceivedEmails);
      return;
    }

    if (Client.connectToServer(host, port)) {
      mailReceivedListModel.clear();
      mailReceivedListModel.addEmails(Client.receiveEmail(Client.getUserMail(), null,false));
      previousReceivedEmails.addAll(mailReceivedListModel.getEmails());

    } else {
      alert("Connessione al server non riuscita", Alert.AlertType.ERROR);
    }

  }

  public void changeButton(HBox first, HBox second){
    first.getStyleClass().remove("not-selectable");
    first.getStyleClass().add("selectable");
    second.getStyleClass().remove("selectable");
    second.getStyleClass().add("not-selectable");
  }
}
