package commandsParsing;

import java.io.IOException;
import java.util.logging.Logger;

import database.DBConnect;
import server.ServerResponse;
import server.Session;

/**
 * This class checks which command client sends and 
 * calls methods from the Command Handler class
 * 
 * @author Martin Holecek
 * 
 */
public class CommandChecker {

	private Session session;
	private CommandHandler commandHandler;	
	private ServerResponse response;
	
	/**
	 * Constructor
	 *  
	 * @param session sends and receives messages from the SMTP Server
	 * @param database connects to the database
	 * @param mailbox is the name of server
	 * @param logger logs error to the file and show error in the console if verbose is enabled 
	 * @param verbose if true then show error in the console
	 */
	public CommandChecker(Session session, DBConnect database, String mailbox, Logger logger, boolean verbose) {
		this.session = session;
		response = new ServerResponse();
		commandHandler = new CommandHandler(session, mailbox, database, verbose);
	}

	/**
	 * This method will check which command was send by the client
	 * 
	 * @param clientMessage is a string text 
	 * (command with one or without argument) 
	 * which client sends to the server
	 * 
	 * @return return false if client message 
	 * causes any error or if client sends quit 
	 * command otherwise return true
	 * 
	 * @throws IOException will appear when client 
	 * unexpectly terminates connection or when 
	 * data streams are not available
	 */
	public boolean parsingMessage(String clientMessage) throws IOException {

		// Check if command line is less then 512 characters
		if (clientMessage.length() > 512) {
			session.write(response.getResponse(500) + ", Error Command line is too long");
			return true;
		}

		// Parse message by spaces
		if(!commandHandler.parseCommand(clientMessage)) {
			// Syntax error in parameters or arguments
			session.write(response.getResponse(501));
			return true;
		}		

		// Check which command user sends
		switch (commandHandler.getCommand()) {
		case "HELO":
			return commandHandler.parseHELO();
		case "MAIL":
			return commandHandler.parseMAIL();
		case "RCPT":
			return commandHandler.parseRCPT();
		case "VRFY":
			return commandHandler.parseVRFY();
		case "EXPN":
			return commandHandler.parseEXPN();
		case "HELP":
			return commandHandler.parseHELP();
		case "DATA":
			return commandHandler.parseDATA();
		case "RSET":
			return commandHandler.parseRSET();
		case "NOOP":
			return commandHandler.parseNOOP();
		case "QUIT":
			return commandHandler.parseQUIT();
		case "SEND":
			return commandHandler.parseSEND();
		case "SOML":
			return commandHandler.parseSOML();
		case "SAML":
			return commandHandler.parseSAML();
		case "TURN":
			return commandHandler.parseTURN();
		default:
			// Syntax error, command unrecognised
			session.write(response.getResponse(500));
			return true;
		}
	}
}
