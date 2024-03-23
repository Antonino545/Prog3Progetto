package it.unito.prog3progetto.Client;

import it.unito.prog3progetto.Lib.Email;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.*;

public class NewMailController {
  @FXML
  public TextField subjectfield;
  @FXML
  public TextField destinationsfield;
  @FXML
  public TextArea ContentField;
  @FXML
  public Button sendmailbutton;
  private String usermail;


  public void setUser(String usermail) {
    this.usermail =  usermail;
  }
  public void initialize(String action){
 if(action.equals("sendmail")){
    destinationsfield.setEditable(true);
      sendmailbutton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
          sendMail(event);
        }
      });
  }
  }
  public void initialize(String action, String sender,String subject, String content, String date){
    if(action.equals("reply")){
      destinationsfield.setText(sender);
      destinationsfield.setEditable(false);
      subjectfield.setText("Re: "+subject);
      ContentField.setText("\n++++++++++++++++++++++++++++\n Data invia mail " +date +" da"+sender  +"\n"+content);
      sendmailbutton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
          sendMail(event);
        }
      });
    }
  }

  public void sendMail(ActionEvent actionEvent) {

    System.out.println("Prova di invio email");
    if(destinationsfield.getText().isEmpty() ){
      alert("Inserire il destinatario o i destinatari", Alert.AlertType.ERROR);
      return;
    }
    if(subjectfield.getText().isEmpty()) {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle("Conferma invio email");
      alert.setHeaderText("Conferma invio email");
      alert.setContentText("Il campo oggetto è vuoto, vuoi continuare?");

      Optional<ButtonType> result = alert.showAndWait();
      if (((Optional<?>) result).isPresent() && result.get() == ButtonType.OK) {
        // Se l'utente conferma, vai avanti con l'operazione
      } else {
        // Se l'utente non conferma, interrompi il flusso del programma
        return; // o qualsiasi altra azione necessaria per gestire la non conferma
      }
    }

    String destination = destinationsfield.getText();
    String[] destinationsArray = destination.split(",");
    boolean success = true;
    List<String> destinationsList = Arrays.asList(destinationsArray);
    Set<String> uniqueDestinations = new HashSet<>(destinationsList);
    if (uniqueDestinations.size() < destinationsList.size()) {
      alert("I destinatari devono essere tutti diversi", Alert.AlertType.ERROR);
      System.out.println("I destinatari devono essere tutti diversi");
      return;
    }
    String emailPattern = "^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    for (String dest : destinationsList) {
      if (!dest.matches(emailPattern)) {
        success = false;
        break;
      }
    }
    if (success) {
      String Content = ContentField.getText();
      Content=Content.replace("\n", "<--Accapo-->");
    Email email = new Email(usermail, new ArrayList<>(destinationsList), subjectfield.getText(),Content, Date.from(java.time.Instant.now()));
    Client c = new Client(usermail);
      String host= "127.0.0.1";
      int port= 4445;
      if(c.connectToServer(host, port)){
        System.out.println("Connessione al server riuscita");
      if(c.SendMail(host, port, email)){
        System.out.println(email);
        Stage stage = (Stage) subjectfield.getScene().getWindow();
        stage.close();
        alert("Email inviata", Alert.AlertType.INFORMATION);
        System.out.println("Email inviata");
      }else{
        System.out.println("Errore durante l'invio dell'email");
        alert("Errore durante l'invio dell'email", Alert.AlertType.ERROR);
        return;
      }
      }else{
        System.out.println("Connessione al server non riuscita");
        alert("Connessione al server non riuscita", Alert.AlertType.ERROR);
        return;
      }

    }else{
      alert("Email non inviata, controllare i destinatari", Alert.AlertType.ERROR);
      System.out.println("Email non inviata, controllare i destinatari");
    }

  }
  public void alert(String message, Alert.AlertType type){
    Alert alert = new Alert(type);
    alert.setTitle("Avviso");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();

  }
}