package it.unito.prog3progetto.Client;

import it.unito.prog3progetto.Model.Email;
import it.unito.prog3progetto.Model.User;

import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.*;

public class  Client {
  private Socket socket = null;
  private ObjectOutputStream outputStream = null;
  private ObjectInputStream inputStream = null;
  private final String mail;
  private UUID token;

  private final int MAX_ATTEMPTS = 1  ;
  private final int DEFAULT_TIMEOUT = 5000;

  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  public Client(String mail) {
    this.mail = mail;
  }

  public String getUserMail() {
    return mail;
  }

  public UUID getToken() {
    return token;
  }

  public void setToken(UUID token) {
    this.token = token;
  }

  public boolean connectToServer(String host, int port) {
    Thread connectionThread = new Thread(() -> {
      int attempts = 0;
      boolean success = false;

      while (attempts < MAX_ATTEMPTS && !success) {
        attempts++;
        success = tryCommunication(host, port);

        if (!success) {
          try {
            Thread.sleep(DEFAULT_TIMEOUT);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    });

    connectionThread.start();

    try {
      connectionThread.join(DEFAULT_TIMEOUT);
      return !connectionThread.isAlive();
    } catch (InterruptedException e) {
      e.printStackTrace();
      return false;
    }
  }


  private boolean tryCommunication(String host, int port) {
    try {
      socket = new Socket(host, port);
      outputStream = new ObjectOutputStream(socket.getOutputStream());
      inputStream = new ObjectInputStream(socket.getInputStream());

      outputStream.flush();
      return true;
    } catch (ConnectException e) {
      System.out.println("[ClientModel " + this.mail + "] Server non raggiungibile");
      return false;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }
  public UUID sendAndCheckCredentials(String host, int port, String email, String password) {
    try {
      User user = new User(email, password);
      outputStream.writeObject("LOGIN");
      outputStream.flush();
      socket.setSoTimeout(DEFAULT_TIMEOUT);
      if (inputStream.readObject().equals(true)) {
        System.out.println("Server pronto a ricevere le credenziali");
      }
      outputStream.writeObject(user);
      outputStream.flush();
      socket.setSoTimeout(DEFAULT_TIMEOUT);
      boolean success = (boolean) inputStream.readObject();
      if (success) {
        Object token = inputStream.readObject();
        if (token instanceof UUID) {
          System.out.println("Token Recuperato con successo.");
          return (UUID) token;
        } else {
          System.out.println("Errore durante il recupero del token.");
          return null;
        }
      } else {
        System.out.println("Credenziali errate.");
        return null;
      }
    } catch (Exception e) {
      System.out.println("Timeout di connessione");
      closeConnections();
      return null;
    }

  }

  public ArrayList<Email> receiveEmail(String email, Date lastmail, boolean isSend) {
    try {
      outputStream.writeObject(token);
      outputStream.flush();
      if (isSend) {
        outputStream.writeObject("RECEIVESENDEMAIL");
      } else {
        outputStream.writeObject("RECEIVEEMAIL");
      }
      outputStream.flush();
      socket.setSoTimeout(DEFAULT_TIMEOUT);
      if (inputStream.readObject().equals(true)) {
        System.out.println("Server pronto a ricevere le email");
      }
      outputStream.writeObject(email);
      outputStream.flush();
      outputStream.writeObject(lastmail);
      outputStream.flush();
      socket.setSoTimeout(DEFAULT_TIMEOUT);

      return (ArrayList<Email>) inputStream.readObject();
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
      return new ArrayList<>();
    }
  }

  public boolean SendMail(Email email) {
    try {
      System.out.println("Prova di invio email");
      outputStream.writeObject(token);
      outputStream.flush();
      outputStream.writeObject("SENDMAIL");
      outputStream.flush();
      outputStream.writeObject(email);
      outputStream.flush();
      socket.setSoTimeout(DEFAULT_TIMEOUT);
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

  public boolean logout() {
    try {
      outputStream.writeObject(token);
      outputStream.flush();
      outputStream.writeObject("LOGOUT");
      outputStream.flush();
      if (inputStream.readObject().equals(true)) {
        System.out.println("Server pronto a fare il logout");
      } else {
        System.out.println("Errore durante il logout.");
        return false;
      }
      outputStream.writeObject(token);
      outputStream.flush();
      socket.setSoTimeout(DEFAULT_TIMEOUT);
      boolean success = (boolean) inputStream.readObject();
      if (success) {
        System.out.println("Logout effettuato con successo.");
      } else {
        System.out.println("Errore durante il logout.");
      }
      return success;
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
      return false;
    }
  }

  public boolean CheckEmail(String email) {
    try {
      outputStream.writeObject(token);
      outputStream.flush();
      outputStream.writeObject("CHECKEMAIL");
      outputStream.flush();
      inputStream.readObject();
      outputStream.writeObject(email);
      outputStream.flush();
      socket.setSoTimeout(DEFAULT_TIMEOUT);
      boolean success = (boolean) inputStream.readObject();
      if (success) {
        System.out.println("Email esistente.");
      } else {
        System.out.println("Email non esistente.");
      }
      return success;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public boolean DeleteMail(Email email, boolean isInbox) {
    try {
      outputStream.writeObject(token);
      outputStream.flush();
      if (isInbox) {
        outputStream.writeObject("DELETEMAILRECEIVED");
      } else {
        outputStream.writeObject("DELETEMAILSEND");
      }
      outputStream.flush();
      outputStream.writeObject(email);
      outputStream.flush();
      socket.setSoTimeout(DEFAULT_TIMEOUT);
      boolean success = (boolean) inputStream.readObject();
      if (success) {
        System.out.println("Email Cancellata con successo.");
      } else {
        System.out.println("Errore durante la cancellazione dell'email.");
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
}
