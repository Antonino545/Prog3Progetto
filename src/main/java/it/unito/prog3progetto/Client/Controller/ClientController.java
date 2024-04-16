package it.unito.prog3progetto.Client.Controller;

import it.unito.prog3progetto.Client.ClientModel;
import it.unito.prog3progetto.Client.MailItemCell;
import it.unito.prog3progetto.Model.Email;
import it.unito.prog3progetto.Model.MailListModel;
import it.unito.prog3progetto.Model.MailListObserver;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static it.unito.prog3progetto.Model.Lib.alert;


public class ClientController implements MailListObserver {
  @FXML
  public Label indexLengthLabel,email, SendMailLengthLabel;
  public HBox inbox;
  public ProgressIndicator spinner;
  @FXML
  private ListView<Email> mailListView;
  public HBox sendemail;

  public boolean isInbox ;
  private Stage primaryStage;
  private ClientModel Client;
  private MailListModel mailReceivedListModel;
  MailListModel mailSendListModel;

  private Timeline autoRefreshTimeline;

  public void initialize(ClientModel clientModel) throws IOException {
    this.Client = clientModel;
    if (clientModel != null) {
      email.setText(clientModel.getEMail());
      mailReceivedListModel = new MailListModel();
      mailSendListModel=new MailListModel();
      mailReceivedListModel.addObserver(this); // Registra il controller come osservatore
      mailSendListModel.addObserver(this);
      indexLengthLabel.textProperty().bind(mailReceivedListModel.sizeProperty().asString());
      SendMailLengthLabel.textProperty().bind(mailSendListModel.sizeProperty().asString());
      FullRefresh();
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
    mailListView.setCellFactory(param -> new MailItemCell( this, Client));
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
      controller.initialize(Client,this); // Durante l'inizializzazione del controller, passa il riferimento al client
      Stage stage = new Stage();
      stage.setScene(new Scene(root));
      stage.setTitle("Nuova Email");
      stage.setMinHeight(400);
      stage.setMinWidth(600);
      stage.show();
    } catch (IOException e) {
    alert("Errore durante l'apertura della finestra per la creazione di una nuova email", Alert.AlertType.ERROR);
    }
  }
/*
  Esegue il logout dell'utente e torna alla schermata di login
 */
  public void logout() {
    spinner.setVisible(true);

    new Thread(() -> {
    if(Client.connectToServer())
      if(Client.logout()){
      Platform.runLater(() -> {
        spinner.setVisible(false);
        if(autoRefreshTimeline.getStatus().equals(Timeline.Status.RUNNING)) autoRefreshTimeline.stop();

        alert("Logout effettuato con successo", Alert.AlertType.INFORMATION);
        loadLogin(primaryStage);// Carica la schermata di login
      });
      }

    else {
      Platform.runLater(() -> alert("Errore durante il logout", Alert.AlertType.ERROR));
    }
    else {
    Platform.runLater(() -> alert("Connessione al server non riuscita", Alert.AlertType.ERROR));
    }

  }).start();

  }

  public void loadLogin(Stage primaryStage) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unito/prog3progetto/Client/Login.fxml"));
      Parent root = loader.load();
      LoginController controller = loader.getController();
      controller.setPrimaryStage(primaryStage);
      Scene scene = new Scene(root);
      scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/it/unito/prog3progetto/Client/style.css")).toExternalForm());
      primaryStage.setScene(scene);
      primaryStage.setTitle("Email Client - Progetto di Programmazione 3");
      primaryStage.show();
    } catch (IOException e) {
      alert("Errore durante il caricamento della schermata di login", Alert.AlertType.ERROR);
    }
  }



  /**
   * Metodo per aggiornare la lista delle email ricevute o inviate
   */
  public void Refresh() {
    // Mostra lo spinner
    spinner.setVisible(true);

    new Thread(() -> {
      boolean connectionSuccessful = false;

      if (Client != null && Client.connectToServer()) {
        connectionSuccessful = true;

        if (isInbox) {
          Email lastEmail = mailReceivedListModel.getEmails().isEmpty() ? null : mailReceivedListModel.getEmails().getLast();
          if (lastEmail == null) {
            FullRefresh();
            return;
          }
          Platform.runLater(() -> mailReceivedListModel.addEmails(Client.receiveEmail(Client.getEMail(), lastEmail.getDatesendMail(), false)));

        } else {
          Email lastEmail = mailSendListModel.getEmails().isEmpty() ? null : mailSendListModel.getEmails().getLast();
          if (lastEmail == null) {
            FullRefresh();
            return;
          }
          Platform.runLater(() ->   mailSendListModel.addEmails(Client.receiveEmail(Client.getEMail(), lastEmail.getDatesendMail(), true)));
        }
      }

      // Nasconde lo spinner
      Platform.runLater(() -> spinner.setVisible(false));

      if (!connectionSuccessful) {
        autoRefreshTimeline.setDelay(autoRefreshTimeline.getCurrentTime().add(autoRefreshTimeline.getCurrentTime()));

        Platform.runLater(() -> alert("Connessione al server non riuscita", Alert.AlertType.ERROR));
      }
    }).start();
  }




  public void FullRefresh() {
    if (Client != null && Client.connectToServer()) {
      if (isInbox) {
        mailReceivedListModel.clear();
        mailReceivedListModel.addEmails(Client.receiveEmail(Client.getEMail(), null, false));
      } else {
        mailSendListModel.clear();
        mailSendListModel.addEmails(Client.receiveEmail(Client.getEMail(), null, true));
      }
      Platform.runLater(() -> spinner.setVisible(false));

    }
  }


  public void deleteEmail(Email email) {
    if (Client.connectToServer()) {
      if (Client.DeleteMail(email,isInbox)) {
        System.out.println(isInbox);
        if(isInbox) {
          mailReceivedListModel.removeEmail(email); // Rimuovi l'email dalla lista
        } else{

          mailSendListModel.removeEmail(email);

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

    if (!isInbox)
      return;
    isInbox = false;
    changeButton(sendemail, inbox);
    if (mailSendListModel.getEmails().isEmpty()) {
      FullRefresh();
      mailListView.getItems().setAll(mailSendListModel.getEmails());
    } else {
      mailListView.setItems(mailSendListModel.getEmails());
      mailListView.refresh();
    }

  }


  @FXML
  public void inboxemail() {
    if(isInbox)
      return;
    isInbox = true;
    changeButton(inbox, sendemail);
    if (mailReceivedListModel.getEmails().isEmpty()) {
      FullRefresh();
      mailListView.getItems().setAll(mailReceivedListModel.getEmails());
    } else {
      mailListView.setItems(mailReceivedListModel.getEmails());
      mailListView.refresh();
    }
  }

  public void changeButton(HBox first, HBox second){
    first.getStyleClass().remove("not-selectable");
    first.getStyleClass().add("selectable");
    second.getStyleClass().remove("selectable");
    second.getStyleClass().add("not-selectable");
  }
}
