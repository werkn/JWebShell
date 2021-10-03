package net.werkncode.mysql;

public class DBConnectorException extends Exception {

	private static final long serialVersionUID = -7361495647623782678L;
	
	public final int ERROR_CODE;

	public final int SQL_ERROR_CODE;
	
	public DBConnectorException(String message, int errorCode, int sqlErrorCode) {
		super(message);
		this.ERROR_CODE = errorCode;
		this.SQL_ERROR_CODE = sqlErrorCode;
	}
	
}
