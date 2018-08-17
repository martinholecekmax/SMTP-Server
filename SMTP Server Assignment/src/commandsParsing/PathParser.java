package commandsParsing;

/**
 * This class parse path and check 
 * if is in valid format.
 * 
 * @author Martin Holecek
 *
 */
public class PathParser {
	
	private DomainParser domainParser;
	private String username;
	private String domain;
	
	/**
	 * Constructor
	 */
	public PathParser() {
		domainParser = new DomainParser();
	}
	
	/**
	 * Check if the path is in valid format according to RFC821, page 29
	 * 
	 * @param input is a string text contains path
	 * @return true if is valid format otherwise false
	 */
	public boolean parsePath(String input) {
		// Check if path contains colon ":"
		if (input.contains(":")) {
			String[] path = input.split(":");
			
			// Length must be minimum 2 if there is colon
			if (path.length < 2) {
				return false;
			}
			
			// Check mailbox correct form
			if(!parseMailbox(path[path.length - 1])) {
				return false;
			}
			
			// Check for each path (except last) if the domain is in valid format
			for (int i = 0; i < path.length - 1; i++) {
				if (path[i].startsWith("@")) {
					if(!domainParser.isDomainValid(path[i].substring(1))){
						return false;
					}
				} else {
					return false;
				}
			}
			
			return true;
		}
		else {
			// Does not contain colon, then check if the mailbox is in valid form
			return parseMailbox(input);
		}
	}

	/**
	 * Check if the mailbox from RFC821, page 29, is valid mailbox address
	 * 
	 * @param input is string text which must contain @ symbol
	 * @return true if input is valid mailbox address
	 */
	private boolean parseMailbox(String input) {
		// Clear username and domain
		setDomain("");
		setUsername("");
		
		// Can not be empty
		if (input.isEmpty()) {
			return false;
		}
		
		if (input.contains("@")) {
			String[] localPath = input.split("@");
			
			// Local path can not be empty
			if(localPath[0].isEmpty()) {
				return false;
			}
			
			// Contains more then one @ symbol
			if (localPath.length > 2 || localPath.length < 2) {
				return false;
			}
			
			// Check quoted String ("username")
			if (localPath[0].contains("\"")) {
				if (!localPath[0].startsWith("\"") || !localPath[0].endsWith("\"")) {
					return false;
				} else {
					localPath[0] = localPath[0].substring(1, localPath[0].length() - 1);
				}
			}
			
			// If contains dot "." then check each element
			if (localPath[0].contains(".")) {
				String[] host = localPath[0].split("\\.");
				for (String str : host) {
					// Cannot be empty
					if (str.isEmpty()) {
						return false;
					}
								
					// Check if element contains only alphanumeric characters
					if(!domainParser.isAlphanumeric(str)) {
						return false;
					}
				}
			} else {
				// Check if element contains only alphanumeric characters
				if(!domainParser.isAlphanumeric(localPath[0])) {
					return false;
				}
			}
			
			// Check if domain name is valid
			if (!domainParser.isDomainValid(localPath[1])) {
				return false;
			}
			
			// Set domain and username
			setUsername(localPath[0]);
			setDomain(localPath[1]);
			
			return true;
		} else {
			// Argument must contain @ symbol
			return false;
		}
	}

	/**
	 * Get username
	 * @return username is a string text
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * Set username
	 * @param username is a string text
	 */
	private void setUsername(String username) {
		this.username = username;
	}

	/**
	 * Get domain name
	 * @return domain is a string text
	 */
	public String getDomain() {
		return domain;
	}

	/**
	 * Set domain name
	 * @param domain is a string text
	 */
	private void setDomain(String domain) {
		this.domain = domain;
	}
}
