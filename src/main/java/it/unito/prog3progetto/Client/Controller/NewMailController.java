package it.unito.prog3progetto.Client.Controller;
import it.unito.prog3progetto.Client.Client;
import it.unito.prog3progetto.Model.Email;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

import static it.unito.prog3progetto.Model.Lib.alert;

public class NewMailController {
  @FXML
  public TextField subjectfield;
  @FXML
  public TextField destinationsfield;
  @FXML
  public TextArea ContentField;
  @FXML
  public Button sendmailbutton;
  private Client clientModel;


  public void initialize( Client clientModel) throws IOException {
  this.clientModel = clientModel;
    destinationsfield.setEditable(true);
      sendmailbutton.setOnAction(event -> {
        try {
          sendMail();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });

  }

public void initialize(String action, String sender, ArrayList<String> Destination, String subject, String content, String date, Client clientModel) {
    EventHandler<ActionEvent> handler = event -> {
        try {
            sendMail();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };
    this.clientModel = clientModel;
    String prefix = action.equals("reply") ? "Re: " : action.equals("forward") ? "Fwd: " : "ReALL: ";
  String destinations;

  if (action.equals("replyall")) {
    if (Destination.size() > 1) {
      // Se ci sono più di un destinatario, li concateniamo con il mittente
      StringBuilder stringBuilder = new StringBuilder();
      for (String destination : Destination) {
        stringBuilder.append(destination).append(",");
      }
      stringBuilder.append(sender);
      destinations = stringBuilder.toString();
    } else if (Destination.size() == 1) {
      // Se c'è solo un destinatario, concateniamo solo quel destinatario e il mittente
      destinations = Destination.getFirst() + "," + sender;
      destinationsfield.setText(destinations);
      destinationsfield.setEditable(false);

    } else {
      // Se non ci sono destinatari, usiamo solo il mittente
      destinations = sender;
      destinationsfield.setText(destinations);
      destinationsfield.setEditable(false);

    }
  } else if(!action.equals("forward")) {

    destinations = sender;
    destinationsfield.setText(destinations);
    destinationsfield.setEditable(false);


  }
    subjectfield.setText(prefix + subject);
    ContentField.setText("\n++++++++++++++++++++++++++++\n Data invia mail " + date + " da" + sender + "\n" + content);
    sendmailbutton.setOnAction(handler);
}

  public void sendMail() throws IOException {
    if(this.clientModel == null){
     alert("Errore durante l'invio dell'email", Alert.AlertType.ERROR);
      System.out.println("Errore durante l'invio dell'email il Client e null");
      return;
    }
    System.out.println("Prova di invio email");

    String destination = destinationsfield.getText();
    String subject = subjectfield.getText();
    String content = ContentField.getText();

    if (destination.isEmpty()) {
      alert("Inserire il destinatario o i destinatari", Alert.AlertType.ERROR);
      return;
    }

    if (subject.isEmpty()) {
      if (!confirmDialog("Il campo oggetto è vuoto, vuoi continuare?")) {
        return;
      }
    }

    String[] destinationsArray = destination.split(",");
    Set<String> uniqueDestinations = new HashSet<>(Arrays.asList(destinationsArray));

    if (uniqueDestinations.size() < destinationsArray.length) {
      alert("I destinatari devono essere tutti diversi", Alert.AlertType.ERROR);
      System.out.println("I destinatari devono essere tutti diversi");
      return;
    }

    String emailPattern = "^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    boolean success = uniqueDestinations.stream().allMatch(dest -> dest.matches(emailPattern));

    if (success) {

      Email email = new Email(clientModel.getUserMail(), new ArrayList<>(uniqueDestinations), subject, content, Date.from(java.time.Instant.now()));

      String host= "127.0.0.1";
      int port= 4445;

        for(String dest: uniqueDestinations){
          if(this.clientModel.connectToServer(host, port)) System.out.println("Connessione al server riuscita");
          if(this.clientModel.CheckEmail(dest)) System.out.println("Email esistente");
          else{
            alert("Email non esistente", Alert.AlertType.ERROR);
            System.out.println("Email non esistente: "+ dest);
            return;
          }
        }
      if (this.clientModel.connectToServer(host, port)) {
        System.out.println("Connessione al server riuscita");
        if (this.clientModel.SendMail( email)) {
          Stage stage = (Stage) subjectfield.getScene().getWindow();
          stage.close();
          alert("Email inviata", Alert.AlertType.INFORMATION);
          System.out.println("Email inviata");
        } else {
          System.out.println("Errore durante l'invio dell'email");
          alert("Errore durante l'invio dell'email", Alert.AlertType.ERROR);
        }
      } else {
        System.out.println("Connessione al server non riuscita");
        alert("Connessione al server non riuscita", Alert.AlertType.ERROR);
      }
    } else {
      alert("Email non inviata, controllare i destinatari", Alert.AlertType.ERROR);
      System.out.println("Email non inviata, controllare i destinatari");
    }
  }

  public boolean confirmDialog(String message) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("Conferma invio email");
    alert.setHeaderText("Conferma invio email");
    alert.setContentText(message);

    Optional<ButtonType> result = alert.showAndWait();
    return result.isPresent() && result.get() == ButtonType.OK;
  }



}