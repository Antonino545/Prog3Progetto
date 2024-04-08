package it.unito.prog3progetto.Client;

import it.unito.prog3progetto.Model.Email;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

public class TestClient {
  public static void main(String[] args) {
    // Creazione dei client con le rispettive credenziali
    Client[] clientModels = {
            new Client("marco.gialli@progmail.com"),
    };

    String host = "127.0.0.1";
    int port = 4445;

    // Testo randomico multiriga per il contenuto dell'email
    String randomContent = generateRandomMultilineText();

    // Connessione e invio delle email per ogni client
    for (Client clientModel : clientModels) {
      Thread thread = new Thread(() -> {
        if (clientModel.connectToServer(host, port)) {
          System.out.println("Connessione al server riuscita per " + clientModel.getUserMail());
          UUID token = clientModel.sendAndCheckCredentials(host, port, clientModel.getUserMail(), "password");
          clientModel.setToken(token);
          if (token != null) {
            System.out.println("Credenziali corrette per " + clientModel.getUserMail());
            if (clientModel.connectToServer(host, port)) {
              clientModel.CheckEmail("stefano.bianchi@progmail.com");
            }

          } else {
            System.out.println("Credenziali incorrette per " + clientModel.getUserMail());
          }
        } else {
          System.out.println("Connessione al server non riuscita per " + clientModel.getUserMail());
        }
      });
      thread.start();
    }
  }

  private static void performThreadOperation(Client clientModel, String host, int port, String randomContent) {
    // Eseguire operazioni specifiche per ogni thread qui
    // Ad esempio, invio di email per alcuni clientModel e altre operazioni per altri
    // Qui, invieremo email solo per il clientModel con l'indirizzo "luca.verdi@progmail.com"

      for (int i = 0; i < 3; i++) {
        ArrayList<String> destinations = new ArrayList<>();
        destinations.add("stefano.bianchi@progmail.com");
        if (clientModel.connectToServer(host, port)) {
          System.out.println(clientModel.SendMail( new Email(clientModel.getUserMail(), destinations, "Ciaoooo son oio", randomContent, Date.from(java.time.Instant.now()))));
        }
      }

  }

  // Metodo di esempio per generare testo casuale multiriga



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
