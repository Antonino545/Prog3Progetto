package it.unito.prog3progetto.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javafx.scene.control.TextArea;

public class ServerModel {
	private ServerSocket serverSocket;
	final Map<UUID, String> authenticatedTokens;


	final TextArea textArea; // TextArea per visualizzare l'output

	// Costruttore che accetta una TextArea per visualizzare l'output
	public ServerModel(TextArea textArea) {
		this.textArea = textArea;
		this.authenticatedTokens = new ConcurrentHashMap<>();
  }

	// Metodo per avviare il server e metterlo in ascolto su una porta specifica
	private volatile boolean isRunning = true; // Flag to control the server's running state

	public void listen(int port) {
		try {
			serverSocket = new ServerSocket(port);
			textArea.appendText("Server avviato sulla porta: " + port + ". In attesa di connessioni...\n");

			loadAuthenticatedTokensFromFile(); // Carica i token dal file al avvio del server

			while (isRunning) {
				Socket socket = serverSocket.accept(); // Accetta connessioni dai client
				ClientHandler clientHandler = new ClientHandler(this, socket);
				Thread thread = new Thread(clientHandler);
				thread.start();
			}

		} catch (IOException e) {
			textArea.appendText("Errore nell'avvio del server sulla porta " + port + ".\n");

		} finally {
			try {
				if (serverSocket != null)
					serverSocket.close();
				textArea.appendText("Server chiuso.\n");

			} catch (IOException e) {
				textArea.appendText("Errore nella chiusura del server.\n");
			}
		}
	}

	private void loadAuthenticatedTokensFromFile() {
		try (BufferedReader reader = new BufferedReader(new FileReader("Server/tokens.txt"))) {
			String line;
			Map<String, Integer> emailSessionCount = new HashMap<>(); // Mappa per tenere traccia del numero di sessioni per ogni email
			List<String> linesToRemove = new ArrayList<>(); // Lista per tenere traccia delle righe da rimuovere dal file
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(",");
				if (parts.length == 2) {
					UUID token = UUID.fromString(parts[0]);
					String email = parts[1];

					// Controlla se l'email ha già raggiunto il limite di sessioni
					int sessionCount = emailSessionCount.getOrDefault(email, 0);
					if (sessionCount >= 5) {
						// Se ha raggiunto il limite, aggiungi la riga alla lista delle righe da rimuovere
						linesToRemove.add(line);
						continue; // Passa alla riga successiva
					}

					// Aggiungi il token all'elenco dei token autenticati
					authenticatedTokens.put(token, email);

					// Aggiorna il conteggio delle sessioni per l'email corrente
					emailSessionCount.put(email, sessionCount + 1);
				}
			}

			// Rimuovi le righe obsolete dal file
			removeLinesFromFile("Server/tokens.txt", linesToRemove);

		} catch (IOException e) {
			// Se il file non esiste o ci sono altri errori di lettura, semplicemente non carichiamo i token.
			// Questo può essere gestito diversamente a seconda dei requisiti.
			textArea.appendText("Impossibile caricare i token degli utenti dal file.\n");
		}
	}
	private void removeLinesFromFile(String filePath, List<String> linesToRemove) {
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			List<String> lines = new ArrayList<>();
			String line;
			while ((line = reader.readLine()) != null) {
				// Aggiungi tutte le righe tranne quelle da rimuovere
				if (!linesToRemove.contains(line)) {
					lines.add(line);
				}
			}

			// Sovrascrivi il file con le righe aggiornate
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
				for (String l : lines) {
					writer.write(l);
					writer.newLine();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	List<String> readDatabaseFromFile() {
			List<String> database = new ArrayList<>();

			try (BufferedReader br = new BufferedReader(new FileReader(Objects.requireNonNull(getClass().getResource("credentials.txt")).getFile()))) {
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
