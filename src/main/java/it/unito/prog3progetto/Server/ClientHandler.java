package it.unito.prog3progetto.Server;

import it.unito.prog3progetto.Model.Email;
import it.unito.prog3progetto.Model.User;
import javafx.application.Platform;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static it.unito.prog3progetto.Model.Lib.readEmails;
import static it.unito.prog3progetto.Model.Lib.writeswmail;

/**
 * this class is used to handle the client requests and send the response back to the client
 */
class ClientHandler implements Runnable {
  private static final Object lock = new Object();
  private final Server server;
  private final Socket socket;
  private ObjectInputStream inStream;
  private ObjectOutputStream outStream;
  private String userMail;

  public ClientHandler(Server server, Socket socket) {
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
        case "DELETEMAIL":
          handleDeleteEmailRequest();
          break;
        case "LOGOUT":
          server.authenticatedTokens.remove((UUID) clientObject);
          outStream.writeObject(true);
          outStream.flush();
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

  private void handleLoginRequest() {
    Object userObject = null;
    try {
      outStream.writeObject(true);
      outStream.flush();
      userObject = inStream.readObject();

      if (userObject instanceof User) {
        User user = (User) userObject;
        boolean isAuthenticated = authenticateUser(user);
        outStream.writeObject(isAuthenticated);
        outStream.flush();
        UUID token = UUID.randomUUID();
        if (isAuthenticated) {
          server.authenticatedTokens.put(token, user.getEmail());
          outStream.writeObject(token);
          outStream.flush();
          Platform.runLater(() -> server.textArea.appendText("Authentication successful for user " + user.getEmail() + ".\n"));
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
      e.printStackTrace();
    }
  }

  private void handleReceiveEmailRequest(boolean b) {
    try {outStream.writeObject(true);
      outStream.flush();
      Object userMailObject = inStream.readObject();
      String userMail = (String) userMailObject;
      Date lastEmailDate = (Date) inStream.readObject();
      ArrayList<Email> mails = new ArrayList<Email>();
      if (b) mails = fetchReceivedEmails(userMail, lastEmailDate);
      else mails = fetchSendEmails(userMail, lastEmailDate);
      Platform.runLater(() -> server.textArea.appendText("Sending email to the client with email: " + userMail + ".\n"));
      outStream.writeObject(mails);
      outStream.flush();
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
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
      e.printStackTrace();
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
    writeswmail(email.getSender(), email, true, server.textArea);
    for (String destination : email.getDestinations()) {
      success = writeswmail(destination, email, false, server.textArea);
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

  public static void DeletemailByid(String usermail, String uuidToDelete) {
    synchronized (lock) {

      List<String> linesToKeep = new ArrayList<>();

      try (BufferedReader br = new BufferedReader(new FileReader(usermail + ".txt"))) {
        String line;
        while ((line = br.readLine()) != null) {
          if (!line.contains(uuidToDelete)) {
            linesToKeep.add(line);
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
      }

      try (BufferedWriter bw = new BufferedWriter(new FileWriter(usermail + ".txt"))) {
        for (String line : linesToKeep) {
          bw.write(line);
          bw.newLine();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  private void handleDeleteEmailRequest() {
    try {
      outStream.writeObject(true);
      outStream.flush();
      Object mailObject = inStream.readObject();
      if (mailObject instanceof Email email) {
        DeletemailByid(userMail, email.getId().toString());
        outStream.writeObject(true);
        outStream.flush();
        Platform.runLater(() -> server.textArea.appendText("Email deleted successfully.\n"));
      } else {
        Platform.runLater(() -> server.textArea.appendText("Error in deleting email.\n"));
      }
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
    }
  }

}
