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

public class ClientController {
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
        // Inizializzazione controller
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

        if (!Objects.equals(mail, "") && !Objects.equals(password, "")) {
            try {
                System.out.println("Email: " + mail);
                System.out.println("Password: " + password);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Email.fxml"));
                Parent root = loader.load();

                // Pass any data to the controller of the new scene if needed
                ClientController controller = loader.getController();
                // controller.setData(data);

                Scene scene = new Scene(root);
                primaryStage.setScene(scene);
                primaryStage.setResizable(false); // Imposta le dimensioni della finestra come non modificabili
                primaryStage.show();

            } catch (IOException e) {
                e.printStackTrace();
                // Handle exception accordingly
            }
        } else {
            // Handle unsuccessful login
            // Handle unsuccessful login
            System.out.println("Login failed");

            // Creazione di un Alert di tipo ERROR
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Errore di accesso");
            alert.setHeaderText(null);
            alert.setContentText("Accesso non riuscito. Controlla le credenziali e riprova.");

            // Mostra l'Alert e attendi la risposta dell'utente
            alert.showAndWait();    }
    }
}
