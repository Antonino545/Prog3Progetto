package it.unito.prog3progetto.Client;

import it.unito.prog3progetto.Lib.Email;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Librerie {

  public static void alert(String message, Alert.AlertType type) {
    Alert alert = new Alert(type);
    alert.setTitle("Avviso");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  public  void loadLogin(Stage primaryStage) throws IOException {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("Login.fxml"));
    Parent root = loader.load();
    LoginController controller = loader.getController();
    controller.setPrimaryStage(primaryStage);
    Scene scene = new Scene(root);
    scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("style.css")).toExternalForm());
    primaryStage.setScene(scene);
    primaryStage.setTitle("Email Client - Progetto di Programmazione 3");
    primaryStage.show();
  }


  public static void writeEmails(List<Email> emails) throws IOException {
    String fileName = "emails.txt";
    try (FileWriter writer = new FileWriter(fileName)) {
      for (Email email : emails) {
        writer.write(email.emailNoEndLine().toString());
      }
      System.out.println("Email salvate con successo nel file: " + fileName);
    }
  }

  /**
   * Read emails from a file
   * @param filename The name of the file to read
   * @param lastEmailDate The date of the last email received
   * @return An ArrayList of Email objects
   * @throws IOException If an I/O error occurs
   */
  public static ArrayList<Email> readEmails(String filename, Date lastEmailDate,boolean sendemail) throws IOException {
    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
    ArrayList<Email> emails = new ArrayList<>();
    boolean foundHeader = false; // Flag per indicare se è stata trovata la linea di intestazione
    try {
      File file = new File(filename);
      Scanner scanner = new Scanner(file);
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();

        if (!sendemail &&line.equals("<----------------------------------------------------Email Ricevute---------------------------------------------------->")) {
          foundHeader = true;
          continue; // Passa alla prossima iterazione del ciclo
        }else{
          if(sendemail && line.equals("<----------------------------------------------------Email Inviate---------------------------------------------------->")){
            foundHeader = true;
            continue; // Passa alla prossima iterazione del ciclo
          }
        }
        if (foundHeader) {
          String[] parts = line.split(" , ");
          if (parts.length >= 6) {
            String sender = parts[0];
            String destinationsString = parts[1];
            String subject = parts[2];
            String content = parts[3];
            String dateString = parts[4];
            String idString = parts[5];
            String[] destinationsArray = destinationsString.substring(1, destinationsString.length() - 1).split(", ");
            ArrayList<String> destinations = new ArrayList<>(Arrays.asList(destinationsArray));
            Date date = dateFormat.parse(dateString);
            UUID id = UUID.fromString(idString);
            // Se lastEmailDate è null, aggiungi tutte le email senza alcun controllo sulla data
            if (lastEmailDate == null || date.after(lastEmailDate)) {
              Email email = new Email(sender, destinations, subject, content, date, id);

              emails.add(email.emailEndLine());
            }
          }
        }
      }
      scanner.close();
    } catch (FileNotFoundException | ParseException e) {
      // In caso di eccezione, restituisci l'elenco vuoto
      return emails;
    }
    return emails;
  }


}
