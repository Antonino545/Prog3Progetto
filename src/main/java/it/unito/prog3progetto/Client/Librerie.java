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
  public static Client readUserEmailFromFile() throws IOException {
    String fileName = "user_email.txt";
    try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
      System.out.println("Contenuto del file " + fileName + ":");
      String line;
      while ((line = reader.readLine()) != null) {
        return new Client(line);
      }
    }
    return null;
  }

  public static void alert(String message, Alert.AlertType type) {
    Alert alert = new Alert(type);
    alert.setTitle("Avviso");
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  public  void loadLogin(Stage primaryStage) throws IOException {
    deleteEmailFile();
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


  private static void deleteEmailFile() {
    try {
      // Percorso del file user_email.txt
      String filePath = "user_email.txt";
      File file = new File(filePath);

      // Controlla se il file esiste e cancellalo
      if (file.exists()) {
        if (file.delete()) {
          System.out.println("Il file user_email.txt è stato eliminato con successo.");
        } else {
          System.out.println("Impossibile eliminare il file user_email.txt.");
        }
      } else {
        System.out.println("Il file user_email.txt non esiste.");
      }
    } catch (Exception e) {
      // Gestisci le eccezioni, ad esempio mostrando un messaggio di errore all'utente
      e.printStackTrace();
    }
  }


  public static void writeEmails(List<Email> emails) throws IOException {
    String fileName = "emails.txt";
    try (FileWriter writer = new FileWriter(fileName)) {
      for (Email email : emails) {
        String content= email.getContent().replace("\n", "<--Accapo-->");
        writer.write(email.getSender() + " , " + email.getDestinations() + " , " + email.getSubject() + " , " + content + " , " + email.getDatesendMail() + " , " + email.getId() + "\n");
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
  public static ArrayList<Email>  readEmails(String filename,Date lastEmailDate) throws IOException {
    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
    ArrayList<Email> emails = new ArrayList<>();
    try {
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
          if (lastEmailDate == null) {
            Email email = new Email(sender, destinations, subject, content, date, id);
            emails.add(email);
          } else if (date.after(lastEmailDate)) {
            Email email = new Email(sender, destinations, subject, content, date, id);
            emails.add(email);
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
  static void deleteFileIfExists(String filename) {
    File file = new File(filename);
    if (file.exists()) {
      if (file.delete()) {
        System.out.println("Il file " + filename + " è stato eliminato con successo.");
      } else {
        System.out.println("Impossibile eliminare il file " + filename);
      }
    } else {
      System.out.println("Il file " + filename + " non esiste.");
    }
  }

}
