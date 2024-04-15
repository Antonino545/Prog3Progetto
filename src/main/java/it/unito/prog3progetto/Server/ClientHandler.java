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
  private List<String> database;


  public ClientHandler(ServerModel server, Socket socket) {
    this.server = server;
    this.socket = socket;
  }
  private boolean isAuthenticated(UUID token) {
    if(token==null) return false;
    return server.authenticatedTokens.containsKey(token);
  }

  private String getUserEmail(UUID token) {
    return server.authenticatedTokens.get(token);
  }

  /**
   *  Questo metodo viene eseguito quando il thread viene avviato
   *  e gestisce le richieste del client in base all'oggetto inviato dal client
   */
  @Override
  public void run() {
    try {
      openStreams();// Apre gli stream di input e output per comunicare con il client
      Object clientObject = inStream.readObject(); // Legge l'oggetto inviato dal client
      if (clientObject != null && clientObject.toString().equals("LOGIN")) {
        handleLoginRequest();
        return;
      }

      // Verifica se l'utente è autenticato
      if (!isAuthenticated(clientObject instanceof UUID ? (UUID) clientObject : null)) {
        Platform.runLater(() -> server.appendToLog("User is not authenticated"));
        System.out.println(clientObject);
        outStream.writeObject(false);
        outStream.flush();
        return;
      }
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
        case "CHECKEMAIL":
           handleCheckEmailRequest();
          break;
        case "CLOSE_CONNECTION":
          closeStreams();
          break;
        default:
          outStream.writeObject(false);
          outStream.flush();
          break;
      }
    } catch (IOException  | ClassNotFoundException e) {
      if(e.getMessage()!=null) Platform.runLater(() ->server.appendToLog("Error communicating with the client: " + e.getMessage() + "."));
    } finally {
      closeStreams();
    }
  }

  private void handleCheckEmailRequest() {
    try {
      outStream.writeObject(true);
      outStream.flush();
      Object mailObject = inStream.readObject();
      if (mailObject instanceof String email) {
        boolean isSent = Checkemail(email);
        outStream.writeObject(isSent);
        outStream.flush();
      }
    }catch (IOException | ClassNotFoundException e) {
      Platform.runLater(() ->server.appendToLog("Error in sending email."));
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
        Platform.runLater(() ->server.appendToLog("User logged out successfully."));
      } else {
        outStream.writeObject(false);
        outStream.flush();
        Platform.runLater(() ->server.appendToLog("Error logging out user."));
      }
    } catch (IOException | ClassNotFoundException e) {
      Platform.runLater(() ->server.appendToLog("Error logging out user."));
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
          Platform.runLater(() ->server.appendToLog("Authentication successful for user " + userEmail + "."));
        } else {
          Platform.runLater(() ->server.appendToLog("Authentication failed for user " + user.getEmail() + "."));
        }
      } else {
        Platform.runLater(() ->server.appendToLog("Error authenticating user."));
      }
    } catch (IOException | ClassNotFoundException e) {
      User user = (User) userObject;
      Platform.runLater(() ->server.appendToLog("Authentication error for user " + user.getEmail() + "."));
    }
  }



  private void saveAuthenticatedTokensToFile() {
    // Map to keep track of session count for each email
    Map<String, Integer> emailSessionCount = new HashMap<>();

    try (PrintWriter writer = new PrintWriter(new FileWriter("Server/tokens.txt"))) {
      // Load existing tokens from file
      Map<UUID, String> existingTokens = loadTokensFromFile();

      for (UUID token : server.authenticatedTokens.keySet()) {
        String email = server.authenticatedTokens.get(token);

        // Remove existing sessions for this email from loaded tokens
        existingTokens.values().removeIf(e -> e.equals(email));

        // Increment session count for the current email
        int sessionCount = 1;

        // Check if the session count exceeds the limit
        if (server.authenticatedTokens.containsValue(email)) {
          sessionCount = emailSessionCount.getOrDefault(email, 0) + 1;
          if (sessionCount > 10) {
            continue; // Skip saving this token if the session count exceeds the limit
          }
        }

        // Save the token to the file
        writer.println(token + "," + email);

        // Update session count for the current email
        emailSessionCount.put(email, sessionCount);
      }

      // Save remaining existing tokens (with removed sessions) back to the file
      for (UUID token : existingTokens.keySet()) {
        String email = existingTokens.get(token);
        writer.println(token + "," + email);
      }
    } catch (IOException e) {
      Platform.runLater(() -> server.appendToLog("Error saving authenticated tokens to file."));
    }
  }

  private Map<UUID, String> loadTokensFromFile() throws IOException {
    Map<UUID, String> tokens = new HashMap<>();
    try (BufferedReader reader = new BufferedReader(new FileReader("Server/tokens.txt"))) {
      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split(",");
        if (parts.length == 2) {
          UUID token = UUID.fromString(parts[0]);
          String email = parts[1];
          tokens.put(token, email);
        }
      }
    }
    return tokens;
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
        Platform.runLater(() ->server.appendToLog("Error in sending email."));
      }
    } catch (IOException | ClassNotFoundException e) {
      Platform.runLater(() ->server.appendToLog("Error in sending email."));
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
      Platform.runLater(() ->server.appendToLog("Sending email to the client with email: " + userMail + "."));
      outStream.writeObject(mails);
      outStream.flush();
    } catch (IOException | ClassNotFoundException e) {
      Platform.runLater(() ->server.appendToLog("Error in sending email to the client."));
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
      Platform.runLater(() ->server.appendToLog("Error closing streams."));
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
   database = server.readDatabaseFromFile();

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
  boolean Checkemail(String usermail){
    database = server.readDatabaseFromFile();
    for (String entry : database) {
      String[] parts = entry.split(",");
      if (parts.length == 2 && parts[0].trim().equals(usermail)) {
        return true; // Se le credenziali sono corrette, restituisce true
      }
    }
    return false; // Se le credenziali non corrispondono, restituisce false
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

    synchronized (lock) {

      List<String> linesToKeep = new ArrayList<>();
      String filename = sendmail ? "Server/" + usermail + "_sent.txt" : "Server/" + usermail + "_received.txt";
      try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
        String line;
        while ((line = br.readLine()) != null) {
          if (!line.contains(uuidToDelete)) {
            linesToKeep.add(line);
          }
        }
      } catch (IOException e) {
        Platform.runLater(() ->server.appendToLog("Error deleting email."));
      }

      try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
        for (String line : linesToKeep) {
          bw.write(line);
          bw.newLine();
        }
      } catch (IOException e) {
        Platform.runLater(() ->server.appendToLog("Error deleting email."));
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
        Platform.runLater(() ->server.appendToLog("Email deleted successfully."));
      } else {
        Platform.runLater(() ->server.appendToLog("Error in deleting email."));
      }
    } catch (IOException | ClassNotFoundException e) {
      Platform.runLater(() ->server.appendToLog("Error in deleting email."));
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
            Email email = new Email(sender, destinations, subject, content.replace("<--Accapo-->", ""), date, id);
            emails.add(email);
          }
        }
      }
      scanner.close();
    } catch (FileNotFoundException | ParseException e) {
      // In caso di eccezione, restituisci l'elenco vuoto
      Platform.runLater(() ->server.appendToLog("Received email list is empty."));
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
        Platform.runLater(() ->server.appendToLog("Email sent successfully to " + destination + "."));
      } catch (IOException e) {
        Platform.runLater(() ->server.appendToLog("Error in sending email to " + destination + "."));

      }
    } catch (Exception e) {
      Platform.runLater(() ->server.appendToLog("Error in sending email to " + destination + "."));
    }
    return success;
  }


}
