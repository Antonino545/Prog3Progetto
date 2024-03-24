package it.unito.prog3progetto.Client;

import it.unito.prog3progetto.Lib.Email;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

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

    // Testo randomico multiriga per il contenuto dell'email
    String randomContent = generateRandomMultilineText();

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
                System.out.println(client.SendMail(host, port, new Email(client.getUserId(), destinations, "Oggetto", randomContent, Date.from(java.time.Instant.now()))));
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

  // Metodo per generare testo randomico multiriga
  private static String generateRandomMultilineText() {
    Random random = new Random();
    StringBuilder sb = new StringBuilder();
    int numLines = random.nextInt(10) + 3; // generiamo da 3 a 12 righe di testo
    for (int i = 0; i < numLines; i++) {
      int lineLength = random.nextInt(50) + 20; // lunghezza della riga da 20 a 70 caratteri
      for (int j = 0; j < lineLength; j++) {
        char c = (char) (random.nextInt(26) + 'a');
        sb.append(c);
      }
      sb.append("\n");
    }
    return sb.toString();
  }
}
