package net.werkncode.mysql;

import java.sql.*; 

/**
 * Description: 
 * Used to connect to MySQL via JDBC.
 * 
 * Adapted from: https://dev.mysql.com/doc/connector-j/5.1/en/connector-j-usagenotes-connect-drivermanager.html
 * 
 * Expected Outputs:
 * None
 * 
 * Actions (How is it launched, what does it launch): 
 * Not Launched
 */
public class MySQLDatabase {  
	
	public Connection conn;
	
	public MySQLDatabase(String jdbcUrl, String mySqlUser, String password) throws MySQLException {
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
			conn = DriverManager.getConnection(jdbcUrl, mySqlUser, password);  	
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			System.err.println("Error creating or finding com.mysql.jdbc.Driver, check your classpath!");
		} catch (SQLException e) {
			throw new MySQLException();
		}  
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		//close db connection if its on the heap
		if (conn != null) conn.close();
	}
	  
}  