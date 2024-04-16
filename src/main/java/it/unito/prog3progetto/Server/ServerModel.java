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
	// Mappa dei token autenticati uso una ConcurrentHashMap per garantire la sicurezza in caso di accessi concorrenti
	final ConcurrentHashMap<UUID, String> authenticatedTokens;
	// Mappa dei token autenticati con data di creazione  uso una ConcurrentHashMap per garantire la sicurezza in caso di accessi concorrenti
	final ConcurrentHashMap<UUID,Date> tokenCreation ;
	volatile boolean isRunning = true; // Flag to control the server's running state
	TextArea textArea;
	private final StringProperty logText = new SimpleStringProperty(""); // Proprietà osservabile per il testo del log
	public ServerModel(TextArea textArea) {
		this.authenticatedTokens = new ConcurrentHashMap<>();
		this.tokenCreation = new ConcurrentHashMap<>();
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
			loadAuthenticatedTokensFromFile();
			isRunning = true;
			while (isRunning) {
				Socket socket = serverSocket.accept();
				appendToLog("Client connected: " + socket.getInetAddress().getHostAddress());
				ClientHandler clientHandler = new ClientHandler(this, socket);
				clientHandler.database = readDatabaseFromFile();
				Thread thread = new Thread(clientHandler);
				thread.start();
			}

		} catch (IOException e) {
			if (isRunning) appendToLog("Errore nell'avvio del server sulla porta " + port + ".\n" + e.getMessage() );
		} finally {
			close();
		}
	}

	public void loadAuthenticatedTokensFromFile() throws IOException {
		createServerDirectoryIfNotExists(); // Assicura che la cartella "Server" esista
		File tokensFile = new File("Server/tokens.txt");
		if (!tokensFile.exists()) {
			if (!tokensFile.createNewFile()) {
				throw new IOException("Impossibile creare il file 'tokens.txt'.");
			}
		}
		try (BufferedReader reader = new BufferedReader(new FileReader(tokensFile))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split(",");
				if (parts.length == 3) {
					authenticatedTokens.put(UUID.fromString(parts[0]), parts[1]);
					tokenCreation.put(UUID.fromString(parts[0]), new Date(Long.parseLong(parts[2])));
				}
			}
			appendToLog("Caricati " + authenticatedTokens.size() + " token dal file 'tokens.txt'.");
			System.out.println("Caricati " + authenticatedTokens.size() + " token dal file 'tokens.txt'.");
		} catch (IOException e) {
			appendToLog("Errore nella lettura del file 'tokens.txt'.");
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
