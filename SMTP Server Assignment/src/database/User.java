package database;

/**
 * This Class creates user which is 
 * used for reading user from database
 * 
 * @author Martin Holecek
 *
 */
public class User {
	
	private String firstname;
	private String surname;
	private String username;
	private String domain;
	
	/**
	 * Get users first name
	 * @return first name of the user
	 */
	public String getFirstname() {
		return firstname;
	}
	
	/**
	 * Set users first name 
	 * 
	 * @param firstname string text
	 */
	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}
	
	/**
	 * Get users surname
	 * 
	 * @return surname string text
	 */
	public String getSurname() {
		return surname;
	}
	
	/**
	 * Set users surname
	 * 
	 * @param surname string text
	 */
	public void setSurname(String surname) {
		this.surname = surname;
	}
	
	/**
	 * Get username string
	 * 
	 * @return username string text
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * Set username
	 * 
	 * @param username string text
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	
	/**
	 * Get domain name
	 * 
	 * @return domain string text
	 */
	public String getDomain() {
		return domain;
	}
	
	/**
	 * Set domain of the server user is from
	 * 
	 * @param domain name of the user
	 */
	public void setDomain(String domain) {
		this.domain = domain;
	}
}
