package it.unito.prog3progetto.Server;

import it.unito.prog3progetto.Lib.Email;
import it.unito.prog3progetto.Lib.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javafx.application.Platform;
import javafx.scene.control.TextArea;

public class Server {
	private ServerSocket serverSocket;

	private final TextArea textArea; // TextArea per visualizzare l'output

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
				ClientHandler clientHandler = new ClientHandler(socket);
				Thread thread = new Thread(clientHandler);
				thread.start();
			}

		} catch (IOException e) {
			textArea.appendText("Errore nell'avvio del server sulla porta "+ port+ ".\n");

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

	// Classe interna per gestire la comunicazione con un singolo client
	private class ClientHandler implements Runnable {
		private final Socket socket;
		private ObjectInputStream inStream;
		private ObjectOutputStream outStream;

		public ClientHandler(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			try {
				openStreams();

				Object clientObject = inStream.readObject();

				switch (clientObject.toString()) {
					case "LOGIN":
						Platform.runLater(() -> textArea.appendText("Richiesta di autenticazione ricevuta.\n"));
						handleLoginRequest();
						break;
					case "SENDMAIL":
						Platform.runLater(() -> textArea.appendText("Richiesta di invio email ricevuta.\n"));
						handleSendMailRequest();
						break;
					case "RECEIVEEMAIL":
						Platform.runLater(() -> textArea.appendText("Richiesta di ricezione email ricevuta.\n"));
						handleReceiveEmailRequest();
						break;
					case "DELETEMAIL":
						Platform.runLater(() -> textArea.appendText("Richiesta di cancellazione email ricevuta.\n"));
						handleDeleteEmailRequest();
						break;
					default:
						outStream.writeObject(false);
						outStream.flush();
						break;
				}

			} catch (IOException | ClassNotFoundException e) {

				Platform.runLater(() -> textArea.appendText("Errore nella comunicazione con il client.\n"));
			} finally {
				closeStreams();
			}
		}

		private void handleLoginRequest() {
			Object userObject = null;
			try {
				outStream.writeObject(true);
				outStream.flush();

				// Wait for the user object
				 userObject = inStream.readObject();

				if (userObject instanceof User) {
					User user = (User) userObject;
					boolean isAuthenticated = authenticateUser(user);

					outStream.writeObject(isAuthenticated);
					outStream.flush();

					if (isAuthenticated) {
						Platform.runLater(() -> textArea.appendText("Authentication successful for user " + user.getEmail() + ".\n"));
					} else {
						Platform.runLater(() -> textArea.appendText("Authentication failed for user " + user.getEmail() + ".\n"));
								}
				} else {
					Platform.runLater(() -> textArea.appendText("Error in authenticating user.\n"));
				}
			} catch (IOException | ClassNotFoundException e) {
				User user = (User) userObject;
				Platform.runLater(() -> textArea.appendText("Authentication error for user " + user.getEmail() + ".\n"));
			}
		}

		private void handleSendMailRequest() {
			try {
				outStream.writeObject(true);
				outStream.flush();

				Object mailObject = inStream.readObject();
				if (mailObject instanceof Email) {
					Email email = (Email) mailObject;
					boolean isSent = sendMail(email);
					outStream.writeObject(isSent);
					outStream.flush();
				} else {
					Platform.runLater(() -> textArea.appendText("Error in sending email.\n"));

				}
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				// Handle send mail request exception
			}
		}

		private void handleReceiveEmailRequest() {
			try {
				// Segnala al client che il server è pronto a ricevere la richiesta
				outStream.writeObject(true);
				outStream.flush();

				// Legge il nome dell'utente dal client
				Object userMailObject = inStream.readObject();
				String userMail = (String) userMailObject;

				// Legge la data dell'ultima email ricevuta dal client
				Date lastEmailDate = (Date) inStream.readObject();

				// Ottiene le email dall'utente con una data successiva a quella dell'ultima email ricevuta
				ArrayList<Email> mails = receiveEmail(userMail, lastEmailDate);

				// Invia le email al client
				outStream.writeObject(mails);
				outStream.flush();

			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				// Gestisci l'eccezione
			}
		}


// Other methods...


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
					return true; // Se le credenziali sono corrette, restituisce true
				}
			}
			return false; // Se le credenziali non corrispondono, restituisce false
		}

		private boolean sendMail(Email email) {
			boolean success = false; // Variabile per tenere traccia dello stato di invio dell'email

			for (String destination : email.getDestinations()) {
				Platform.runLater(() -> textArea.appendText("Sending email to " + destination + ".\n"));
				try {
					// Check if the file already exists
					Path filePath = Paths.get(destination + ".txt");
					if (Files.exists(filePath)) {
						// If the file exists, append to it
						FileWriter fileWriter = new FileWriter(filePath.toString(), true);
						BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
						bufferedWriter.write(email.toString());
						bufferedWriter.close();
						Platform.runLater(() -> textArea.appendText("Email add to postbox of " + destination + ".\n"));

					} else {
						FileWriter fileWriter = new FileWriter(filePath.toString());
						BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
						bufferedWriter.write(email.toString());
						bufferedWriter.close();
						Platform.runLater(() -> textArea.appendText("Email don't exist, create new postbox of " + destination + ".\n"));
					}
					success = true; // L'invio dell'email è riuscito per questo destinatario
          Platform.runLater(() -> textArea.appendText("Email sent successfully to " + destination + ".\n"));
        } catch (IOException e) {
					Platform.runLater(() -> textArea.appendText("Error in sending email to " + destination + ".\n"));
					e.printStackTrace();
					success = false; // L'invio dell'email non è riuscito per questo destinatario
				}
			}

			return success; // Restituisci true solo se l'email è stata inviata con successo a tutti i destinatari
		}
		private ArrayList<Email> receiveEmail(String usermail, Date lastEmailDate) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
			ArrayList<Email> emails = new ArrayList<>();
			try {
				File file = new File(usermail + ".txt");
				Scanner scanner = new Scanner(file);

				while (scanner.hasNextLine()) {
					String line = scanner.nextLine();
					String[] parts = line.split(" , ");
					if (parts.length >= 6) {
						String sender = parts[0];
						String destinationsString = parts[1];
						String subject = parts[2];
						String content = parts[3];
						String dateString = parts[4];
						String idString = parts[5];

						// Extracting destinations from destinationsString
						String[] destinationsArray = destinationsString.substring(1, destinationsString.length() - 1).split(", ");
						ArrayList<String> destinations = new ArrayList<>(Arrays.asList(destinationsArray));

						// Parsing date
						Date date = dateFormat.parse(dateString);

						// Parsing ID
						UUID id = UUID.fromString(idString);

						// Se lastEmailDate è null, aggiungi tutte le email senza alcun controllo sulla data
						if (lastEmailDate == null) {
							Email email = new Email(sender, destinations, subject, content.replace("<--Accapo-->","\n"), date, id);
							emails.add(email);
						} else if (date.after(lastEmailDate)) {
							// Aggiungi solo le email con data successiva a lastEmailDate
							Email email = new Email(sender, destinations, subject, content.replace("<--Accapo-->","\n"), date, id);
							emails.add(email);
						}
					}
				}
				Platform.runLater(() -> textArea.appendText("Email send to client.\n"));
				scanner.close();
			} catch (FileNotFoundException | ParseException e) {
				// In caso di eccezione, restituisci l'elenco vuoto
				return emails;
			}
			return emails;
		}
		public static void DeletemailByid(String usermail, String uuidToDelete) {
			List<String> linesToKeep = new ArrayList<>();

			try (BufferedReader br = new BufferedReader(new FileReader(usermail + ".txt"))) {
				String line;
				while ((line = br.readLine()) != null) {
					if (!line.contains(uuidToDelete)) {
						linesToKeep.add(line);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			try (BufferedWriter bw = new BufferedWriter(new FileWriter(usermail + ".txt"))) {
				for (String line : linesToKeep) {
					bw.write(line);
					bw.newLine();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		private void handleDeleteEmailRequest() {
			try {
				outStream.writeObject(true);
				outStream.flush();
				Object mailObject = inStream.readObject();
				if (mailObject instanceof Email email) {
          for(String destination : email.getDestinations()){
						DeletemailByid(destination,email.getId().toString());
					}
					outStream.writeObject(true);
					outStream.flush();
					Platform.runLater(() -> textArea.appendText("Email deleted successfully.\n"));
				} else {
					Platform.runLater(() -> textArea.appendText("Error in deleting email.\n"));
				}
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
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
