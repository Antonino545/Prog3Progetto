package it.unito.prog3progetto.Client.Controller;

import it.unito.prog3progetto.Client.ClientModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;


public class LoginController {
    @FXML
    private TextField emailTextField;
    @FXML
    private TextField passwordTextField;

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
      ClientModel clientModel = new ClientModel(useremail);
      String host= "127.0.0.1";
      int port= 4445;
    if(clientModel.connectToServer(host, port)){
      System.out.println("Connessione al server riuscita");
    }else{
      System.out.println("Connessione al server non riuscita");
      alert("Connessione al server non riuscita");
      return;
    }

    UUID token = clientModel.sendAndCheckCredentials(host, port, useremail, password);
    clientModel.setToken(token);
    if (token != null) {
      try {
        System.out.println("Login riuscito");
        FXMLLoader loader = new FXMLLoader(new File("src/main/resources/it/unito/prog3progetto/Client/Client.fxml").toURI().toURL());
        Parent root = loader.load();
        ClientController clientController = loader.getController();

        clientController.setPrimaryStage(primaryStage);

        // Imposta manualmente il clientModel
        clientController.initialize(clientModel);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(new File("src/main/resources/it/unito/prog3progetto/Client/style.css").toURI().toURL().toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.setTitle("ClientModel");

        // Aggiungi il listener per gestire le dimensioni della finestra quando è massimizzata
        primaryStage.maximizedProperty().addListener((observable, oldValue, newValue) -> {
          if (newValue) { // Se la finestra è massimizzata
            // Imposta le dimensioni minime in base alle dimensioni correnti
            primaryStage.setMinWidth(primaryStage.getWidth());
            primaryStage.setMinHeight(primaryStage.getHeight());
          } else { // Se la finestra non è massimizzata
            // Ripristina le dimensioni minime predefinite
            primaryStage.setMinWidth(300); // Imposta le dimensioni minime desiderate in base alle tue esigenze
            primaryStage.setMinHeight(400); // Imposta le dimensioni minime desiderate in base alle tue esigenze
          }
        });

        primaryStage.show();

      } catch (IOException e) {
        e.printStackTrace();
      }





    } else {
      // Handle unsuccessful login
      alert("Credenziali non valide");
      clientModel.closeConnections();
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