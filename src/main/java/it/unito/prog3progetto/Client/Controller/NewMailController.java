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
import java.time.Instant;
import java.util.*;
import static it.unito.prog3progetto.Model.Lib.Alert;

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

public void initialize(String action, String sender, ArrayList<String> DestinationList, String subject, String content, String date, ClientModel clientModel) {
    EventHandler<ActionEvent> handler = event -> {
        try {
            sendMail();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    };
    this.clientModel = clientModel;
    String prefix = action.equals("reply") ? "Re: " : action.equals("forward") ? "Fwd: " : "ReALL: ";
  String destinations = "";
  System.out.println("replyall");

  if (action.equals("replyall")) {
    if (DestinationList.size() >1) {
      // Se ci sono più di un destinatario, li concateniamo con il mittente
      StringBuilder stringBuilder = new StringBuilder();
      for (String destination : DestinationList) {
        if(!destination.equals(clientModel.getEMail()))stringBuilder.append(destination).append(",");
      }
      if(!Objects.equals(sender, clientModel.getEMail())) stringBuilder.append(sender);
      destinations = stringBuilder.toString();
      destinationsfield.setText(destinations);

    } else if (DestinationList.size() == 1) {
      // Se c'è solo un destinatario, concateniamo solo quel destinatario e il mittente

      destinations = DestinationList.getFirst() + "," + sender;
      System.out.println(destinations);
      destinationsfield.setText(destinations);
    } else {
      // Se non ci sono destinatari, usiamo solo il mittente
      if(!Objects.equals(sender, clientModel.getEMail()))
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
     Alert("Errore durante l'invio dell'email", Alert.AlertType.ERROR);
      System.out.println("Errore durante l'invio dell'email il Client e null");
      return;
    }
    System.out.println("Prova di invio email");

    String destination = destinationsfield.getText();
    String subject = subjectfield.getText();
    String content = ContentField.getText();

    if (destination.isEmpty()) {
      Alert("Inserire il destinatario o i destinatari", Alert.AlertType.ERROR);
      return;
    }

    if (subject.isEmpty()) {
      if (!confirmDialog("Il campo oggetto è vuoto, vuoi continuare?")) {
        return;
      }
    }

    String[] destinationsArray = destination.split(",");
    Set<String> uniqueDestinations = new HashSet<>(Arrays.asList(destinationsArray));

    String emailPattern = "^[A-Za-z0-9]+\\.[A-Za-z0-9]+@progmail\\.com$";
    boolean success = uniqueDestinations.stream().allMatch(dest -> dest.matches(emailPattern));
    // Mostra lo spinner
    spinner.setVisible(true);

    new Thread(() -> {
      if (success) {
        Email email = new Email(clientModel.getEMail(), new ArrayList<>(uniqueDestinations), subject, content, Date.from(Instant.now()));


        for(String dest: uniqueDestinations){
          if(this.clientModel.connectToServer()) {
            System.out.println("Connessione al server riuscita");
          } else {
            Platform.runLater(() -> Alert("Connessione al server non riuscita. Impossibile inviare email", Alert.AlertType.ERROR));
            Platform.runLater(() -> spinner.setVisible(false));
            return;
          }
          if(this.clientModel.CheckEmail(dest)) {
            System.out.println("Email esistente");
          } else {
            Platform.runLater(() -> spinner.setVisible(false));

            Platform.runLater(() -> Alert("Email non esistente: "+ dest, Alert.AlertType.ERROR));
            System.out.println("Email non esistente: "+ dest);
            return;
          }
        }
        if (this.clientModel.connectToServer()) {
          System.out.println("Connessione al server riuscita");
          if (this.clientModel.SendMail(email)) {
            Platform.runLater(() -> {
              Stage stage = (Stage) subjectfield.getScene().getWindow();
              Platform.runLater(() -> spinner.setVisible(false));
              stage.close();
              Alert("Email inviata", Alert.AlertType.INFORMATION);
            });
            System.out.println("Email inviata");

          } else {
            System.out.println("Errore durante l'invio dell'email");
            Platform.runLater(() -> Alert("Errore durante l'invio dell'email", Alert.AlertType.ERROR));
            Platform.runLater(() -> spinner.setVisible(false));

          }
        } else {
          System.out.println("Connessione al server non riuscita");
          Platform.runLater(() -> Alert("Connessione al server non riuscita", Alert.AlertType.ERROR));
          Platform.runLater(() -> spinner.setVisible(false));

        }
      } else {
        Platform.runLater(() -> Alert("Email non inviata, controllare i destinatari", Alert.AlertType.ERROR));
        System.out.println("Email non inviata,destinatari non sono nel formato corretto");
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