package net.werkncode.mysql;

/**
 * Class representing errors we trap and report on for the
 * RestAPI.
 * 
 * @author werkn
 *
 */
public class DBConnectorErrors {

	public static final int NO_SQL_ERROR = -1;

	public static final int CLIENT_DOESNT_EXIST_CODE = 0;
	public static final String CLIENT_DOESNT_EXIST_MSG = "Client does not exist.";
	
	public static final int DUPLICATE_RECORD_CODE = 1;
	public static final String DUPLICATE_RECORD_MSG = "Duplicate record.";	
	
	public static final int DUPLICATE_CLIENT_CODE = 3;
	public static final String DUPLICATE_CLIENT_MSG = "Client already exists on server.";
	
	public static final int UNKNOWN_UPDATE_ERROR_CODE = 4;
	public static final String UNKNOWN_UPDATE_ERROR_MSG = "Unknown error occured while performing update";

	public static final int NO_ROWS_AFFECTED_CODE = 5;
	public static final String NO_ROWS_AFFECTED_MSG = "No rows affected by query";
	
}
