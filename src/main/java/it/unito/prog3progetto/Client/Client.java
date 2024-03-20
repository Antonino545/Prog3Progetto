package it.unito.prog3progetto.Client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Client {
  Socket socket = null;
  ObjectOutputStream outputStream = null;
  ObjectInputStream inputStream = null;
  int id;

  final int MAX_ATTEMPTS = 5;

  /**
   * Costruisce un nuovo client.
   * @param id identificatore numerico, utile solamente per la stampa dei messaggi.
   */
  public Client(int id) {
    this.id = id;
  }

  /**
   * Fa fino a 5 tentativi per comunicare con il server. Dopo ogni tentativo fallito
   * aspetta 1 secondo.
   * @param host l'indirizzo sul quale il server è in ascolto.
   * @param port la porta su cui il server è in ascolto.
   */
  public boolean communicate(String host, int port) {
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


  // Tenta di comunicare con il server. Restituisce true se ha successo, false altrimenti
  private boolean tryCommunication(String host, int port) {
    try {
      connectToServer(host, port);
      sendUserCredentials("admin", "admin");
      return CheckCredentials();
    } catch (ConnectException e) {
      System.out.println("[Client "+ this.id +"] Server non raggiungibile");
      return false;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    } finally {
      closeConnections();
    }
  }

  private void closeConnections() {
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

  public void sendUserCredentials(String email, String password) {
    try {
      User user = new User(email, password);
      outputStream.writeObject(user);
      outputStream.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  public boolean CheckCredentials() {
    try {
      return (boolean) inputStream.readObject();
    } catch (IOException | ClassNotFoundException e) {
      e.printStackTrace();
      return false;
    }
  }
  private void connectToServer(String host, int port) throws IOException {
    socket = new Socket(host, port);
    outputStream = new ObjectOutputStream(socket.getOutputStream());

    // Dalla documentazione di ObjectOutputStream
    // callers may wish to flush the stream immediately to ensure that constructors for receiving
    // ObjectInputStreams will not block when reading the header.
    outputStream.flush();

    inputStream = new ObjectInputStream(socket.getInputStream());

    System.out.println("[Client "+ this.id + "] Connesso");
  }
}
