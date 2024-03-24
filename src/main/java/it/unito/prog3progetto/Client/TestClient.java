package it.unito.prog3progetto.Client;

import it.unito.prog3progetto.Lib.Email;

import java.util.ArrayList;
import java.util.Date;

public class TestClient {
  public static void main(String[] args) {
    // Creazione dei client con le rispettive credenziali
    Client[] clients = {
            new Client("luca.verdi@progmail.com"),
            new Client("stefano.bianchi@progmail.com"),
            new Client("marco.gialli@progmail.com")
    };

    String host = "127.0.0.1";
    int port = 4445;

    // Connessione e invio delle email per ogni client
    for (Client client : clients) {
      Thread thread = new Thread(() -> {
        if (client.connectToServer(host, port)) {
          System.out.println("Connessione al server riuscita per " + client.getUserId());
          if (client.sendAndCheckCredentials(host, port, client.getUserId(), "password")) {
            System.out.println("Credenziali corrette per " + client.getUserId());
            for (int i = 0; i < 3; i++) {
              ArrayList<String> destinations = new ArrayList<>();
              destinations.add("andrea.rossi@progmail.com");
              if(client.connectToServer(host, port))
                System.out.println(client.SendMail(host, port, new Email(client.getUserId(), destinations, "Oggetto", "Contenuto", Date.from(java.time.Instant.now()))));
            }

          } else {
            System.out.println("Credenziali incorrette per " + client.getUserId());
          }
        } else {
          System.out.println("Connessione al server non riuscita per " + client.getUserId());
        }
      });
      thread.start();
    }
  }
}
