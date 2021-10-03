package net.werkncode.ssl;

/**
 * POJO (Plain old java object) used for mapping automatically
 * SSLShellServer connection details to JSON in the RestAPI, SSLShellServer
 * and lastly, SSLShellClient.
 *
 * Duplicate of SSLClientServer/net/werkncode/ssl/SSLShellServerDetials.java
 */
public class SSLShellServerDetails {
	
	private String ip;
	private int port;
	private String accessToken;
	
	public String getIP() {
		return ip;
	}

	public int getPort() {
		return port;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public SSLShellServerDetails(final String ip, final int port, final String accessToken) {
		this.ip = ip;
		this.port = port;
		this.accessToken = accessToken;
	}
	
	@Override
	public String toString() {
		return "{ ip: '"+ ip + "'," 
				+ " port: " + port + ", accessToken:'" + accessToken +"' },";
	}
}
