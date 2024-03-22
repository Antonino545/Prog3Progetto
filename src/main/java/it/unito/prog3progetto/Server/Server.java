package it.unito.prog3progetto.Server;

import it.unito.prog3progetto.Lib.Mail;
import it.unito.prog3progetto.Lib.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class Server {
	private ServerSocket serverSocket;

	private TextArea textArea; // TextArea per visualizzare l'output

	// Costruttore che accetta una TextArea per visualizzare l'output
	public Server(TextArea textArea) {
		this.textArea = textArea;
	}

	// Metodo per avviare il server e metterlo in ascolto su una porta specifica
	public void listen(int port) {
		try {
			serverSocket = new ServerSocket(port);

			textArea.appendText("Server avviato sulla porta:"+ port+ ". In attesa di connessioni...\n");

			while (true) {
				Socket socket = serverSocket.accept(); // Accetta connessioni dai client
				textArea.appendText("Nuova connessione accettata da "+ socket.getInetAddress().getHostAddress()+ ".\n");

				// Avvia un thread per gestire la connessione con il client
				ClientHandler clientHandler = new ClientHandler(socket);
				Thread thread = new Thread(clientHandler);
				thread.start();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (serverSocket != null)
					serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// Classe interna per gestire la comunicazione con un singolo client
	private class ClientHandler implements Runnable {
    private Socket socket;
		private ObjectInputStream inStream;
		private ObjectOutputStream outStream;

		public ClientHandler(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				openStreams(); // Apre gli stream di input e output per comunicare con il client

				Object clientObject = inStream.readObject(); // Legge l'oggetto inviato dal client

				if (clientObject instanceof User) {
					User user = (User) clientObject;
					boolean isAuthenticated = authenticateUser(user); // Autentica l'utente
					outStream.writeObject(isAuthenticated); // Invia al client il risultato dell'autenticazione
					outStream.flush();

					if (isAuthenticated) {
						textArea.appendText("Utente " + user.getEmail() + " autenticato con successo.\n");
					} else {
						textArea.appendText("Autenticazione fallita per l'utente " + user.getEmail() + ".\n");
					}
				}
				else if (clientObject instanceof Mail) {
					Mail mail = (Mail) clientObject;
					boolean isSent = SendMail(mail); // Invia l'email al destinatario
					outStream.writeObject(isSent); // Invia al client il risultato dell'invio dell'email
					outStream.flush();
				}

			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				textArea.appendText("Errore durante la comunicazione con il client.\n"); // Aggiorna l'interfaccia utente con un messaggio di errore
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
				if (socket != null) {
					socket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		// Metodo per aprire gli stream di input e output per comunicare con il client
		private void openStreams() throws IOException {
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
          textArea.appendText("Utente autenticato: "+ user.getEmail()+ ".\n"); // Aggiorna l'interfaccia utente con un messaggio di autenticazione
					return true; // Se le credenziali sono corrette, restituisce true
				}
			}
			textArea.appendText("Autenticazione fallita per l'utente: "+ user.getEmail()+ ".\n"); // Aggiorna l'interfaccia utente con un messaggio di errore
			return false; // Se le credenziali non corrispondono, restituisce false
		}
		private boolean SendMail(Mail mail) {
			// Invia l'email al destinatario
			for(String destination : mail.getDestinations()) {
				// Invia l'email al destinatario
				Platform.runLater(() -> textArea.appendText("Email inviata a "+ destination+ ".\n"));
				try {
					// Create a FileWriter object
					FileWriter fileWriter = new FileWriter(destination + ".txt");

					// Wrap the FileWriter in a BufferedWriter
					BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

					// Write some content into the file
					bufferedWriter.write(mail.toString() + "\n");

					// Close the BufferedWriter
					bufferedWriter.close();

					System.out.println("File created successfully!");
					return true; // Se le credenziali sono corrette, restituisce true

				} catch (IOException e) {
					System.out.println("An error occurred while creating the file.");
					e.printStackTrace();
					return false;
				}
			}
			return false;
		}
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
