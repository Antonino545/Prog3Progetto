package it.unito.prog3progetto.Client;
import it.unito.prog3progetto.Lib.Email;
import it.unito.prog3progetto.Lib.User;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;

public class Client {
  private Socket socket = null;
  private ObjectOutputStream outputStream = null;
  private ObjectInputStream inputStream = null;
  private final String id;

  private final int MAX_ATTEMPTS = 3;

  public Client(String id) {
    this.id = id;
  }

  String getUserId() {
    return id;
  }

  public boolean connectToServer(String host, int port) {
    int attempts = 0;
    boolean success = false;

    while (attempts < MAX_ATTEMPTS && !success) {
      attempts++;
      success = tryCommunication(host, port);

      if (success) {
        continue;
      }

      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    return success;
  }

  private boolean tryCommunication(String host, int port) {
    try {
      socket = new Socket(host, port);
      outputStream = new ObjectOutputStream(socket.getOutputStream());
      outputStream.flush();
      inputStream = new ObjectInputStream(socket.getInputStream());
      return true;
    } catch (ConnectException e) {
      System.out.println("[Client " + this.id + "] Server non raggiungibile");
      return false;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }


  public boolean sendAndCheckCredentials(String host, int port, String email, String password) {
    try {
      User user = new User(email, password);
      outputStream.writeObject("LOGIN");
      outputStream.flush();
      socket.setSoTimeout(5000);
      if(inputStream.readObject().equals(true)){
        System.out.println("Server pronto a ricevere le credenziali");
      }
      outputStream.writeObject(user);
      outputStream.flush();
      socket.setSoTimeout(5000);
      return (boolean) inputStream.readObject();
    } catch (SocketTimeoutException e) {
      System.out.println("Timeout di connessione");
      closeConnections();
      return false;
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
      closeConnections();
      return false;
    }
  }
  public ArrayList<Email> receiveEmail(String host, int port, String email, Date lastmail) {
    try {
      outputStream.writeObject("RECEIVEEMAIL");
      outputStream.flush();
      socket.setSoTimeout(5000);
      if(inputStream.readObject().equals(true)){
        System.out.println("Server pronto a ricevere le email");
      }
      outputStream.writeObject(email);
      outputStream.flush();
      outputStream.writeObject(lastmail);
      outputStream.flush();
      socket.setSoTimeout(5000);
      return (ArrayList<Email>) inputStream.readObject();
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
      return new ArrayList<>();
    }
  }

  public boolean SendMail(String host, int port, Email email) {
    try {
      outputStream.writeObject("SENDMAIL");
      outputStream.flush();
      outputStream.writeObject(email);
      outputStream.flush();
      socket.setSoTimeout(5000);
      boolean success = (boolean) inputStream.readObject();
      if (success) {
        System.out.println("Email inviata con successo.");
      } else {
        System.out.println("Errore durante l'invio dell'email.");
      }
      return success;
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
      return false;
    }
  }


  public void closeConnections() {
    try {
      if (outputStream != null) {
        outputStream.writeObject("CLOSE_CONNECTION");
        outputStream.flush();
      }
      if (inputStream != null)
        inputStream.close();
      if (outputStream != null)
        outputStream.close();
      if (socket != null)
        socket.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    Client client = new Client("mario.rossi@progmail.com");
    String host= "127.0.0.1";
    int port= 4445;
    if(client.connectToServer(host, port)){
      System.out.println("Connessione al server riuscita");
      if(client.sendAndCheckCredentials(host, port, "mario.rossi@progmail.com", "password")){
        System.out.println("Credenziali corrette");
        for(int i = 0; i < 3; i++){
          client.connectToServer(host, port);
          ArrayList<String> destinations = new ArrayList<>();
          destinations.add("mario.rossi@progmail.com");
          destinations.add("mario.bianchi@progmail.com");
          System.out.println(client.SendMail(host, port, new Email("mario.rossi@progmail.com", destinations, "Oggetto", "Contenuto", Date.from(java.time.Instant.now()))));

        }
  }}else{
      System.out.println("Connessione al server non riuscita");
    }
  }
}