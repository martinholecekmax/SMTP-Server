package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

import ascii.ConvertToASCII;

/**
 * This Class creates Session between 
 * server and client.
 * 
 * @author Martin Holecek
 *
 */
public class Session {

	public Socket socket = null;
	private DataInputStream input = null;
	private DataOutputStream output = null;
	private Logger logger;

	/**
	 * Constructor
	 * 
	 * @param socket connection to between server and client
	 * @param logger logs the errors to the file or console
	 * @throws IOException if the DataStream is not available
	 */
	public Session(Socket socket, Logger logger) throws IOException {
		this.socket = socket;
		this.logger = logger;
		//socket.setSoTimeout(30000); // Test timeout (30 seconds)
		socket.setSoTimeout(600000);
		input = new DataInputStream(socket.getInputStream());
		output = new DataOutputStream(socket.getOutputStream());
	}

	/**
	 * Close data stream and socket channel
	 */
	public void close() {
		try {
			input.close();
			output.close();
			socket.close();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error timeout exceeded", e);
		}
	}
	
	/**
	 * Sent string message over the network (7-bits ASCII)
	 * @param msg is a message in string format
	 * @throws IOException if the DataStream is not available
	 */
	public synchronized void write(String msg) throws IOException {
		byte[] message = ConvertToASCII.getAsciiBytes(msg);
		output.writeInt(message.length);
		output.flush();
    	output.write(message);
    	output.flush();
	}
	
	/**
	 * Read incoming ASCII text and convert that to the
	 * string text
	 * @return text in string format
	 * @throws IOException if the DataStream is not available
	 */
	public synchronized String read() throws IOException {
		int length = input.readInt();  
    	byte[] message = null;
    	String data;
    	if(length>0) {
    	    message = new byte[length];
    	    input.readFully(message, 0, message.length); // read the message
    	    data = new String(message, "UTF-8");
    	} else {
    		data = "";
    	}
    	return data;
	}
}
