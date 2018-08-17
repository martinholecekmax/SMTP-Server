package server;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import commandsParsing.CommandChecker;
import database.DBConnect;

/**
 * This class initialise server and handles messages from the client
 * 
 * @author Martin Holecek
 *
 */
public class ServerHandler implements Runnable {

	private ArrayList<Session> clientsList;
	private CommandChecker commandParser;
	private DBConnect databaseConnection;
	private Session session;
	private String mailbox = "martin.holecek@derby.ac.uk";
	private Logger logger;
	private boolean databaseReady;
	private boolean verbose;

	/**
	 * Constructor
	 * 
	 * @param clientList list of all clients connected to the server
	 * @param session which handles sockets and data streams
	 * @param logger logs error to the file and show error in the console if verbose is enabled 
	 * @param verbose show error in the console
	 */
	public ServerHandler(ArrayList<Session> clientList, Session session, Logger logger, boolean verbose) {
		this.session = session;
		this.clientsList = clientList;
		this.verbose = verbose;
		this.logger = logger;
		
		databaseReady = true;
	}

	/**
	 * Override method which starts while a thread is created.
	 * This method will check if database is available, send handshake
	 * and then parse each command from the client.
	 */
	public void run() {

		String clientMessage;

		// Greeting message
		if (verbose) {
			System.out.println("Client Connected ...");
		}
		
		// Connect to the database
		try {
			databaseConnection = new DBConnect(verbose);
			commandParser = new CommandChecker(session, databaseConnection, mailbox, logger, verbose);
		} catch (SQLException e) {
			// Connection to the database failed
			logger.log(Level.SEVERE, "Error database connection failes", e);
			databaseReady = false;
		}
		
		// Server Handshake
		try {
			if (databaseReady) {
				session.write("220 " + mailbox + " SMTP MTA running");
			} else {
				session.write("421 " + mailbox + " not available, closing connection");
				//session.close();
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error establishing connection", e);
		}

		while (databaseReady) {
			try {
				clientMessage = session.read();
				
				// Print message sended by client
				if (verbose) {
					System.out.println("Client Sends --> " + clientMessage);
				}
				
				if (!commandParser.parsingMessage(clientMessage)) {
					databaseReady = false;
				}
			} catch (SocketTimeoutException ex) {
				try {
					// 221 closing connection time out
					logger.log(Level.SEVERE, "Error timeout exceeded", ex);
					session.write("221 " + mailbox + " SMTP MTA closing connection time out exceeded");
				} catch (IOException e) {
					logger.log(Level.SEVERE, "Error timeout exceeded", ex);
				}
				databaseReady = false;
			} catch (IOException ex) {
				logger.log(Level.SEVERE, "Client terminated connection! ", ex);
				databaseReady = false;
			}
		} 
		
		// Close connection
		exit();
	}

	/**
	 * Closing of the socket and removing the client from the list
	 */
	private void exit() {
		// Close socket and data stream 
		session.close();
		
		// Remove client from the list
		clientsList.remove(session);
		
		// Print message to the user
		if (verbose) {
			System.out.println("Client close connection.");
		}
	}
}
