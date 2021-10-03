package net.werkncode.mysql;

/**
 * Description: 
 * Exception, used to alert server no database connectivity.
 * 
 * Expected Outputs:
 * None
 * 
 * Actions (How is it launched, what does it launch): 
 * Not Launched
 */
public class MySQLException extends Exception {

	private static final long serialVersionUID = 1L;

	public MySQLException() {
		super("Unable to connect to MySQL database, check your connection string and make sure MySQL is running.");
	}
	
}
