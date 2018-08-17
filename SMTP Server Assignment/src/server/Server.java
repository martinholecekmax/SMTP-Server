package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * This class will initiate and start SMTP Server
 * 
 * @author Martin Holecek
 * 
 */
public class Server {

	private ServerSocket serverSocket = null;
	private ArrayList<Session> clients = null;
	private int port = 50000;
	private boolean running = true;
	private boolean verbose = true;
	private static Scanner scanner = new Scanner(System.in);
	private final static Logger lOGGER = Logger.getLogger("ServerHandler");

	/**
	 * Constructor
	 * 
	 * @param port number must be between 2048 and 65535 
	 * @param verbose if true error are visible otherwise errors are saved to the file only
	 */
	public Server(int port, boolean verbose) {
		if (port > 2048 && port < 65535) {
			this.port = port;
		}
		this.verbose = verbose;
	}

	/**
	 * This method will initialise logger and then for each client which 
	 * will try to connect, it will create new session and start a thread
	 */
	public void startServer() {
		
		// Initialise logger
		InitializeLogger();
		
		try {
			serverSocket = new ServerSocket(port);
			clients = new ArrayList<>();


			// This message has to be there to allow user to see which port is server running on
			System.out.println("Server is connected to the port: " + port);
			
			while (running) {
				// Create socket
				Socket socket = serverSocket.accept();

				// Create new session (socket and data streams)
				Session session = new Session(socket, lOGGER);
				clients.add(session);

				ServerHandler serverHandler = new ServerHandler(clients, session, lOGGER, verbose);
				Thread serverThread = new Thread(serverHandler);
				serverThread.start();
			}
		} catch (IOException e) {
			lOGGER.log(Level.SEVERE, "Input Error", e);
		}
	}

	/**
	 * This method will initialise logger file and if the verbose is set
	 * then it will also add console handler to print logs into the console.
	 * 
	 * @throws SecurityException is when the file handler fails to create file
	 */
	private void InitializeLogger(){
		LogManager.getLogManager().reset();
		lOGGER.setLevel(Level.ALL);

		// Print logger messages to the file
		try {
			FileHandler fileHandler = new FileHandler("logger.log");
			fileHandler.setLevel(Level.ALL);
			lOGGER.addHandler(fileHandler);
		} catch (IOException | SecurityException ex) {
			lOGGER.log(Level.SEVERE, "File logger not working !!!", ex);

			// If file handler fail to load log messages in the console
			ConsoleHandler consoleHandler = new ConsoleHandler();
			consoleHandler.setLevel(Level.SEVERE);
			lOGGER.addHandler(consoleHandler);
		}

		if (verbose) {
			// Print logger messages in the console
			ConsoleHandler consoleHandler = new ConsoleHandler();
			consoleHandler.setLevel(Level.SEVERE);
			lOGGER.addHandler(consoleHandler);
		}
	}

	/**
	 * Start point of the program
	 * 
	 * @param args the command line arguments are not used
	 */
	public static void main(String[] args) {
		
		// Print banner
		printBanner();
		
		boolean verbose = false;
		int port = 0;
		
		// Check if user inputs number as a port
		port = getPortNumber();

		// Check if user will see error messages
		verbose = getVerbose();
				
		Server server = new Server(port, verbose);
		server.startServer();
	}

	/**
	 * Print ASCII ART Banner to the console
	 */
	private static void printBanner() {
		System.out.println("");
		System.out.println("  (       *            (      (         (                 (     ");
		System.out.println("  )\\ )  (  `     *   ) )\\ )   )\\ )      )\\ )              )\\ )  ");
		System.out.println(" (()/(  )\\))(  ` )  /((()/(  (()/( (   (()/( (   (   (   (()/(  ");
		System.out.println("  /(_))((_)()\\  ( )(_))/(_))  /(_)))\\   /(_)))\\  )\\  )\\   /(_)) ");
		System.out.println(" (_))  (_()((_)(_(_())(_))   (_)) ((_) (_)) ((_)((_)((_) (_))   ");
		System.out.println(" / __| |  \\/  ||_   _|| _ \\  / __|| __|| _ \\\\ \\ / / | __|| _ \\  ");
		System.out.println(" \\__ \\ | |\\/| |  | |  |  _/  \\__ \\| _| |   / \\ V /  | _| |   /  ");
		System.out.println(" |___/ |_|  |_|  |_|  |_|    |___/|___||_|_\\  \\_/   |___||_|_\\  ");
		System.out.println("");
	}

	/**
	 * User can set if he wants to see error messages.
	 * Default verbose is true.
	 * 
	 * @return true if user sets verbose
	 */
	private static boolean getVerbose() {
		// Ask user if he wants verbose
		System.out.println("\nDo you want messages to be verbose? (y or n, default = y): ");
		
		// Get input from user
		String input = scanner.nextLine();
		
		// Set verbose
		if (input.equalsIgnoreCase("N")) {
			System.out.println("Verbose is disabled.");
			return false;
		} else if (input.equalsIgnoreCase("Y")) {
			System.out.println("Verbose is enabled.");
			return true;
		} else {
			System.out.println("\nSorry, Wrong answer, Verbose is enabled.");
			return true;
		}
	}

	/**
	 * Get port number from user input
	 * 
	 * @return port number
	 */
	private static int getPortNumber() {
		// Initialise scanner and port number
		int port = 0;
		
		// Check if user enters valid port number
		boolean validPort = true;
		do {
			
		// Ask user to enter the port number
		System.out.println("Please Enter Port Number: ");

		// Get port number from user input
		String input = scanner.nextLine();

			try {
				// Try to parse integer
				port = Integer.parseInt(input);
				
				// Port number must be between 2048 and 65535 
				if (port > 2048 && port < 65535) {
					validPort = false;
				} else {
					System.out.println("Port number must be between 2048 and 65535");
				}
			} catch (Exception e) {
				// port is not valid port number
				System.out.println("\"" + input + "\" is not valid Port Number.");
				System.out.println("Port must be a number!");
			}
		} while (validPort);
		
		return port;
	}
}