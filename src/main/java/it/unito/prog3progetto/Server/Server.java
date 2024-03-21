package it.unito.prog3progetto.Server;

import it.unito.prog3progetto.Lib.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class Server {

	Socket socket = null;
	ObjectInputStream inStream = null;
	ObjectOutputStream outStream = null;
	private final String DATABASE_FILE = "database.txt";
	private TextArea textArea; // TextArea per visualizzare l'output

	// Costruttore che accetta una TextArea per visualizzare l'output
	public Server(TextArea textArea) {
		this.textArea = textArea;
	}

	// Metodo per avviare il server e metterlo in ascolto su una porta specifica
	public void listen(int port) {
		try {
			ServerSocket serverSocket = new ServerSocket(port);

			while (true) {
				serveClient(serverSocket); // Accetta connessioni dai client e gestiscili
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (socket != null)
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}

	// Metodo per gestire la connessione con un client
	private void serveClient(ServerSocket serverSocket) {
		try {
			openStreams(serverSocket); // Apre gli stream di input e output per comunicare con il client

			User user = (User) inStream.readObject(); // Legge l'oggetto User inviato dal client

			boolean isAuthenticated = authenticateUser(user); // Autentica l'utente

			outStream.writeObject(isAuthenticated); // Invia al client il risultato dell'autenticazione
			outStream.flush();

			// Gestisce la richiesta di chiusura della connessione da parte del client
			Object clientMessage = inStream.readObject();
			if (clientMessage instanceof String && clientMessage.equals("CLOSE_CONNECTION")) {
				Platform.runLater(() -> textArea.appendText("Client disconnesso.\n")); // Aggiorna l'interfaccia utente con un messaggio di disconnessione
				closeStreams(); // Chiude gli stream di input e output
			}

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			Platform.runLater(() -> textArea.appendText("Errore durante la comunicazione con il client.\n")); // Aggiorna l'interfaccia utente con un messaggio di errore
		} finally {
			closeStreams(); // Chiude gli stream di input e output in ogni caso
		}
	}

	// Metodo per chiudere gli stream di input e output
	private void closeStreams() {
		try {
			if (inStream != null) {
				inStream.close();
			}

			if (outStream != null) {
				outStream.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Metodo per aprire gli stream di input e output per comunicare con il client
	private void openStreams(ServerSocket serverSocket) throws IOException {
		socket = serverSocket.accept(); // Accetta la connessione con il client
		Platform.runLater(() -> textArea.appendText("Client  Connesso\n")); // Aggiorna l'interfaccia utente con un messaggio di connessione

		inStream = new ObjectInputStream(socket.getInputStream()); // Stream di input per ricevere dati dal client
		outStream = new ObjectOutputStream(socket.getOutputStream()); // Stream di output per inviare dati al client
		outStream.flush(); // Assicura che tutti i dati siano inviati
	}

	// Metodo per autenticare l'utente confrontando le credenziali con un database
	private boolean authenticateUser(User user) {
		// Legge il database di credenziali da file
		List<String> database = readDatabaseFromFile();

		// Verifica se le credenziali dell'utente sono presenti nel database
		for (String entry : database) {
			String[] parts = entry.split(",");
			if (parts.length == 2 && parts[0].trim().equals(user.getEmail()) && parts[1].trim().equals(user.getPassword())) {
				return true; // Se le credenziali sono corrette, restituisce true
			}
		}
		return false; // Se le credenziali non corrispondono, restituisce false
	}

	// Metodo per leggere il database di credenziali da file
	private List<String> readDatabaseFromFile() {
		List<String> database = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader("/Users/antonino/Documents/Project/Unito/Prog3Progetto/src/main/resources/it/unito/prog3progetto/Server/database.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				database.add(line); // Aggiunge ogni riga del file al database
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return database;
	}


}
