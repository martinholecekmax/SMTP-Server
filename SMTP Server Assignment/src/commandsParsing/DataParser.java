package commandsParsing;

import java.io.IOException;
import java.util.ArrayList;

import server.Session;

/**
 * This class handles parsing of the data message
 * 
 * @author Martin Holecek
 *
 */
public class DataParser {

	private String subject = "";
	private String date = "";
	private String body = "";
	private boolean running;
	private ArrayList<String> data;

	/**
	 * This message will parse data message
	 * sended from the client.
	 * 
	 * @param session sends and receives messages from the SMTP Server
	 * @return true if parsing is OK, false if line is longer then 1000 characters
	 * @throws IOException if data stream are not available
	 */
	public boolean parse(Session session) throws IOException {
		// Clear buffers
		subject = "";
		date = "";
		body = "";

		running = true;
		data = new ArrayList<>();

		while (running) {
			// Get message from the server
			String message = session.read();

			// Max length of the line must be 1000 characters
			if (message.length() > 1000) {
				return false;
			}

			// Parse message line by line
			String linesArray[] = message.split("\r\n");
			for (String line : linesArray) {
				if (line.equals(".")) {
					running = false;
				}
				else if(line.equals("..")) {
					line = line.substring(1);
					data.add(line);
				}
				else {
					data.add(line);	
				}
			}
		}

		// Parse subject, date and body
		for (int i = 0; i < data.size(); i++) {
			if (data.size() - 1 == i){
				setBody(getBody() + data.get(i));
			}else if (data.get(i).toLowerCase().startsWith("subject:")) {
				setSubject(data.get(i).substring(8));
			} else if (data.get(i).toLowerCase().startsWith("date:")) {
				setDate(data.get(i).substring(5));
			} else {
				setBody(getBody() + data.get(i) +"\r\n");
			}
		}		
		return true;
	}

	/**
	 * Get subject of the message
	 * 
	 * @return subject is a string text
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * Set subject of the message
	 * 
	 * @param subject is a string text
	 */
	private void setSubject(String subject) {
		this.subject = subject;
	}

	/**
	 * Get date of the message
	 * 
	 * @return date is a string format
	 */
	public String getDate() {
		return date;
	}

	/**
	 * Set date of the message
	 * 
	 * @param date is a string format
	 */
	private void setDate(String date) {
		this.date = date;
	}

	/**
	 * Get body (content) of the message
	 * 
	 * @return body of the message
	 */
	public String getBody() {
		return body;
	}

	/**
	 * Set body of the message
	 * @param message is a string text
	 */
	private void setBody(String message) {
		this.body = message;
	}
}
