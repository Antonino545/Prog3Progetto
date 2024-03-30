package it.unito.prog3progetto.Client.Controller;

import it.unito.prog3progetto.Client.ClientModel;
import it.unito.prog3progetto.Client.MailItemCell;
import it.unito.prog3progetto.Model.Email;
import it.unito.prog3progetto.Model.MailListModel;
import it.unito.prog3progetto.Model.MailListObserver;
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
  private ClientModel clientModel;
  private MailListModel mailReceivedListModel,mailSendListModel;
  private ArrayList<Email> previousSentEmails = new ArrayList<>();
  private ArrayList<Email> previousReceivedEmails = new ArrayList<>();

  private final String host = "127.0.0.1";
  private final int port = 4445;


  public void initialize(ClientModel clientModel) throws IOException {
    this.clientModel = clientModel;
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
  }

  // Implementazione del metodo dell'interfaccia MailListObserver per gestire l'aggiunta di email
  @Override
  public void onEmailAdded(Email email) {
    mailListView.getItems().add(email); // Aggiorna la ListView aggiungendo l'email
    mailListView.setCellFactory(param -> new MailItemCell(primaryStage, this, clientModel));
  }

  // Implementazione del metodo dell'interfaccia MailListObserver per gestire la rimozione di email
  @Override
  public void onEmailRemoved(Email email) {
    mailListView.getItems().remove(email); // Rimuovi l'email dalla ListView
  }

  @Override
  public void onAllEmailsRemoved() {
    mailListView.getItems().clear(); // Rimuovi tutti gli email dalla ListView

  }


  public void setPrimaryStage(Stage primaryStage) {
    this.primaryStage = primaryStage;
  }

  public void NewEmail() {
    try {
      FXMLLoader loader = new FXMLLoader(new File("src/main/resources/it/unito/prog3progetto/Client/Newemail.fxml").toURI().toURL());
      Parent root = loader.load();
      NewMailController controller = loader.getController();
      controller.initialize("sendmail", clientModel);
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

  public void logout() throws IOException {
    if(clientModel.connectToServer(host, port))
    clientModel.logout();
    else {
      alert("Connessione al server non riuscita", Alert.AlertType.ERROR);
    }
    loadLogin(primaryStage);
    clientModel.closeConnections();
  }
  public  void loadLogin(Stage primaryStage) throws IOException {
    FXMLLoader loader = new FXMLLoader(new File("src/main/resources/it/unito/prog3progetto/Client/Login.fxml").toURI().toURL());
    Parent root = loader.load();
    LoginController controller = loader.getController();
    controller.setPrimaryStage(primaryStage);
    Scene scene = new Scene(root);
    scene.getStylesheets().add(new File("src/main/resources/it/unito/prog3progetto/Client/style.css").toURI().toURL().toExternalForm());
    primaryStage.setScene(scene);
    primaryStage.setTitle("Email ClientModel - Progetto di Programmazione 3");
    primaryStage.show();
  }
  public void Refresh() {
    if (clientModel != null && clientModel.connectToServer(host, port) ) {
      if(isInbox){
      Email lastEmail = mailReceivedListModel.getEmails().isEmpty() ? null : mailReceivedListModel.getEmails().getLast();
      if(lastEmail == null) {
        FullRefresh();
        return;
      }
      mailReceivedListModel.addEmails(clientModel.receiveEmail( clientModel.getUserMail(), lastEmail.getDatesendMail(),false));

      }
      else {
        Email lastEmail = mailSendListModel.getEmails().isEmpty() ? null : mailSendListModel.getEmails().getLast();
        if(lastEmail == null) {
          FullRefresh();
          return;
        }
        mailSendListModel.addEmails(clientModel.receiveEmail( clientModel.getUserMail(), lastEmail.getDatesendMail(),true));}
    } else {
      alert("Connessione al server non riuscita", Alert.AlertType.ERROR);
    }
  }

  public void FullRefresh()  {
    if(clientModel.connectToServer(host, port)){
      if(isInbox){
        mailReceivedListModel.clear();
        mailReceivedListModel.addEmails(clientModel.receiveEmail(clientModel.getUserMail(), null,false));
      }
      else {
        mailSendListModel.clear();
        mailSendListModel.addEmails(clientModel.receiveEmail(clientModel.getUserMail(), null,true));
      }
      System.out.println("Email ricevute");
    } else {
      alert("Connessione al server non riuscita", Alert.AlertType.ERROR);
    }
  }


  public void deleteEmail(Email email) {
    System.out.println(isInbox);
    System.out.println("Email eliminata:");
    System.out.println(email);
    if (clientModel.connectToServer(host, port)) {
      if (clientModel.DeleteMail(email,isInbox)) {
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
      } else {
        alert("Errore durante l'eliminazione dell'email", Alert.AlertType.ERROR);
      }
    } else {
      alert("Connessione al server non riuscita", Alert.AlertType.ERROR);
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
    if (clientModel.connectToServer(host, port)) {
      mailSendListModel.clear();
      mailSendListModel.addEmails(clientModel.receiveEmail(clientModel.getUserMail(), null,true));
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

    if (clientModel.connectToServer(host, port)) {
      mailReceivedListModel.clear();
      mailReceivedListModel.addEmails(clientModel.receiveEmail(clientModel.getUserMail(), null,false));
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
