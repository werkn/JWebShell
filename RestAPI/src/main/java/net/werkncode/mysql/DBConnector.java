package net.werkncode.mysql;

import java.sql.*;
import java.util.ArrayList;

import net.werkncode.ssl.SSLShellServerDetails;

/**
 * DBConnector is a utility class that maps adding, removing and querying available
 * servers on behalf of the RestAPI.
 * @author werkn
 *
 */
public class DBConnector {
	
	MySQLDatabase db;
	
	public DBConnector() {
		try {
			//connection string when using dockerized MySQL instance
			db = new MySQLDatabase("jdbc:mysql://localhost:3306/rest-client-api-mysql","root","superSecurePassword12345!!root");
		} catch (MySQLException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		} 
	}
	
	/**
	 * Executes SELECT against the database.
	 * 
	 * We don't verify the STMT is SELECT only which is insecure but fine
	 * for this assignment.
	 * 
	 * @param stmt MySQL statement as a string, ie: SELECT * FROM XXXX
	 * @param deferClose deferClose allows us to close the statement later in our code, ie: so we can iterate over a results set
	 * @return ResultSet ResultSet containing results from Query
	 */
	private ResultSet executeQuery(String stmt, boolean deferClose) {
		Statement statement;
		ResultSet results = null;
		try {
			statement = db.conn.createStatement();
			
			//executeQuery is used for SELECT 
			results = statement.executeQuery(stmt);
			
			if (!deferClose) {
				statement.close();
			}
		} catch (SQLException e) {
			System.out.println("In executeQuery Catch" + "CODE:" + e.getSQLState());
		}
		return results;
	}
	
	/**
	 * Execute and update on a record already in the database using stmt provided.
	 * 
	 * We don't verify the STMT is UPDATE only which is insecure but fine
	 * for this assignment.
	 * 
	 * @param stmt MySQL statement as string representing a UPDATE
	 * @return boolean true if rows were affected, false otherwise
	 * @throws DBConnectorException {@link DBConnectorException} thrown for duplicate, no client or unknown error
	 */
	private boolean executeUpdate(String stmt, boolean ignoreNoRowsAffected) throws DBConnectorException {
		Statement statement;
		int rowsAffected = 0;
		try {
			statement = db.conn.createStatement();
			//executeUpdate is used for INSERT, DELETE and UPDATE
			rowsAffected = statement.executeUpdate(stmt);
			if (rowsAffected == 0 && !ignoreNoRowsAffected) {
				//no rows affected, return exception
				throw new DBConnectorException(DBConnectorErrors.NO_ROWS_AFFECTED_MSG, DBConnectorErrors.NO_ROWS_AFFECTED_CODE, DBConnectorErrors.NO_SQL_ERROR);
			}
			statement.close();
		} catch (SQLException e) {
			System.out.println("In executeUpdate Catch" + "SQLSTATE CODE:" + e.getSQLState() + "SQLERRORCODE:" + e.getErrorCode());
			switch (e.getSQLState()) {
			
				//J/Connector states: https://dev.mysql.com/doc/connector-j/5.1/en/connector-j-reference-error-sqlstates.html
				case "23000":
					switch (e.getErrorCode()) {
						case 1452:
							throw new DBConnectorException(DBConnectorErrors.CLIENT_DOESNT_EXIST_MSG, DBConnectorErrors.CLIENT_DOESNT_EXIST_CODE, e.getErrorCode());
						case 1062:
							throw new DBConnectorException(
									DBConnectorErrors.DUPLICATE_RECORD_MSG, DBConnectorErrors.DUPLICATE_RECORD_CODE, e.getErrorCode());
					}
				default:
					throw new DBConnectorException(DBConnectorErrors.UNKNOWN_UPDATE_ERROR_MSG, DBConnectorErrors.UNKNOWN_UPDATE_ERROR_CODE, e.getErrorCode());
			}
		}
		return (rowsAffected == 1) ? true : false;
	}
	
	public boolean addClient(SSLShellServerDetails details) throws DBConnectorException {
		return addClient(details.getIP(), details.getPort(), details.getAccessToken());
	}
	
	//add a client to the database, to access client we will need the generate
	//accessToken, client will also register access token
	public boolean addClient(String ip, int port, String accessToken) throws DBConnectorException {
		String update = String.format("INSERT INTO Clients values('%s', %d, '%s')", ip, port, accessToken);
		System.out.println(update);
		return executeUpdate(update, false);
	}
	
	public boolean removeClient(SSLShellServerDetails details) throws DBConnectorException {
		return removeClient(details.getIP(), details.getPort(), details.getAccessToken());
	}
	
	//this will delete the Client
	public boolean removeClient(String ip, int port, String accessToken) throws DBConnectorException {
		String update = String.format("DELETE FROM Clients WHERE IP = '%s' AND Port = '%d' AND AccessToken = '%s'", ip, port, accessToken);
		System.out.println(update);
		return executeUpdate(update, false);
	}

	// Return all records as an ArrayList<FileRecord>, these will be serialized in
	// the server as JSON and sent to the client
	public SSLShellServerDetails[] getAllFiles() {
		
		System.out.println("SELECT * FROM Clients ORDER BY IP ASC");
		ResultSet results = executeQuery("SELECT * FROM Clients ORDER BY IP ASC", true);
		try {
			ArrayList<SSLShellServerDetails> serverList = new ArrayList<>();
			if (results != null) {
				while (results.next()) {
					serverList.add(new SSLShellServerDetails(results.getString(1), results.getInt(2), results.getString(3)));
				}
			}

			// close our statement we deferred from executeQuery
			results.getStatement().close();
			
			//casting in the return was failing... this seems to work
			SSLShellServerDetails[] serverListAsArray = new SSLShellServerDetails[serverList.size()];
			serverListAsArray = serverList.toArray(serverListAsArray);
			return serverListAsArray;
			
		} catch (SQLException e) {
		}

		return new SSLShellServerDetails[] {};
	}

	//utility method for testing to wipe the database
	public static void wipeDatabase() {
		DBConnector dbConnector = new DBConnector();
		//delete files before clients due to FK reference to client ip
		try {
			dbConnector.executeUpdate("DELETE FROM Clients;", true);
			dbConnector.db.conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



}
