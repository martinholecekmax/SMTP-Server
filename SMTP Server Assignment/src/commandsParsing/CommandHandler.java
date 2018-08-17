package commandsParsing;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import database.DBConnect;
import database.User;
import server.ServerResponse;
import server.Session;

/**
 * This class handles each command specified in RFC 821
 * 
 * @author Martin Holecek
 * 
 */
public class CommandHandler {

	// Global variables
	private boolean heloCommand;
	private boolean mailCommand;
	private boolean rcptCommand;
	private ServerResponse response;
	private DBConnect database;
	private DataParser dataParser;
	private DomainParser domainParser;
	private PathParser pathParser;
	private Session session;
	private String mailbox;
	private String argument;
	private String command;
	private String forwardPath;
	private ArrayList<String> reversePath;
	private boolean verbose;

	/**
	 *  Get Command parsed from the message client sends
	 *  
	 * @return command is four character string
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * Get argument parsed from the message client sends
	 * 
	 * @return argument (parameters) of the command
	 */
	public String getArgument() {
		return argument;
	}

	/**
	 * Constructor
	 * 
	 * @param session sends and receives messages from the SMTP Server
	 * @param mailbox is domain name of the server
	 * @param database connects to the database
	 */
	public CommandHandler(Session session, String mailbox, DBConnect database, boolean verbose) {
		this.session = session;
		this.mailbox = mailbox;
		this.database = database;
		heloCommand = false;
		mailCommand = false;
		rcptCommand = false;
		reversePath = new ArrayList<>();
		response = new ServerResponse();
		dataParser = new DataParser();
		domainParser = new DomainParser();
		pathParser = new PathParser();
		this.verbose = verbose;
	}

	/**
	 * Parses command by spaces
	 * 
	 * @param message is a string sended by the client which is parsed 
	 * by spaces.
	 * @return true if there is command and argument or return false
	 * if there is not argument or if there is more then one argument
	 */
	public boolean parseCommand(String message) {
		message = message.trim();
		if (message.contains(" ")) {
			String[] text = message.split(" ");
			this.command = text[0].toUpperCase();

			// If more then one argument return syntax error
			if (text.length > 2) {
				return false;
			} 

			argument = text[1];
			return true;
		} 
		else {
			argument = "";
			command = message.toUpperCase();
			return true;
		}
	}

	/**
	 * Handles HELO Command
	 * 
	 * @return always return true, false is not implemented
	 * @throws IOException if data stream are not available
	 */
	public boolean parseHELO() throws IOException {
		// Helo must be called only once to initialise connection
		if (heloCommand) {
			session.write(response.getResponse(503) + ", Error: helo command already initialized");
			return true;
		}

		// Clear all buffers
		clearBuffers();
		
		// Check if domain is in valid format
		if(domainParser.isDomainValid(argument)) {
			session.write(response.getResponse(250));
			heloCommand = true;
		} else {
			session.write(response.getResponse(501) + ", Error: domain parsing failed");
			heloCommand = false;
		}
		return true;
	}

	/**
	 * Handles MAIL Command
	 * 
	 * @return false only when there is error with reading
	 *  from database, otherwise there is true
	 * @throws IOException if data stream are not available
	 */
	public boolean parseMAIL() throws IOException {
		// Check if helo command has been send first
		if (!heloCommand) {
			session.write(response.getResponse(503) + ", Error: send HELO first");
			return true;
		}

		// RFC 821 does not allow nested MAIL commands
		if (mailCommand) {
			session.write(response.getResponse(503) + ", Error: nested MAIL command");
			return true;
		}

		// Must contain "FROM:" syntax
		if (argument.toUpperCase().startsWith("FROM:")) {
			argument = argument.substring(5, argument.length());
		} else {
			session.write(response.getResponse(501) + ", Error: missing \"FROM:\"");
			return true;
		}

		// Path cannot be longer then 256 characters
		if (argument.length() > 256) {
			session.write(response.getResponse(501) + ", Path is too long");
			return true;
		}

		// Argument must start with "<" and end with ">"
		if (!argument.startsWith("<") || !argument.endsWith(">")) {
			session.write(response.getResponse(501) + ", Error: path must contain brackets \"<path>\"");
			return true;
		}

		// Erase brackets ("<", ">")
		argument = argument.substring(1, argument.length() - 1);

		// Check if there is path
		if (argument.isEmpty()) {
			// Does not support empty brackets, however the RFC 821 
			// can support relay and in that case this would be supported
			session.write(response.getResponse(501) + ", Error: path can not be empty");
			return true;
		}

		// Check if path is in valid format
		if(!pathParser.parsePath(argument)) {
			session.write(response.getResponse(501) + ", Error: path parsing failed");
			return true;
		}

		// Valid mailbox, send response and add user to the forward path
		forwardPath = pathParser.getUsername() + "@" + pathParser.getDomain();
		mailCommand = true;
		session.write(response.getResponse(250) + " mail");

		return true;
	}

	/**
	 * Handles RCPT Command
	 * 
	 * @return false only when there is error with reading
	 *  from database, otherwise there is true
	 * @throws IOException if data stream are not available
	 */
	public boolean parseRCPT() throws IOException {
		// Mail command must be send first
		if (!mailCommand) {
			session.write(response.getResponse(503) + ", Error: need MAIL command");
			return true;
		} 

		// If client sends more then 100 recipients
		if (reversePath.size() > 100) {
			session.write(response.getResponse(552));
			return true;
		}
		
		// Must contain "TO:" syntax
		if (argument.toUpperCase().startsWith("TO:")) {
			argument = argument.substring(3, argument.length());
		} else {
			session.write(response.getResponse(501) + ", Error: missing \"FROM:\"");
			return true;
		}

		// Path cannot be longer then 256 characters
		if (argument.length() > 256) {
			session.write(response.getResponse(501) + ", Path is too long");
			return true;
		}

		// Argument must start with "<" and end with ">"
		if (!argument.startsWith("<") || !argument.endsWith(">")) {
			session.write(response.getResponse(501) + ", Error: path must contain brackets \"<path>\"");
			return true;
		}

		// Erase brackets ("<", ">")
		argument = argument.substring(1, argument.length() - 1);

		// Check if there is path
		if (argument.isEmpty()) {
			// Does not support empty brackets, however the RFC 821 
			// can support relay and in that case this would be supported
			session.write(response.getResponse(501) + ", Error: path can not be empty");
			return true;
		}

		// Check if path is in valid format
		if(!pathParser.parsePath(argument)) {
			session.write(response.getResponse(501) + ", Error: path parsing failed");
			return true;
		}

		// Valid recipient, send response and add recipient to the list
		reversePath.add(pathParser.getUsername() + "@" + pathParser.getDomain());
		rcptCommand = true;
		session.write(response.getResponse(250) + " rcpt");
		
		return true;
	}

	/**
	 * Handles Verify Command
	 * 
	 * @return false only when there is error with reading
	 *  from database, otherwise there is true
	 * @throws IOException if data stream are not available
	 */
	public boolean parseVRFY() throws IOException {
		// Argument can not be empty
		if (argument.isEmpty()) {
			session.write(response.getResponse(501) + ", Error: Argument is empty");
			return true;
		}
		
		try {
			// Check if user name exists in database
			if (database.usernameExists(argument)) {
				session.write(response.getResponse(250));
				return true;
			} else {
				session.write(response.getResponse(550) + ", Error: String does not match anything");
				return true;
			}
		} catch (SQLException e) {
			// Database is unavailable, server will close connection
			session.write(response.getResponse(421, mailbox) + ", Error: Database unavailable");
			return true;
		}
	}

	/**
	 * Handles Expand Command
	 * 
	 * @return false only when there is error with reading
	 *  from database, otherwise there is true
	 * @throws IOException if data stream are not available
	 */
	public boolean parseEXPN() throws IOException {
		// Argument can not be empty
		if (argument.isEmpty()) {
			session.write(response.getResponse(501) + ", Error: Argument is empty");
			return true;
		}
		
		try {
			String userList = "";
			ArrayList<User> list = database.getExpandList(argument);

			// There is no such a mail group
			if (list.isEmpty()) {
				session.write(response.getResponse(550) + ", Error: There is not such a mailing list");
				return true;
			}
			
			// Send each username and domain from the list to the client
			for (int i = 0; i < list.size(); i++) {
				// Print mail group to the console
				if (verbose) {
					System.out.println(list.get(i).getUsername() + " " + list.get(i).getDomain());
				}
				
				// Store each user name from the list to the string
				if (i == list.size() - 1) {
					userList += "250 " + list.get(i).getUsername() + " " + list.get(i).getDomain();
				} else {
					userList += "250-" + list.get(i).getUsername() + " " + list.get(i).getDomain() + "\r\n";
				}
			}		
			
			// Send user list to the client
			session.write(userList);
			
			return true;
			
		} catch (SQLException e) {
			// if mail group does not exists
			session.write(response.getResponse(421, mailbox) + ", Error: Database unavailable");
			return true;
		}
	}

	/**
	 * Handles HELP Command
	 * 
	 * @return false only when there is error with reading
	 *  from database, otherwise there is true
	 * @throws IOException if data stream are not available
	 */
	public boolean parseHELP() throws IOException {
		// If argument is empty then print general help
		if (argument.isEmpty()) {
			session.write(response.getResponse(214));
			return true;
		} else {
			session.write(response.getResponse(214,argument));
			return true;
		}
	}

	/**
	 * Handles DATA Command
	 * 
	 * @return false only when there is error with reading
	 *  from database or if SQL Query fails, otherwise there is true
	 * @throws IOException if data stream are not available
	 */
	public boolean parseDATA() throws IOException {
		// Check if recipient is set
		if (!rcptCommand) {
			session.write(response.getResponse(503) + ", Error: RCPT command must be send before data");
			return true;
		}

		// Send intermediate response
		session.write(response.getResponse(354));

		// Try to parse data
		if(!dataParser.parse(session)) {
			session.write(response.getResponse(500) + ", Line too long");
			return true;
		}

		// Strings
		String subject = "";
		String date = "";
		String body = "";
		String recipients = "";
		
		// Get subject from data parser
		if (!dataParser.getSubject().isEmpty()) {
			subject = dataParser.getSubject();
		}
		
		// Get date from data parser
		if (!dataParser.getDate().isEmpty()) {
			date = dataParser.getDate();
		}
				
		// Get mime header from data parser
		if (!dataParser.getBody().isEmpty()) {
			body = dataParser.getBody();
		}
		
		// Get sender from forward path
		if (forwardPath.isEmpty()) {
			session.write(response.getResponse(503) + ", Error: forward path missing");
			return true;
		}
		
		// Get recipients from reverse path
		if (!reversePath.isEmpty()) {
			for (int i = 0; i < reversePath.size() - 1; i++) {
				recipients += reversePath.get(i) + ", ";
			}
			recipients += reversePath.get(reversePath.size() - 1);
		} else {
			session.write(response.getResponse(503) + ", Error: there are no recipients");
			return true;
		}
		
		// Insert message
		try {
			database.insertMessage(subject, forwardPath, recipients, date, "", body);
			clearBuffers();
			session.write(response.getResponse(250));
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			// if mail group does not exists
			session.write(response.getResponse(421, mailbox) + ", Error: Database unavailable");
			return true;
		}
	}
	
	/**
	 * Clear the buffers. This method will clear forward 
	 * string and reverse arraylist. Also sets the mail 
	 * command and recipients command to false.
	 */
	private void clearBuffers() {
		forwardPath = "";
		reversePath.clear();
		mailCommand = false;
		rcptCommand = false;
	}

	/**
	 * Handles Reset Command. This command specifies that the current mail 
	 * transaction is to be aborted.  Any stored sender, recipients, 
	 * and mail data must be discarded, and all buffers and state 
	 * tables cleared. The receiver must send an OK reply.
	 * 
	 * @return false only when there is error with reading
	 *  from database or if SQL Query fails, otherwise there is true
	 * @throws IOException if data stream are not available
	 */
	public boolean parseRSET() throws IOException {
		if (argument.isEmpty()) {
			forwardPath = "";
			reversePath.clear();
			mailCommand = false;
			rcptCommand = false;
			session.write(response.getResponse(250));
			return true;
		} else {
			session.write(response.getResponse(500));
			return true;
		}
	}

	/**
	 * Handles NOOP Command. This command does not affect any 
	 * parameters or previously entered commands. It specifies 
	 * no action other than that the receiver send an OK reply.
	 * 
	 * @return false only when there is error with reading
	 *  from database or if SQL Query fails, otherwise there is true
	 * @throws IOException if data stream are not available
	 */
	public boolean parseNOOP() throws IOException {
		if (argument.isEmpty()) {
			session.write(response.getResponse(250));
			return true;
		} else {
			session.write(response.getResponse(500));
			return true;
		}
	}

	/**
	 * Handles QUIT Command. This command specifies that 
	 * the receiver must send an OK reply, and then close 
	 * the transmission channel.
	 * 
	 * @return false and close the transmission channel, 
	 * this commands does not use arguments but if client 
	 * will send any arguments the method will return true
	 * @throws IOException if data stream are not available
	 */
	public boolean parseQUIT() throws IOException {
		if (argument.isEmpty()) {
			session.write(response.getResponse(221, mailbox));
			return false;
		} else {
			session.write(response.getResponse(500));
			return true;
		}
	}

	/**
	 * Handles SEND Command. This command is not implemented 
	 * which sends 502 response back to the client.
	 * 
	 * @return false only when there is error with reading
	 *  from database or if SQL Query fails, otherwise there is true
	 * @throws IOException if data stream are not available
	 */
	public boolean parseSEND() throws IOException {
		session.write(response.getResponse(502));
		return true;
	}

	/**
	 * Handles SOML Command. This command is not implemented 
	 * which sends 502 response back to the client.
	 * 
	 * @return false only when there is error with reading
	 *  from database or if SQL Query fails, otherwise there is true
	 * @throws IOException if data stream are not available
	 */
	public boolean parseSOML() throws IOException {
		session.write(response.getResponse(502));
		return true;
	}

	/**
	 * Handles SAML Command. This command is not implemented
	 * which sends 502 response back to the client.
	 * 
	 * @return false only when there is error with reading
	 *  from database or if SQL Query fails, otherwise there is true
	 * @throws IOException if data stream are not available
	 */
	public boolean parseSAML() throws IOException {
		session.write(response.getResponse(502));
		return true;
	}

	/**
	 * Handles TURN Command. This command is not implemented 
	 * which sends 502 response back to the client.
	 * 
	 * @return false only when there is error with reading
	 *  from database or if SQL Query fails, otherwise there is true
	 * @throws IOException if data stream are not available
	 */
	public boolean parseTURN() throws IOException {
		session.write(response.getResponse(502));
		return true;
	}
}
