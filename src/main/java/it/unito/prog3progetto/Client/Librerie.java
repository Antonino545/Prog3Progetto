package it.unito.prog3progetto.Client;

import it.unito.prog3progetto.Client.Controller.LoginController;
import it.unito.prog3progetto.Model.Email;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.*;
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
   * @param lastEmailDate The date of the last email received
   * @return An ArrayList of Email objects
   * @throws IOException If an I/O error occurs
   */
  public static ArrayList<Email> readEmails(String usermail, Date lastEmailDate, boolean sendemail) throws IOException {
      SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
      ArrayList<Email> emails = new ArrayList<>();
      try {
        // Determina il nome del file in base al tipo di email
        String filename = sendemail ? "Server/" + usermail + "_sent.txt" : "Server/" + usermail + "_received.txt";

        File file = new File(filename);
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) {
          String line = scanner.nextLine();
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
              emails.add(email);
            }
          }
        }
        scanner.close();
      } catch (FileNotFoundException | ParseException e) {
        // In caso di eccezione, restituisci l'elenco vuoto
        e.printStackTrace();
      }
      return emails;


  }

  public static boolean writeswmail(String destination, Email email, boolean sendmail, TextArea textArea) {
    boolean success = false; // Variabile per tenere traccia dello stato di invio dell'email

    try {
      // Determina il nome del file in base al tipo di email
      String filename = sendmail ? "Server/" + destination + "_sent.txt" : "Server/" + destination + "_received.txt";

      // Scrivi l'email nel file corretto
      try (FileWriter fileWriter = new FileWriter(filename, true);
           BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
        // Scrivi l'email nel file
        bufferedWriter.write(email.emailNoEndLine().toString());
        bufferedWriter.newLine();
        success = true; // L'invio dell'email è riuscito per questo destinatario
        Platform.runLater(() -> textArea.appendText("Email sent successfully to " + destination + ".\n"));
      } catch (IOException e) {
        Platform.runLater(() -> textArea.appendText("Error in sending email to " + destination + ".\n"));
        e.printStackTrace();
      }
    } catch (Exception e) {
      Platform.runLater(() -> textArea.appendText("Error in sending email to " + destination + ".\n"));
      e.printStackTrace();
    }
    return success;
  }

}
