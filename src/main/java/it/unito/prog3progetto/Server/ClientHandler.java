package it.unito.prog3progetto.Server;

import it.unito.prog3progetto.Model.Email;
import it.unito.prog3progetto.Model.User;

import java.io.*;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class ClientHandler implements Runnable {
  private final ServerModel server;
  private final Socket socket;
  private ObjectInputStream inStream;
  private ObjectOutputStream outStream;
  private String userMail;
  private final ConcurrentHashMap<String, String> database;

  public ClientHandler(ServerModel server, Socket socket,ConcurrentHashMap <String,String> database) {
    this.server = server;
    this.socket = socket;
    this.database = database;
  }

  private boolean isAuthenticated(UUID token) {
    if(token==null) return false;
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
          Thread.currentThread().interrupt(); // Interrompi il thread corrente

          break;
        default:
          outStream.writeObject(false);
          outStream.flush();
          Thread.currentThread().interrupt(); // Interrompi il thread corrente

          break;
      }
    } catch (IOException  | ClassNotFoundException e) {
      if(e.getMessage()!=null) server.appendToLog("Error communicating with the client: " + e.getMessage() + ".");
    } finally {
      closeStreams();
      Thread.currentThread().interrupt(); // Interrompi il thread corrente

    }
  }

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
      server.appendToLog("Error closing streams.");
    }
  }

  private void openStreams() throws IOException {
    inStream = new ObjectInputStream(socket.getInputStream());
    outStream = new ObjectOutputStream(socket.getOutputStream());
    outStream.flush();
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
      server.appendToLog("Error in sending email.");
    }
  }

  private void handleLogoutRequest() {
    try {
      outStream.writeObject(true);
      outStream.flush();
      Object tokenObject = inStream.readObject();
      if (tokenObject instanceof UUID token) {
        String userEmail = server.authenticatedTokens.get(token);
        server.authenticatedTokens.remove(token);
        saveAuthenticatedTokensToFile(token,userEmail);
        outStream.writeObject(true);
        outStream.flush();
        server.appendToLog("User logged out successfully.");
      } else {
        outStream.writeObject(false);
        outStream.flush();
        server.appendToLog("Error logging out user.");
      }
    } catch (IOException | ClassNotFoundException e) {
      server.appendToLog("Error logging out user.");
    }
  }

  private void handleSendMailRequest() {
    try {
      outStream.writeObject(true);
      outStream.flush();
      Object mailObject = inStream.readObject();
      if (mailObject instanceof Email email) {

       boolean isSent = sendMail(email);

        outStream.writeObject(isSent);
        outStream.flush();
      } else {
        server.appendToLog("Error in sending email.");
      }
    } catch (IOException | ClassNotFoundException e) {
      server.appendToLog("Error in sending email.");
    }
  }

  private void handleReceiveEmailRequest(boolean isinbox) {
    try {
      outStream.writeObject(true);
      outStream.flush();
      Object userMailObject = inStream.readObject();
      String userMail = (String) userMailObject;
      Date lastEmailDate = (Date) inStream.readObject();
      ArrayList<Email> mails;
      if (isinbox) mails = fetchReceivedEmails(userMail, lastEmailDate);
      else mails = fetchSendEmails(userMail, lastEmailDate);
      server.appendToLog("Sending email to the client with email: " + userMail + ".");
      outStream.writeObject(mails);
      outStream.flush();
    } catch (IOException | ClassNotFoundException e) {
      server.appendToLog("Error in sending email to the client.");
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
          synchronized (server.authenticatedTokens) {
            server.authenticatedTokens.put(token, userEmail);
            server.tokenCreation.put(token, new Date(Date.from(Instant.now()).getTime()));
            saveAuthenticatedTokensToFile(token,userEmail);
          }
          outStream.writeObject(token);
          outStream.flush();
          server.appendToLog("Authentication successful for user " + userEmail + ".");
        } else {
          server.appendToLog("Authentication failed for user " + user.getEmail() + ".");
        }
      } else {
        server.appendToLog("Error authenticating user.");
      }
    } catch (IOException | ClassNotFoundException e) {
      User user = (User) userObject;
      server.appendToLog("Authentication error for user " + user.getEmail() + ".");
    }
  }

  private void saveAuthenticatedTokensToFile(UUID token,String userEmail) {
    synchronized (server.authenticatedTokens) {
      int tokenCount = (int) server.authenticatedTokens.values().stream()
              .filter(email -> email.equals(userEmail))
              .count();

      if (tokenCount > 10) {
        UUID oldestToken = server.authenticatedTokens.entrySet().stream()
                .filter(tokenEntry -> tokenEntry.getValue().equals(userEmail))
                .min(Comparator.comparing(tokenEntry -> server.tokenCreation.get(tokenEntry.getKey())))
                .map(Map.Entry::getKey)
                .orElse(null);

        if (oldestToken != null) {
          server.authenticatedTokens.remove(oldestToken);
          server.tokenCreation.remove(oldestToken);
        }
      }

      server.authenticatedTokens.put(token, userEmail);
      server.tokenCreation.put(token, new Date());

      try (BufferedWriter writer = new BufferedWriter(new FileWriter("Server/tokens.txt"))) {
        for (Map.Entry<UUID, String> entry : server.authenticatedTokens.entrySet()) {
          writer.write(entry.getKey().toString() + "," + entry.getValue()+ "," + server.tokenCreation.get(entry.getKey()).getTime());
          writer.newLine();
        }
      } catch (IOException e) {
        server.appendToLog("Error saving authenticated tokens to file.");
      }
    }
  }

  private boolean authenticateUser(User user) {
    String password = database.get(user.getEmail());
    return password != null && password.equals(user.getPassword());
  }

  private  boolean sendMail(Email email) {
    boolean success = false;
    synchronized (server.getLock(email.getSender(), true)) {
      success = writeswmail(email.getSender(), email, true);
    }
    for (String destination : email.getDestinations()) {
      synchronized (server.getLock(destination, false))
      {
      success = writeswmail(destination, email, false);
      }
    }

    return success;
  }

  boolean Checkemail(String usermail){
    return database.containsKey(usermail);
  }

  private ArrayList<Email> fetchReceivedEmails(String usermail, Date lastEmailDate) throws IOException {

    synchronized (server.getLock(usermail, false)) {
      return readEmails(usermail, lastEmailDate, false);
    }
  }

  private ArrayList<Email> fetchSendEmails(String usermail, Date lastEmailDate) throws IOException {

    synchronized (server.getLock(usermail, true)) {
      return readEmails(usermail, lastEmailDate, true);

    }
  }

  public void DeletemailByid(String usermail, String uuidToDelete,boolean sendmail) {
    synchronized (server.getLock(usermail, true)) {

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
        server.appendToLog("Error deleting email.");
      }

      try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
        for (String line : linesToKeep) {
          bw.write(line);
          bw.newLine();
        }
      } catch (IOException e) {
        server.appendToLog("Error deleting email.");
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
        server.appendToLog("Email deleted successfully.");
      } else {
        server.appendToLog("Error in deleting email.");
      }
    } catch (IOException | ClassNotFoundException e) {
      server.appendToLog("Error in deleting email.");
    }
  }

  public ArrayList<Email> readEmails(String usermail, Date lastEmailDate, boolean sendemail)  {
    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
    ArrayList<Email> emails = new ArrayList<>();
    try {
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
          if (lastEmailDate == null || date.after(lastEmailDate)) {
            Email email = new Email(sender, destinations, subject, content.replace("<--Accapo-->", "\n"), date, id);
            emails.add(email);
          }
        }
      }
      scanner.close();
    } catch (FileNotFoundException | ParseException e) {
      server.appendToLog("Received email list is empty.");
    }
    return emails;
  }

  public  boolean writeswmail(String destination, Email email, boolean sendmail) {
    boolean success = false;

    try {
      String filename = sendmail ? "Server/" + destination + "_sent.txt" : "Server/" + destination + "_received.txt";

      try (FileWriter fileWriter = new FileWriter(filename, true);
           BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
        bufferedWriter.write(email.emailNoEndLine().toString());
        bufferedWriter.newLine();
        success = true;
      } catch (IOException e) {
        server.appendToLog("Error in sending email to " + destination + ".");
      }
    } catch (Exception e) {
      server.appendToLog("Error in sending email to " + destination + ".");
    }
    return success;
  }
}
