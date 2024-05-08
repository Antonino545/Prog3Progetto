package it.unito.prog3progetto.Client.Controller;
import it.unito.prog3progetto.Client.ClientModel;
import it.unito.prog3progetto.Model.Email;
import javafx.application.Platform;
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
  public ProgressIndicator spinner;

  @FXML
  public TextArea ContentField;
  @FXML
  public Button sendmailbutton;
  private ClientModel clientModel;


  public void initialize( ClientModel clientModel,ClientController clientController) throws IOException {
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

public void initialize(String action, String sender, ArrayList<String> Destination, String subject, String content, String date, ClientModel clientModel) {
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
  System.out.println("replyall");

  if (action.equals("replyall")) {
    if (Destination.size() >1) {
      // Se ci sono più di un destinatario, li concateniamo con il mittente
      StringBuilder stringBuilder = new StringBuilder();
      for (String destination : Destination) {
        if(destination.equals(clientModel.getEMail())) continue;
        stringBuilder.append(destination).append(",");
      }
      if(!Objects.equals(sender, clientModel.getEMail())) stringBuilder.append(sender);
      destinations = stringBuilder.toString();
      destinationsfield.setText(destinations);

    } else if (Destination.size() == 1) {
      // Se c'è solo un destinatario, concateniamo solo quel destinatario e il mittente
      destinations = Destination.getFirst() + "," + sender;
      System.out.println(destinations);
      destinationsfield.setText(destinations);
    } else {
      // Se non ci sono destinatari, usiamo solo il mittente
      destinations = sender;
      destinationsfield.setText(destinations);

    }
  } else if(!action.equals("forward")) {

    destinations = sender;
    destinationsfield.setText(destinations);


  }
    subjectfield.setText(prefix + subject);
    ContentField.setText("\n++++++++++++++++++++++++++++\n| Data invia mail: " + date + " da " + sender +"\n"+"| Oggetto:"+subject+ "\n| Contenuto:\n|" + content);
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

    String emailPattern = "^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    boolean success = uniqueDestinations.stream().allMatch(dest -> dest.matches(emailPattern));
    // Mostra lo spinner
    spinner.setVisible(true);

    new Thread(() -> {
      if (success) {
        Email email = new Email(clientModel.getEMail(), new ArrayList<>(uniqueDestinations), subject, content, Date.from(java.time.Instant.now()));


        for(String dest: uniqueDestinations){
          if(this.clientModel.connectToServer()) {
            System.out.println("Connessione al server riuscita");
          } else {
            Platform.runLater(() -> alert("Connessione al server non riuscita", Alert.AlertType.ERROR));
            return;
          }
          if(this.clientModel.CheckEmail(dest)) {
            System.out.println("Email esistente");
          } else {
            spinner.setVisible(false);

            Platform.runLater(() -> alert("Email non esistente: "+ dest, Alert.AlertType.ERROR));
            System.out.println("Email non esistente: "+ dest);
            return;
          }
        }
        if (this.clientModel.connectToServer()) {
          System.out.println("Connessione al server riuscita");
          if (this.clientModel.SendMail(email)) {
            Platform.runLater(() -> {
              Stage stage = (Stage) subjectfield.getScene().getWindow();
              spinner.setVisible(false);
              stage.close();
              alert("Email inviata", Alert.AlertType.INFORMATION);
            });
            System.out.println("Email inviata");
          } else {
            System.out.println("Errore durante l'invio dell'email");
            Platform.runLater(() -> alert("Errore durante l'invio dell'email", Alert.AlertType.ERROR));
          }
        } else {
          System.out.println("Connessione al server non riuscita");
          Platform.runLater(() -> alert("Connessione al server non riuscita", Alert.AlertType.ERROR));
        }
      } else {
        Platform.runLater(() -> alert("Email non inviata, controllare i destinatari", Alert.AlertType.ERROR));
        System.out.println("Email non inviata, controllare i destinatari");
      }
      Platform.runLater(() -> spinner.setVisible(false));
    }).start();
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