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

				if (clientObject.equals("LOGIN")) {
					handleLoginRequest();
				} else if (clientObject.equals("SENDMAIL")) {
					handleSendMailRequest();
				} else if (clientObject.equals("RECEIVEEMAIL")) {
					handleReceiveEmailRequest();
				}

			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				// Handle other exceptions
			} finally {
				closeStreams();
			}
		}

		private void handleLoginRequest() {
			try {
				outStream.writeObject(true);
				outStream.flush();

				// Wait for the user object
				Object userObject = inStream.readObject();

				if (userObject instanceof User) {
					User user = (User) userObject;
					boolean isAuthenticated = authenticateUser(user);

					outStream.writeObject(isAuthenticated);
					outStream.flush();

					if (isAuthenticated) {
						textArea.appendText("Utente " + user.getEmail() + " autenticato con successo.\n");
					} else {
						textArea.appendText("Autenticazione fallita per l'utente " + user.getEmail() + ".\n");
					}
				} else {
					textArea.appendText("Errore: oggetto utente non valido.\n");
				}
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				// Handle login request exception
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
				outStream.writeObject(true);
				outStream.flush();

				Object userMailObject = inStream.readObject();
					String userMail = (String) userMailObject;
					ArrayList<Email> mails = receiveEmail(userMail);
					outStream.writeObject(mails);
					outStream.flush();

			} catch (IOException e) {
				e.printStackTrace();
				// Handle receive email request exception
			} catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
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
				System.out.println("Sending email to " + destination + "...");
				// Send email to the recipient
				Platform.runLater(() -> textArea.appendText("Email sent to " + destination + ".\n"));
				try {
					// Check if the file already exists
					Path filePath = Paths.get(destination + ".txt");
					if (Files.exists(filePath)) {
						// If the file exists, append to it
						FileWriter fileWriter = new FileWriter(filePath.toString(), true);
						BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
						bufferedWriter.write(email.toString());
						bufferedWriter.close();
						System.out.println("Content appended to the file successfully!");
					} else {
						// If the file doesn't exist, create it
						FileWriter fileWriter = new FileWriter(filePath.toString());
						BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
						bufferedWriter.write(email.toString());
						bufferedWriter.close();
						System.out.println("File created successfully!");
					}
					success = true; // L'invio dell'email è riuscito per questo destinatario
				} catch (IOException e) {
					System.out.println("An error occurred while processing the file.");
					e.printStackTrace();
					success = false; // L'invio dell'email non è riuscito per questo destinatario
				}
			}

			return success; // Restituisci true solo se l'email è stata inviata con successo a tutti i destinatari
		}
	}
		private ArrayList<Email> receiveEmail(String usermail){
			SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
			ArrayList<Email> emails = new ArrayList<>();
			try {
				File file = new File(usermail + ".txt");
				Scanner scanner = new Scanner(file);

				while (scanner.hasNextLine()) {

					String line = scanner.nextLine();
					System.out.println(line);
					String[] parts = line.split(" , ");
					if (parts.length >= 6) {
						String sender = parts[0];
						String destinationsString = parts[1];
						String subject = parts[2];
						String content = parts[3];
						String dateString = parts[4];
						String id = parts[5];

						// Extracting destinations from destinationsString
						String[] destinationsArray = destinationsString.substring(1, destinationsString.length() - 1).split(", ");
            ArrayList<String> destinations = new ArrayList<>(Arrays.asList(destinationsArray));

						// Parsing date
						Date date = dateFormat.parse(dateString);

						// Creating Email object
						Email email = new Email(sender, destinations, subject, content.replace("<--Accapo-->","\n"), date, Integer.parseInt(id));
						emails.add(email);
					}
				}
				scanner.close();
			} catch (FileNotFoundException | ParseException e) {
				return emails;
			}
			return emails;

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

	public static void main(String[] args) {
		Server server = new Server(null);
		System.out.println(server.receiveEmail("mario.rossi222@progmail.com"	));
	}
	}
