package it.unito.prog3progetto.Server;

import it.unito.prog3progetto.Lib.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

	Socket socket = null;
	ObjectInputStream inStream = null;
	ObjectOutputStream outStream = null;
	private final String DATABASE_FILE = "database.txt";

	public void listen(int port) {
		try {
			ServerSocket serverSocket = new ServerSocket(port);

			while (true) {
				serveClient(serverSocket);
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

	private void serveClient(ServerSocket serverSocket) {
		try {
			openStreams(serverSocket);

			User user = (User) inStream.readObject();

			boolean isAuthenticated = authenticateUser(user);

			outStream.writeObject(isAuthenticated);
			outStream.flush();

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			closeStreams();
		}
	}

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

	private void openStreams(ServerSocket serverSocket) throws IOException {
		socket = serverSocket.accept();
		System.out.println("Server Connesso");

		inStream = new ObjectInputStream(socket.getInputStream());
		outStream = new ObjectOutputStream(socket.getOutputStream());
		outStream.flush();
	}

	private boolean authenticateUser(User user) {
		// Leggi il database di credenziali da file
		List<String> database = readDatabaseFromFile();

		// Verifica se le credenziali dell'utente sono presenti nel database
		for (String entry : database) {
			String[] parts = entry.split(",");
			if (parts.length == 2 && parts[0].trim().equals(user.getEmail()) && parts[1].trim().equals(user.getPassword())) {
				return true;
			}
		}
		return false;
	}

	private static List<String> readDatabaseFromFile() {
		List<String> database = new ArrayList<>();

		try (BufferedReader br = new BufferedReader(new FileReader("/Users/antonino/Documents/Project/Unito/Prog3Progetto/src/main/resources/it/unito/prog3progetto/Server/database.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				database.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return database;
	}

	public static void main(String[] args) {
		Server server = new Server();
		server.listen(4445);
		System.out.println("Server in ascolto sulla porta 4445");
	}
}
