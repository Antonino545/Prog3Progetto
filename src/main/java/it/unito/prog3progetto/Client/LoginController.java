package it.unito.prog3progetto.Client;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

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
    String mail = emailTextField.getText();
    String password = passwordTextField.getText();

    Client c = new Client(0);
    String host= "127.0.0.1";
    int port= 4445;

    if ((!Objects.equals(mail, "") && !Objects.equals(password, ""))&&(c.sendAndCheckCredentials(host,port,mail,password))) {
      try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Client.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Imposta le dimensioni della finestra come non modificabili
        primaryStage.show();

      } catch (IOException e) {
        e.printStackTrace();
        // Handle exception accordingly
      }
    } else {
      // Handle unsuccessful login
      System.out.println("Login failed");

      // Creazione di un Alert di tipo ERROR
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Errore di accesso");
      alert.setHeaderText(null);
      alert.setContentText("Email o password errate! Riprova. Oppure server non  attivo.");

      // Mostra l'Alert e attendi la risposta dell'utente
      alert.showAndWait();
    }


  }




}