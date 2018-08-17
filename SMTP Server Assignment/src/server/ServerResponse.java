package server;

/**
 * This class handles string representation of the reply codes.
 * 
 * @author Martin Holecek
 *
 */
public class ServerResponse {

	/**
	 * Get string representation of the response by specific code.
	 * List of the reply codes: 214, 220, 221, 251, 421, 551 
	 * 
	 * @param response is numeric code of the response
	 * @param argument is the additional information such as domain or path
	 * @return string text representation of the response code
	 */
	public String getResponse(int response, String argument) {
		switch (response) {
		case 214:
			// Get specific help message
			return helpMessage(argument);
		case 220:
			// Argument is <domain>
			return "220 " + argument + " Service ready";
		case 221:
			// Argument is <domain>
			return "221 " + argument + " Service closing transmission channel";
		case 251:
			// Argument is <forward-path>
			return "251 User not local; will forward to " + argument;
		case 421:
			// Argument is <domain>
			return "421 " + argument + " Service not available, closing transmission channel";
		case 551:
			// Argument is <forward-path>
			return "551 User not local; please try " + argument;
		}
		return null;
	}

	/**
	 * This method will get specific hepl message from the argument
	 * 
	 * @param argument which is RFC command
	 * @return string text containing message
	 */
	private String helpMessage(String argument) {
		switch (argument.toUpperCase()) {
		case "HELO":
			return ("214-HELO: \r\n"
					+ "214-Identify sending SMTP\r\n"
					+ "214-Syntax of this command: \"HELO<SP><SENDING-HOST>\"\r\n"
					+ "214 End of HELP info");
		case "MAIL":
			return ("214-MAIL: \r\n"
					+ "214-Sender address\r\n"
					+ "214-Syntax of this command: \"MAIL<SP>FROM:<FROM-ADDRESS>\"\r\n"
					+ "214 End of HELP info");
		case "RCPT":
			return ("214-RCPT: \r\n"
					+ "214-Recipient address\r\n"
					+ "214-Syntax of this command: \"RCPT<SP>TO:<TO-ADDRESS>\"\r\n"
					+ "214 End of HELP info");
		case "DATA":
			return ("214-DATA: \r\n"
					+ "214-Begin a message\r\n"
					+ "214-Syntax of this command: \"DATA\"\r\n"
					+ "214 End of HELP info");
		case "RSET":
			return ("214-RSET: \r\n"
					+ "214-Abort a message and clear all buffers\r\n"
					+ "214-Syntax of this command: \"RSET\"\r\n"
					+ "214 End of HELP info");
		case "NOOP":
			return ("214-NOOP: \r\n"
					+ "214-Keeps server alive\r\n"
					+ "214-Syntax of this command: \"NOOP\"\r\n"
					+ "214 End of HELP info");
		case "QUIT":
			return ("214-QUIT: \r\n"
					+ "214-End the SMTP Session\r\n"
					+ "214-Syntax of this command: \"QUIT\"\r\n"
					+ "214 End of HELP info");
		case "VRFY":
			return ("214-VRFY: \r\n"
					+ "214-Verify a username\r\n"
					+ "214-Syntax of this command: \"VRFY<STRING>\"\r\n"
					+ "214 End of HELP info");
		case "EXPN":
			return ("214-EXPN: \r\n"
					+ "214-Expand a mailing list\r\n"
					+ "214-Syntax of this command: \"EXPN<STRING>\"\r\n"
					+ "214 End of HELP info");			
		default:
			return ("214-Argument not recognized\r\n"
					+ "214-Commands: \r\n"
					+ "214-HELO\tMAIL\tRCPT\tDATA\tRSET\r\n"
					+ "214-NOOP\tQUIT\tHELP\tVRFY\tEXPN\r\n"
					+ "214-For more info use \"HELP<SP><TOPIC>\"\r\n"
					+ "214 End of HELP info");
		}
	}

	/**
	 * Get string representation of the response by specific code.
	 * List of the reply codes: 211, 214, 250, 354, 450, 451, 452, 500, 
	 * 501, 502, 503, 504, 550, 552, 553, 554
	 * 
	 * @param response is numeric code of the response
	 * @return string text representation of the response code
	 */
	public String getResponse(int response) {
		switch (response) {
		case 211:
			// More information about the server
			return "211 System status, or system help reply";
		case 214:
			// Help reply
			return "214-Commands: \r\n"
			+ "214-HELO\tMAIL\tRCPT\tDATA\tRSET\r\n"
			+ "214-NOOP\tQUIT\tHELP\tVRFY\tEXPN\r\n"
			+ "214-For more info use \"HELP<SP><TOPIC>\"\r\n"
			+ "214 End of HELP info";
		case 250:
			return "250 Requested mail action okay, completed";
		case 354:
			return "354 Start mail input; end with <CRLF>.<CRLF>";
		case 450:
			return "450 Requested mail action not taken: mailbox unavailable";
		case 451:
			return "451 Requested action aborted: local error in processing";
		case 452:
			return "452 Requested action not taken: insufficient system storage";
		case 500:
			return "500 Syntax error, command unrecognized";
		case 501:
			return "501 Syntax error in parameters or arguments";
		case 502:
			return "502 Command not implemented, use HELP command for more info";
		case 503:
			return "503 Bad sequence of commands";
		case 504:
			return "504 Command parameter not implemented";
		case 550:
			return "550 Requested action not taken: mailbox unavailable";
		case 552:
			return "552 Requested mail action aborted: exceeded storage allocation";
		case 553:
			return "553 Requested action not taken: mailbox name not allowed";
		case 554:
			return "554 Transaction failed";
		}
		return null;
	}
}
