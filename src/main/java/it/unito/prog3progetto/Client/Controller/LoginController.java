package it.unito.prog3progetto.Client.Controller;

import it.unito.prog3progetto.Client.ClientModel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javafx.event.EventHandler;
import javafx.scene.input.KeyEvent;

import static it.unito.prog3progetto.Model.Lib.alert;


public class LoginController {
  @FXML
  private ProgressIndicator spinner;

  @FXML
  private TextField emailTextField;
  @FXML
  private TextField passwordField;

    private Stage primaryStage; // Reference to your primary stage

    // Setter method to set the primary stage
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

  public void initialize() {
    emailTextField.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
          loginAsync();
        }
      }
    });

    passwordField.setOnKeyPressed(new EventHandler<KeyEvent>() {
      @Override
      public void handle(KeyEvent event) {
        if (event.getCode().equals(KeyCode.ENTER)) {
          loginAsync();
        }
      }
    });

  }

    public String getEmailFieldValue() {
        return emailTextField.getText();

    }

    @FXML



    private void loginAsync() {
      String useremail = emailTextField.getText();
      String password = passwordField.getText();

      if (Objects.equals(useremail, "") || Objects.equals(password, "")) {
        Platform.runLater(() -> alert("Inserire email e password", Alert.AlertType.ERROR));
        return;
      }

      String emailPattern = "^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
      if (!useremail.matches(emailPattern)) {
        Platform.runLater(() -> alert("Inserire un indirizzo email valido, Formato non valido", Alert.AlertType.ERROR));
        return;
      }

      String host = "127.0.0.1";
      int port = 4445;
      spinner.setVisible(true);
      ClientModel clientModel = new ClientModel(useremail);
      CompletableFuture.supplyAsync(() -> {

        if (clientModel.connectToServer(host, port)) {
          System.out.println("Connessione al server riuscita");
          UUID token = clientModel.sendAndCheckCredentials(useremail, password);
          clientModel.setToken(token);
          return token;
        } else {
          return null;
        }
      }).thenAccept(token -> {
        if (token != null) {
          Platform.runLater(() -> spinner.setVisible(false));

          System.out.println("Login riuscito");
          try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/it/unito/prog3progetto/Client/Client.fxml"));
            Parent root = loader.load();
            ClientController clientController = loader.getController();
            clientController.setPrimaryStage(primaryStage);
            clientController.initialize(clientModel); // Pass the clientModel
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/it/unito/prog3progetto/Client/style.css").toExternalForm());
            Platform.runLater(() -> {
              primaryStage.setScene(scene);
              primaryStage.setTitle("Client Email - Progetto di Programmazione 3");
              primaryStage.setMinHeight(600);
              primaryStage.setMinWidth(800);
              primaryStage.show();
            });
          } catch (IOException e) {
            e.printStackTrace();
          }
        } else {
          Platform.runLater(() -> alert("Connessione al server non riuscita", Alert.AlertType.ERROR));
          Platform.runLater(() -> spinner.setVisible(false));

        }
      });
    }





}