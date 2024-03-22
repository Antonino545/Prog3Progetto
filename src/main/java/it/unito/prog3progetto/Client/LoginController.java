package it.unito.prog3progetto.Client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class LoginController {
    @FXML
    private TextField emailTextField;
    @FXML
    private TextField passwordTextField;

    Client client;
    private Stage primaryStage; // Reference to your primary stage

    // Setter method to set the primary stage
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

   public void initialize() {

       // Creazione della lista di oggetti MailItem
   }

    public String getEmailFieldValue() {
        return emailTextField.getText();

    }
    public String getPasswordFieldValue() {
        return passwordTextField.getText();
    }
    @FXML



  private void Login() {
    String useremail = emailTextField.getText();
    String password = passwordTextField.getText();

      if(Objects.equals(useremail, "") || Objects.equals(password, "")){
        alert("Inserire email e password");
        return;
      }
      String emailPattern = "^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
      if (!useremail.matches(emailPattern)) {
        alert("Inserire un indirizzo email valido , Formato non valido");
        return;
      }
      Client client = new Client(useremail);
    String host= "127.0.0.1";
    int port= 4445;
    if(client.connectToServer(host, port)){
      System.out.println("Connessione al server riuscita");
    }else{
      System.out.println("Connessione al server non riuscita");
      alert("Connessione al server non riuscita");
      return;
    }


    if (client.sendAndCheckCredentials(host,port,useremail,password)) {
      try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Client.fxml"));
        Parent root = loader.load();
        ClientController controller = loader.getController();

        // Imposta manualmente il controller
        // controller.setClient(client); // Rimuovi questo

        controller.setPrimaryStage(primaryStage);

        // Imposta manualmente il client
        controller.initialize(client);

        // Non chiamare initialize() esplicitamente

        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Imposta le dimensioni della finestra come non modificabili
        primaryStage.show();

      } catch (IOException e) {
        e.printStackTrace();
        // Gestisci l'eccezione di conseguenza
      }



    } else {
      // Handle unsuccessful login
      alert("Credenziali non valide");
      client.closeConnections();
      return;
    }


  }
  public void alert(String message){
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle("Errore di accesso");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();

  }




}