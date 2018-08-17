package database;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * This class connects to the database
 * 
 * @author Martin Holecek
 */
public class DBConnect {
	private Connection connection;
	private ResultSet resultSet;
	private User user;
	private boolean verbose;
	private final String PASSWORD = "DcMriSnsJwiKquLo";
	private final String USERNAME = "user";
	
	/**
	 * Constructor
	 * Creates connection to the database
	 * 
	 * @param verbose if is true prints errors to the console
	 * @throws SQLException if database is unreachable
	 */
	public DBConnect(boolean verbose) throws SQLException{
		this.verbose = verbose;
		
		// Connect to the local host on my computer
		// connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/smtp", USERNAME, PASSWORD);		
		connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/smtp?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", USERNAME, PASSWORD);
	}

	/**
	 * This method is called by EXPN command
	 * 
	 * @param input is string text of the mail group
	 * @return list of users which are in the mail group
	 * @throws SQLException if the database is not available
	 */
	public ArrayList<User> getExpandList(String input) throws SQLException{
		ArrayList<User> list = new ArrayList<>();
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM users WHERE mailgroup = ?");

		// Insert input into the query and execute query
		preparedStatement.setString(1, input);
		resultSet = preparedStatement.executeQuery();

		// Add results to the list
		while (resultSet.next()) {  
			user = new User();
			user.setUsername(resultSet.getString("username"));
			user.setDomain(resultSet.getString("domain"));
			list.add(user);
		}

		return list;
	}

	/**
	 * Check if user exists in the database.
	 * This method is used by the verify command.
	 * 
	 * @param username string
	 * @return true if user exists otherwise return false
	 * @throws SQLException if the database is not available or if SQL Query fails
	 */
	public boolean usernameExists(String username) throws SQLException {
		// Prepare query and get the result
		PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `users` WHERE username = ?");
		preparedStatement.setString(1, username);
		resultSet = preparedStatement.executeQuery();		

		return resultSet.first();
	}

	/**
	 * Insert Email message to the database
	 * 
	 * @param subject is string text
	 * @param mailFrom is string text
	 * @param rcptTo is string text
	 * @param dateString is string text
	 * @param mime is string text
	 * @param body is string text
	 * @throws SQLException if the database is not available or if SQL Query fails
	 */
	public void insertMessage(String subject, String mailFrom, String rcptTo, String dateString, String mime, String body) throws SQLException{
		// create date
		long time = System.currentTimeMillis();
		Date date;

		// Get valid date
		if (dateString.isEmpty()) {
			date = new Date(time);
		} else {
			try {
				SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d MMM yy HH:mm:ss");
				java.util.Date dateUtil;
				dateUtil = simpleDateFormat.parse(dateString);
				date = new Date(dateUtil.getTime()); 
			} catch (ParseException e) {
				date = new Date(time);
			}
		}

		// Print message
		if (verbose) {
			System.out.println("\nBEGIN MAIL");
			System.out.println("Subject: " + subject);
			System.out.println("Mail From: " + mailFrom);
			System.out.println("Recipients: " + rcptTo);
			System.out.println("Date: " + date.toString());
			System.out.println("Mime Header: " + mime);
			System.out.println("Body: " + body);
			System.out.println("END MAIL\n");
		}

		// Insert new user into the table users
		PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `messages` (`id`, `subject`, `mail_from`, `rcpt_to`, `date`, `mime`, `body`) VALUES (NULL, ?, ?, ?, ?, ?, ?);");
		preparedStatement.setString(2, mailFrom);
		preparedStatement.setString(3, rcptTo);
		preparedStatement.setDate(4, date);
		preparedStatement.setString(6, body);

		// Check if subject is empty
		if (subject.isEmpty()) {
			preparedStatement.setNull(1, Types.VARCHAR);
		} else {
			preparedStatement.setString(1, subject);
		}

		// Check if mime header is empty
		if (mime.isEmpty()) {
			preparedStatement.setNull(5, Types.LONGVARCHAR);
		} else {
			preparedStatement.setString(5, mime);
		}

		// Execute query
		preparedStatement.executeUpdate();
	}
}
