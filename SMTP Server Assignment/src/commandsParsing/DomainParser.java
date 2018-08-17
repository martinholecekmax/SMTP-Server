package commandsParsing;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class parse domain and check 
 * if is in valid format.
 * 
 * @author Martin Holecek
 *
 */
public class DomainParser {
	
	/**
	 * Check if domain has valid format (numbers, dotted numbers
	 * or name)
	 * 
	 * @param domain name
	 * @return true if domain is valid otherwise false
	 */
	public boolean isDomainValid(String domain) {
		// Check "[", "]" and more then one dot ".." syntax
		if (!checkBrackets(domain)) {
			return false;
		}

		// domain must have minimum length of 3 characters
		if (domain.length() < 3) {
			return false;
		}

		// Dot can not be at the begining or end of the domain
		if (domain.startsWith(".") || domain.endsWith(".")) {
			return false;
		}

		// 32-bit Internet Address (dotted format)
		if (domain.contains("[") && domain.contains("]")) {
			Pattern pattern = Pattern.compile("\\[(.*?)\\]");
			Matcher matcher = pattern.matcher(domain);
			while(matcher.find()){
				String word = matcher.group(1);
				if (!isDottedNumber(word)) {
					return false;
				} 
			}
			
			// delete ip and brackets for further testing
			domain = domain.replaceAll("\\s*\\[[^\\]]*\\]\\s*", "");
			
			// Check if domain is empty
			if (domain.isEmpty()) {
				return true;
			}
			
			// Delete begining dot
			if(domain.startsWith(".")) {
				domain = domain.substring(1);
			}
			
			// Delete ending dot
			if (domain.endsWith(".")) {
				domain = domain.substring(0, domain.length() - 1);
			}
		}

		// Valid domain
		if(domain.contains(".")) {
			String array[] = domain.split("\\.");
			for (String element : array) {
				if (element.startsWith("#")) {
					element = element.substring(1);
					// check if element is valid number
					if (!isNumber(element)) {
						return false;
					}
				} else if (!isNameValid(element)) {
					return false;
				}
			}
			return true;
		} else {
			// domain contains only numbers
			if (domain.startsWith("#")) {
				domain = domain.substring(1);
				return isNumber(domain);
			}
			return isNameValid(domain);
		}
	}

	/**
	 * Check if string is in dotted ("X.X.X.X") number format (4 bytes - IP address)
	 * which is "dotnum" in RFC specification
	 * 
	 * @param input is a string which must contain dotted number (IP address format)
	 * @return true if is valid dotted number, else return false
	 */
	private boolean isDottedNumber(String input) {
		// dotted domain must contain numbers
		if (!input.contains(".")) {
			return false;
		}

		String ip[] = input.split("\\.");

		// check if is 4 bit address
		if (ip.length != 4) {
			return false;
		}

		// check if contains only numbers and if numbers are in range 0 to 255 (valid IP)
		for (String number : ip) {
			try {
				int value = Integer.parseInt(number);
				if (value < 0 || value > 255) {
					return false;
				}
			} catch (NumberFormatException e) {
				return false;
			}
		}
		return true;

	}

	/**
	 * Check if the input string can be parsed to number
	 * 
	 * @param input is string must be digits between 0-9
	 * @return true if is number otherwise false
	 */
	public boolean isNumber(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * Check if each "name" (symbol from RFC) is valid format 
	 * (page 29 in RFC 821)
	 * 
	 * @param input is string
	 * @return true if is in valid format, otherwise false
	 */
	private boolean isNameValid(String input) {
		// If input is empty then then name is ok
		if (input.isEmpty()) {
			return true;
		}
		
		// First character must be letter
		if (!Character.isLetter(input.charAt(0))) {
			return false;
		}

		if (input.contains("-")) {
			// Domain can not start with "-" symbol
			if (input.startsWith("-") || input.endsWith("-")) {
				return false;
			}
			String domainCharArray[] = input.split("-");

			// Check each element if does not contains symbols
			for (String eachChar : domainCharArray) {
				if (!isAlphanumeric(eachChar)) {
					return false;
				}
			}
			return true;
		} else {
			// Check if is valid ASCII (a-z and digits) and does not contains symbols 
			if (!isAlphanumeric(input)) {
				return false;
			}
			return true;
		}
	}

	/**
	 * Check if input string contains only alphanumeric
	 * values (a-z, A-Z, 0-9).
	 * 
	 * @param input string
	 * @return true if input does contain only alphanumerics (a-z, A-Z, 0-9)
	 */
	public boolean isAlphanumeric(String input) {
		char[] array = input.toCharArray();

		for (char c : array) {
			if (!Character.isLetterOrDigit(c)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Check if the IP brackets are in valid form
	 * and if there are not double dots ("..") syntaxes
	 * 
	 * @param input string
	 * @return true if input is in valid form otherwise return false
	 */
	private boolean checkBrackets(String input) {
		char[] array = input.toCharArray();

		boolean leftBracket = false;
		boolean rightBracket = false;
		char previousChar = ' ';
		
		// Iterate through string and check each character
		for (char c : array) {
			// check for nested brackets which are not supported in RFC
			if (c == '[' && leftBracket) {
				return false;
			}
			
			// check for nested brackets which are not supported in RFC
			if (c == ']' && rightBracket) {
				return false;
			}
			
			// Previous character must be dot "." or empty char
			if (c == '[') {
				leftBracket = true;
				rightBracket = false;
				if (previousChar != ' ' && previousChar != '.') {
					return false;
				}
			}
			
			// Set right bracket
			if (c == ']') {
				rightBracket = true;
				leftBracket = false;
			}
			
			// Character after right bracket "]" must be dot "."
			if (previousChar == ']') {
				if (c != '.') {
					return false;
				}
			}
			
			// Check for multiple dots syntax ".."
			if (previousChar == '.' && c == '.') {
				return false;
			}
						
			previousChar = c;
		}
		return true;
	}
}
