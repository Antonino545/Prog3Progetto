package it.unito.prog3progetto.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.*;

import javafx.scene.control.TextArea;

public class Server {
	private ServerSocket serverSocket;
	final Map<UUID, String> authenticatedTokens;


	final TextArea textArea; // TextArea per visualizzare l'output

	// Costruttore che accetta una TextArea per visualizzare l'output
	public Server(TextArea textArea) {
		this.textArea = textArea;
		this.authenticatedTokens = new HashMap<>();
  }

	// Metodo per avviare il server e metterlo in ascolto su una porta specifica
	private volatile boolean isRunning = true; // Flag to control the server's running state

	public void listen(int port) {
		try {
			serverSocket = new ServerSocket(port);
			textArea.appendText("Server avviato sulla porta: " + port + ". In attesa di connessioni...\n");

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
