package it.unito.prog3progetto.Server;

import it.unito.prog3progetto.Model.Email;
import it.unito.prog3progetto.Model.User;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * this class is used to handle the client requests and send the response back to the client
 */
class ClientHandler implements Runnable {
  private static final Object lock = new Object();
  private final ServerModel server;
  private final Socket socket;
  private ObjectInputStream inStream;
  private ObjectOutputStream outStream;
  private String userMail;

  public ClientHandler(ServerModel server, Socket socket) {
    this.server = server;
    this.socket = socket;
  }
  private boolean isAuthenticated(UUID token) {
    return server.authenticatedTokens.containsKey(token);
  }

  private String getUserEmail(UUID token) {
    return server.authenticatedTokens.get(token);
  }
  @Override
  public void run() {
    try {
      openStreams();
      Object clientObject = inStream.readObject();
      if (clientObject != null && clientObject.toString().equals("LOGIN")) {
        handleLoginRequest();
        return;
      }

      if (!isAuthenticated(clientObject instanceof UUID ? (UUID) clientObject : null)) {
        Platform.runLater(() -> server.textArea.appendText("User is not authenticated.\n"));
        outStream.writeObject(false);
        outStream.flush();
        return;
      }
      Platform.runLater(() -> server.textArea.appendText("User is authenticated.\n"));
      assert clientObject instanceof UUID;
      userMail = getUserEmail((UUID) clientObject);

      clientObject = inStream.readObject();
      switch (clientObject.toString()) {
        case "SENDMAIL":
          handleSendMailRequest();
          break;
        case "RECEIVESENDEMAIL":
        case "RECEIVEEMAIL":
          handleReceiveEmailRequest("RECEIVEEMAIL".equals(clientObject.toString()));
          break;
        case "DELETEMAILRECEIVED":
          handleDeleteEmailRequest(false);
          break;
        case "DELETEMAILSEND":
          handleDeleteEmailRequest(true);
          break;
        case "LOGOUT":
          handleLogoutRequest();
          break;
        case "CLOSE_CONNECTION":
          closeStreams();
          break;
        default:
          outStream.writeObject(false);
          outStream.flush();
          break;
      }
    } catch (IOException | ClassNotFoundException e) {
      Platform.runLater(() -> server.textArea.appendText("Error communicating with the client: " + e.getMessage() + ".\n"));
    } finally {
      closeStreams();
    }
  }

  private void handleLogoutRequest() {
    try {
      outStream.writeObject(true);
      outStream.flush();
      Object tokenObject = inStream.readObject();
      if (tokenObject instanceof UUID token) {
        server.authenticatedTokens.remove(token);
        saveAuthenticatedTokensToFile();
        outStream.writeObject(true);
        outStream.flush();
        Platform.runLater(() -> server.textArea.appendText("User logged out successfully.\n"));
      } else {
        outStream.writeObject(false);
        outStream.flush();
        Platform.runLater(() -> server.textArea.appendText("Error logging out user.\n"));
      }
    } catch (IOException | ClassNotFoundException e) {
      Platform.runLater(() -> server.textArea.appendText("Error logging out user.\n"));
    }
  }

  private void handleLoginRequest() {
    Object userObject = null;
    try {
      outStream.writeObject(true);
      outStream.flush();
      userObject = inStream.readObject();

      if (userObject instanceof User user) {
        boolean isAuthenticated = authenticateUser(user);
        outStream.writeObject(isAuthenticated);
        outStream.flush();
        UUID token = UUID.randomUUID();
        if (isAuthenticated) {
          String userEmail = user.getEmail();
          synchronized (server.authenticatedTokens) { // Synchronize on the map itself
            server.authenticatedTokens.put(token, userEmail);
          }
          outStream.writeObject(token);
          outStream.flush();
          saveAuthenticatedTokensToFile();
          Platform.runLater(() -> server.textArea.appendText("Authentication successful for user " + userEmail + ".\n"));
        } else {
          Platform.runLater(() -> server.textArea.appendText("Authentication failed for user " + user.getEmail() + ".\n"));
        }
      } else {
        Platform.runLater(() -> server.textArea.appendText("Error authenticating user.\n"));
      }
    } catch (IOException | ClassNotFoundException e) {
      User user = (User) userObject;
      Platform.runLater(() -> server.textArea.appendText("Authentication error for user " + user.getEmail() + ".\n"));
    }
  }

  private UUID getExistingToken(String userEmail) {
    for (Map.Entry<UUID, String> entry : server.authenticatedTokens.entrySet()) {
      if (entry.getValue().equals(userEmail)) {
        return entry.getKey();
      }
    }
    return null;
  }


  private void saveAuthenticatedTokensToFile() {
    synchronized (server.authenticatedTokens) { // Synchronize on the map itself
      try (PrintWriter writer = new PrintWriter(new FileWriter("Server/tokens.txt"))) {
        Map<String, Integer> emailSessionCount = new HashMap<>(); // Map to keep track of session count for each email
        for (UUID token : server.authenticatedTokens.keySet()) {
          String email = server.authenticatedTokens.get(token);

          // Increment session count for the current email
          int sessionCount = emailSessionCount.getOrDefault(email, 0) + 1;

          // Check if the session count exceeds the limit
          if (sessionCount > 5) {
            continue; // Skip saving this token if the session count exceeds the limit
          }

          // Save the token to the file
          writer.println(token + "," + email);

          // Update session count for the current email
          emailSessionCount.put(email, sessionCount);
        }
      } catch (IOException e) {
        Platform.runLater(() -> server.textArea.appendText("Error saving authenticated tokens to file.\n"));
      }
    }
  }

  private synchronized void handleSendMailRequest() {
    try {
      outStream.writeObject(true);
      outStream.flush();
      Object mailObject = inStream.readObject();
      if (mailObject instanceof Email email) {
        boolean isSent = sendMail(email);
        outStream.writeObject(isSent);
        outStream.flush();
      } else {
        Platform.runLater(() -> server.textArea.appendText("Error in sending email.\n"));
      }
    } catch (IOException | ClassNotFoundException e) {
      Platform.runLater(() -> server.textArea.appendText("Error in sending email.\n"));
    }
  }

  private void handleReceiveEmailRequest(boolean isinbox) {
    try {outStream.writeObject(true);
      outStream.flush();
      Object userMailObject = inStream.readObject();
      String userMail = (String) userMailObject;
      Date lastEmailDate = (Date) inStream.readObject();
      ArrayList<Email> mails;
      if (isinbox) mails = fetchReceivedEmails(userMail, lastEmailDate);
      else mails = fetchSendEmails(userMail, lastEmailDate);
      Platform.runLater(() -> server.textArea.appendText("Sending email to the client with email: " + userMail + ".\n"));
      outStream.writeObject(mails);
      outStream.flush();
    } catch (IOException | ClassNotFoundException e) {
      Platform.runLater(() -> server.textArea.appendText("Error in sending email to the client.\n"));
      // Gestisci l'eccezione
    }
  }


  // Metodo per chiudere gli stream di input e output
  private void closeStreams() {
    try {
      if (inStream != null) {
        inStream.close();
      }
      if (outStream != null) {
        outStream.close();
      }
      if (socket != null) {
        socket.close();
      }
    } catch (IOException e) {
      Platform.runLater(() -> server.textArea.appendText("Error closing streams.\n"));
    }
  }

  // Metodo per aprire gli stream di input e output per comunicare con il client
  private void openStreams() throws IOException {
    inStream = new ObjectInputStream(socket.getInputStream()); // Stream di input per ricevere dati dal client
    outStream = new ObjectOutputStream(socket.getOutputStream()); // Stream di output per inviare dati al client
    outStream.flush(); // Assicura che tutti i dati siano inviati
  }

  // Metodo per autenticare l'utente confrontando le credenziali con un database
  private boolean authenticateUser(User user) {
    // Legge il database di credenziali da file
    List<String> database = server.readDatabaseFromFile();

    // Verifica se le credenziali dell'utente sono presenti nel database
    for (String entry : database) {
      String[] parts = entry.split(",");
      if (parts.length == 2 && parts[0].trim().equals(user.getEmail()) && parts[1].trim().equals(user.getPassword())) {
        return true; // Se le credenziali sono corrette, restituisce true
      }
    }
    return false; // Se le credenziali non corrispondono, restituisce false
  }

  private boolean sendMail(Email email) {
    boolean success = false; // Variabile per tenere traccia dello stato di invio dell'email
    writeswmail(email.getSender(), email, true);
    for (String destination : email.getDestinations()) {
      success = writeswmail(destination, email, false);
    }

    return success; // Restituisci true solo se l'email è stata inviata con successo a tutti i destinatari
  }


  private ArrayList<Email> fetchReceivedEmails(String usermail, Date lastEmailDate) throws IOException {
    synchronized (lock) {
      return readEmails(usermail, lastEmailDate, false);
    }
  }

  private ArrayList<Email> fetchSendEmails(String usermail, Date lastEmailDate) throws IOException {
    synchronized (lock) {

      return readEmails(usermail, lastEmailDate, true);

    }
  }

  public synchronized  void DeletemailByid(String usermail, String uuidToDelete,boolean sendmail) {
    System.out.println("Deleting email with id: " + uuidToDelete);

    synchronized (lock) {

      List<String> linesToKeep = new ArrayList<>();
      System.out.println("sendmail: " + sendmail);
      String filename = sendmail ? "Server/" + usermail + "_sent.txt" : "Server/" + usermail + "_received.txt";
      System.out.println("filename: " + filename);
      try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
        String line;
        while ((line = br.readLine()) != null) {
          if (!line.contains(uuidToDelete)) {
            linesToKeep.add(line);
          }
        }
      } catch (IOException e) {
        Platform.runLater(() -> server.textArea.appendText("Error deleting email.\n"));
      }

      try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
        for (String line : linesToKeep) {
          bw.write(line);
          bw.newLine();
        }
      } catch (IOException e) {
        Platform.runLater(() -> server.textArea.appendText("Error deleting email.\n"));
      }
    }
  }

  private void handleDeleteEmailRequest(boolean b) {
    try {
      outStream.writeObject(true);
      outStream.flush();
      Object mailObject = inStream.readObject();
      if (mailObject instanceof Email email) {
        DeletemailByid(userMail, email.getId().toString(),b);
        outStream.writeObject(true);
        outStream.flush();
        Platform.runLater(() -> server.textArea.appendText("Email deleted successfully.\n"));
      } else {
        Platform.runLater(() -> server.textArea.appendText("Error in deleting email.\n"));
      }
    } catch (IOException | ClassNotFoundException e) {
      Platform.runLater(() -> server.textArea.appendText("Error in deleting email.\n"));
    }
  }
  /**
   * Read emails from a file
   * @param lastEmailDate The date of the last email received
   * @return An ArrayList of Email objects
   */
  public  ArrayList<Email> readEmails(String usermail, Date lastEmailDate, boolean sendemail)  {
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
            Email email = new Email(sender, destinations, subject, content.replace("<--Accapo-->", "\n"), date, id);
            emails.add(email);
          }
        }
      }
      scanner.close();
    } catch (FileNotFoundException | ParseException e) {
      // In caso di eccezione, restituisci l'elenco vuoto
      Platform.runLater(() -> server.textArea.appendText("Received email list is empty.\n"));
    }
    return emails;


  }

  public  boolean writeswmail(String destination, Email email, boolean sendmail) {
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
        Platform.runLater(() -> server.textArea.appendText("Email sent successfully to " + destination + ".\n"));
      } catch (IOException e) {
        Platform.runLater(() -> server.textArea.appendText("Error in sending email to " + destination + ".\n"));

      }
    } catch (Exception e) {
      Platform.runLater(() -> server.textArea.appendText("Error in sending email to " + destination + ".\n"));
    }
    return success;
  }

}
