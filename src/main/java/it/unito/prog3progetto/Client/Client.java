package it.unito.prog3progetto.Client;

import it.unito.prog3progetto.Lib.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.SocketTimeoutException;

public class Client {
  Socket socket = null;
  ObjectOutputStream outputStream = null;
  ObjectInputStream inputStream = null;
  int id;

  final int MAX_ATTEMPTS = 5;

  /**
   * Costruisce un nuovo client.
   *
   * @param id identificatore numerico, utile solamente per la stampa dei messaggi.
   */
  public Client(int id) {
    this.id = id;
  }

  /**
   * Fa fino a 5 tentativi per comunicare con il server. Dopo ogni tentativo fallito
   * aspetta 1 secondo.
   *
   * @param host l'indirizzo sul quale il server è in ascolto.
   * @param port la porta su cui il server è in ascolto.
   */
  public boolean connectToServer(String host, int port) {
    int attempts = 0;
    boolean success = false;

    while (attempts < MAX_ATTEMPTS && !success) {
      attempts += 1;
      success = tryCommunication(host, port);

      if (!success) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt(); // Ripristina lo stato interrotto
          return false; // Restituisci false se il thread è stato interrotto
        }
      }
    }

    return success;
  }

  /**
   * Tenta di comunicare con il server. Restituisce true se ha successo, false altrimenti
   */
  boolean tryCommunication(String host, int port) {
    try {
      socket = new Socket(host, port);
      outputStream = new ObjectOutputStream(socket.getOutputStream());
      // Dalla documentazione di ObjectOutputStream
      // callers may wish to flush the stream immediately to ensure that constructors for receiving
      // ObjectInputStreams will not block when reading the header.
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

  /**
   * Chiude le connessioni.
   */
  public void closeConnections() {
    if (socket != null) {
      try {
        inputStream.close();
        outputStream.close();
        socket.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Invia le credenziali dell'utente al server e verifica se sono corrette.
   *
   * @param email    l'email dell'utente.
   * @param password la password dell'utente.
   * @return true se le credenziali sono corrette, false altrimenti.
   */

  public boolean sendAndCheckCredentials(String host, int port, String email, String password) {
    if (!connectToServer(host, port)) {
      return false; // Connessione fallita
    }

    try {
      User user = new User(email, password);
      outputStream.writeObject(user);
      outputStream.flush();

      // Imposta un timeout per la lettura della risposta
      socket.setSoTimeout(5000); // Timeout di 5 secondi

      return (boolean) inputStream.readObject(); // Restituisce la risposta del server
    } catch (SocketTimeoutException e) {
      System.out.println("Timeout di connessione");
      return false;
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
      return false;
    } finally {
      closeConnections();
    }
  }
}
