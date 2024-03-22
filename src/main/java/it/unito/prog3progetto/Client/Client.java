package it.unito.prog3progetto.Client;
import it.unito.prog3progetto.Lib.User;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;

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

      if(success) {
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


  public boolean sendAndCheckCredentials(String host, int port, String email, String password) {
    try {
      User user = new User(email, password);
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
    }finally {
      closeConnections();
    }
}
}
