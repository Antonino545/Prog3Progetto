package it.unito.prog3progetto.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TextArea;

public class ServerModel {
	private ServerSocket serverSocket;// Socket del server
	final ConcurrentHashMap<UUID, String> authenticatedTokens;

	volatile boolean isRunning = true; // Flag to control the server's running state
	TextArea textArea;
	private final StringProperty logText = new SimpleStringProperty(""); // Proprietà osservabile per il testo del log
	public ServerModel(TextArea textArea) {
		this.authenticatedTokens = new ConcurrentHashMap<>();
		textArea.textProperty().bind(logText);
		this.textArea = textArea;
	}

	void appendToLog(String message) {
		Platform.runLater(() -> {
			logText.set(logText.get() + message + "\n");
			textArea.positionCaret(logText.get().length());
		});
	}


	void clearLog() {
		Platform.runLater(() -> logText.set(""));
	}

	public void listen(int port) {
		try {
			serverSocket = new ServerSocket(port);
			// Rimozione dell'aggiunta diretta al textArea, ora utilizzeremo appendToLog
			appendToLog("Server avviato sulla porta: " + port + ". In attesa di connessioni...");
			isRunning = true;
			while (isRunning) {
				Socket socket = serverSocket.accept();
				appendToLog("Client connected: " + socket.getInetAddress().getHostAddress());
				ClientHandler clientHandler = new ClientHandler(this, socket);
				Thread thread = new Thread(clientHandler);
				thread.start();
			}

		} catch (IOException e) {
			if (isRunning) appendToLog("Errore nell'avvio del server sulla porta " + port + ".\n" + e.getMessage() );
		} finally {
			close();
		}
	}

	private void loadAuthenticatedTokensFromFile() {
		try {
			createServerDirectoryIfNotExists(); // Assicura che la cartella "Server" esista

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
				appendToLog("Impossibile caricare i token degli utenti dal file.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createServerDirectoryIfNotExists() throws IOException {
		File serverDirectory = new File("Server");
		if (!serverDirectory.exists()) {
			if (serverDirectory.mkdir()) {
				appendToLog("Cartella 'Server' creata con successo.");
			} else {
				throw new IOException("Impossibile creare la cartella 'Server'.");
			}
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
				appendToLog("Errore nella scrittura del file: " + filePath);
			}
		} catch (IOException e) {
			appendToLog("Errore nella lettura del file: " + filePath);

		}
	}

	List<String> readDatabaseFromFile() {
		List<String> database = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader("Server/credentials.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				database.add(line); // Aggiunge ogni riga del file al database
			}
		} catch (IOException e) {
			appendToLog("Errore nella lettura del file 'credentials.txt'.");
		}
		return database;
	}

	public void close() {
		try {
			isRunning = false;
			if (serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
				appendToLog("Server chiuso.");
			}
		} catch (IOException e) {
			appendToLog("Errore nella chiusura del server socket.");
		}
	}

}
