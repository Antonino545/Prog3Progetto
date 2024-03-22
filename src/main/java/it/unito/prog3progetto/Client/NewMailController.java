package it.unito.prog3progetto.Client;

import it.unito.prog3progetto.Lib.Mail;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.*;

public class NewMailController {
  @FXML
  public TextField subjectfield;
  @FXML
  public TextField destinationsfield;
  @FXML
  public TextArea ContentField;
  private String usermail;


  public void setUser(String usermail) {
    this.usermail =  usermail;
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
    String emailPattern = "^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    for (String dest : destinationsList) {
      if (!dest.matches(emailPattern)) {
        success = false;
        break;
      }
    }
    if (success) {
    Mail mail = new Mail(usermail, new ArrayList<>(destinationsList), subjectfield.getText(), ContentField.getText(), Date.from(java.time.Instant.now()));
    System.out.println(mail);
    Stage stage = (Stage) subjectfield.getScene().getWindow();
    stage.close();
      alert("Email inviata", Alert.AlertType.INFORMATION);
      System.out.println("Email inviata");
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