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
	ConcurrentHashMap<String,String> database ;
	private ServerSocket serverSocket;// Socket del server
	// Mappa dei token autenticati uso una ConcurrentHashMap per garantire la sicurezza in caso di accessi concorrenti
	final ConcurrentHashMap<UUID, String> authenticatedTokens;
	// Mappa dei token autenticati con data di creazione  uso una ConcurrentHashMap per garantire la sicurezza in caso di accessi concorrenti
	final ConcurrentHashMap<UUID,Date> tokenCreation ;
	volatile boolean isRunning = true; // Flag to control the server's running state
	TextArea textArea;
	private final Map<String, Object> sendMailLocks = new ConcurrentHashMap<>();
	private final Map<String, Object> receiveMailLocks = new ConcurrentHashMap<>();
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
			database= readDatabaseFromFile();
			isRunning = true;
			while (isRunning) {
				Socket socket = serverSocket.accept();
				appendToLog("Client connected: " + socket.getInetAddress().getHostAddress());
				ClientHandler clientHandler = new ClientHandler(this, socket,database);
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

	public ConcurrentHashMap<String,String> readDatabaseFromFile() {
		ConcurrentHashMap<String,String> db = new ConcurrentHashMap<>();

		try (BufferedReader br = new BufferedReader(new FileReader("Server/credentials.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] parts = line.split(","); // Supponendo che il delimitatore sia ":"
				if (parts.length == 2) {
					String userEmail = parts[0].trim();
					String password = parts[1].trim();
					sendMailLocks.computeIfAbsent(userEmail, k -> new Object());
					receiveMailLocks.computeIfAbsent(userEmail, k -> new Object());
					db.put(userEmail, password); // Aggiunge la coppia chiave-valore alla mappa
				} else {
					appendToLog("Linea non valida nel file 'credentials.txt': " + line);
				}
			}
		} catch (IOException e) {
			appendToLog("Errore nella lettura del file 'credentials.txt'.");
		}
		appendToLog("Caricate " + db.size() + " credenziali dal file 'credentials.txt'.");
		return db;
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

	/**
	 * Restituisce la mappa dei token autenticati
	 * @param email Email dell'utente in cui si vuole effettuare l'operazione di scrittura o lettura delle email
	 * @param send Flag per indicare se si tratta del file di invio o di ricezione delle email
	 * @return lock del file indicato
	 */
	public Object getLock(String email, boolean send) {
		return send ? sendMailLocks.get(email) : receiveMailLocks.get(email);
	}

}
