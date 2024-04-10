package it.unito.prog3progetto.Client;

import it.unito.prog3progetto.Model.Email;
import it.unito.prog3progetto.Model.User;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class ClientModel {
  private Socket socket = null;
  private ObjectOutputStream outputStream = null;
  private ObjectInputStream inputStream = null;
  private final String email;
  private UUID token;

  private final int DEFAULT_TIMEOUT = 5000;

  public ClientModel(String email) {
    this.email = email;
  }

  public String getEMail() {
    return email;
  }


  public void setToken(UUID token) {
    this.token = token;
  }

  /**
   * Metodo per connettersi al server tramite socket in piu tentativi
   * @param host indirizzo del server
   * @param port porta del server
   * @return true se la connessione è andata a buon fine, false altrimenti
   */
  public boolean connectToServer(String host, int port) {
      int attempts = 0;
      boolean success = false;
    int MAX_ATTEMPTS = 3;// Numero massimo di tentativi di connessione al server
    while (attempts < MAX_ATTEMPTS && !success) {
        attempts++;
        success = tryCommunication(host, port);

        if (!success) {
          try {
            Thread.sleep(DEFAULT_TIMEOUT);
          } catch (InterruptedException e) {
            System.out.println("Errore durante il tentativo di connessione al server");
          }
        }
      }
      return success;
    }

  /**
   * Metodo per provare a comunicare con il server
   * @param host indirizzo del server
   * @param port porta del server
   * @return true se la comunicazione è andata a buon fine, false altrimenti
   */
  private boolean tryCommunication(String host, int port) {
    try {
      socket = new Socket(host, port);
      outputStream = new ObjectOutputStream(socket.getOutputStream());
      inputStream = new ObjectInputStream(socket.getInputStream());

      outputStream.flush();
      return true;
    } catch (IOException e) {
      System.out.println("[ClientModel " + this.email + "] Server non raggiungibile");
      return false;
    }
  }

  /**
   * Metodo per inviare le credenziali al server e ricevere il token
   * @param email email dell'utente
   * @param password password dell'utente
   * @return token dell'utente se le credenziali sono corrette, null altrimenti
   */
  public UUID sendAndCheckCredentials(String email, String password) {
    try {
      User user ;
      outputStream.writeObject("LOGIN");
      outputStream.flush();
      socket.setSoTimeout(DEFAULT_TIMEOUT);
      if (inputStream.readObject().equals(true)) {
        System.out.println("Server pronto a ricevere le credenziali");
        user = new User(email, password);
      }
      else {
        System.out.println("Errore durante il login il server non ha risposto correttamente");
        return null;
      }
      outputStream.writeObject(user);
      outputStream.flush();
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
    } catch (SocketTimeoutException e) {
      System.out.println("Timeout di connessione");
    } catch (IOException | ClassNotFoundException e) {
      System.out.println("Errore durante la comunicazione con il server.");
    } finally {
      closeConnections();
    }
    return null;
  }

  /**
   * Metodo per ricevere le email
   * @param email email dell'utente
   * @param lastmail ultima email ricevuta
   * @param isSend true se si vogliono ricevere le email inviate, false altrimenti
   * @return lista delle email ricevute
   */
  public ArrayList<Email> receiveEmail(String email, Date lastmail, boolean isSend) {
    try {
      outputStream.writeObject(token);
      outputStream.flush();

      outputStream.writeObject(isSend ? "RECEIVESENDEMAIL" : "RECEIVEEMAIL");
      outputStream.flush();

      if (inputStream.readObject().equals(true)) {
        System.out.println("Server pronto a inviare le mail ricevere le email");
      } else {
        System.out.println("Errore durante il recupero delle email il server non ha risposto correttamente");
        return new ArrayList<>();
      }
      outputStream.writeObject(email);
      outputStream.flush();
      outputStream.writeObject(lastmail);
      outputStream.flush();
      Object receivedObject = inputStream.readObject();
      if (receivedObject instanceof ArrayList) {
        System.out.println("Email ricevute con successo.");
        return (ArrayList<Email>) receivedObject;
      } else {
        System.out.println("Il dato ricevuto non è un ArrayList<Email>.");
        return new ArrayList<>();
      }
    } catch (IOException | ClassNotFoundException e) {
      System.out.println("Errore durante la comunicazione con il server. Errore:" + e.getMessage());
      return new ArrayList<>();
    }finally {
      closeConnections();
    }
  }


  /**
   * Metodo per inviare una email
   * @param email email da inviare
   * @return true se l'invio è andato a buon fine, false altrimenti
   */
  public boolean SendMail(Email email) {
    try {
      outputStream.writeObject(token);
      outputStream.flush();
      outputStream.writeObject("SENDMAIL");
      outputStream.flush();
      if (inputStream.readObject().equals(true)) {
        System.out.println("Server pronto a inviare la email");
      } else {
        System.out.println("Errore durante l'invio dell'email server non ha risposto correttamente");
        return false;
      }
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
      System.out.println("Errore durante l'invio della mail .\n Errore:" + e.getMessage());
      return false;
    }finally {
      closeConnections();
    }
  }

  /**
   * Metodo per fare il logout
   * @return true se il logout è andato a buon fine, false altrimenti
   */
  public boolean logout() {
    try {
      outputStream.writeObject(token);
      outputStream.flush();
      outputStream.writeObject("LOGOUT");
      outputStream.flush();
      if (inputStream.readObject().equals(true)) {
        System.out.println("Server pronto a fare il logout");
      } else {
        System.out.println("Errore durante il logout il server non ha risposto correttamente");
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
      System.out.println("Errore durante il logout.\n Errore:" + e.getMessage());
      return false;
    }
    finally {
      closeConnections();
    }
  }

  /**
   * Metodo per controllare se l'email esiste
   * @param email email da controllare
   * @return true se l'email esiste, false altrimenti
   */
  public boolean CheckEmail(String email) {
    try {
      outputStream.writeObject(token);
      outputStream.flush();
      outputStream.writeObject("CHECKEMAIL");
      outputStream.flush();
      if (inputStream.readObject().equals(true)) {
        System.out.println("Server pronto a fare il check dell'email");
      } else {
        System.out.println("Errore durante il check dell'email il server non ha risposto correttamente");
        return false;
      }
      outputStream.writeObject(email);
      outputStream.flush();
      socket.setSoTimeout(DEFAULT_TIMEOUT);
      return (boolean) inputStream.readObject();
    } catch (IOException | ClassNotFoundException e) {
      System.out.println("Errore durante il check dell'email.\n Errore:" + e.getMessage());
      return false;
    } finally {
      closeConnections();
    }
  }

  /**
   * Metodo per cancellare una email
   * @param email email da cancellare
   * @param isInbox true se l'email è nella casella di posta in arrivo, false altrimenti
   * @return true se l'email è stata cancellata, false altrimenti
   */
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
      if (inputStream.readObject().equals(true)) {
        System.out.println("Server pronto a cancellare l'email");
      } else {
        System.out.println("Errore durante la cancellazione dell'email il server non ha risposto correttamente");
        return false;
      }
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
      System.out.println("Errore durante la cancellazione dell'email.\n Errore:" + e.getMessage());
      return false;
    }
    finally {
      closeConnections();
    }
  }

  /**
   * Metodo per chiudere le connessioni
   *
   */
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
      System.out.println("Errore durante la chiusura delle connessioni.+ Errore:" + e.getMessage());
    }
  }
}
