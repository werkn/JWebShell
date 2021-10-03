package net.werkncode.spring.RestAPI;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import net.werkncode.mysql.DBConnector;
import net.werkncode.mysql.DBConnectorException;
import net.werkncode.spring.RestAPI.security.AccessTokenGenerator;
import net.werkncode.ssl.SSLShellServerDetails;

/**
 * Represents all the endpoints under our RestAPI.
 * 
 * Specifically it handles:
 *  - adding a client and returning a secure accessToken
 *  - removing a client
 *  - querying available clients
 * 
 * @author werkn
 *
 */
@RestController
public class APIEndpointsController {
	
	public DBConnector db;
	
	public APIEndpointsController() {
		//wipe the database on every startup, wait ~5 seconds after doing so
		//before init db for controller use
		DBConnector.wipeDatabase();	
		try {
			System.out.println("Waiting for server to come up after wiping database...");
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		db = new DBConnector();
	}
	
	/**
	 * Retrieve as JSONArray a list of available servers to connect to.
	 * @return
	 */
	@GetMapping("/getserverlist")
	public SSLShellServerDetails[] getServerList() {
		return db.getAllFiles();
	}
	
	/**
	 * Retrieve as JSONObject the connection details needed to add a new server to the API.
	 * @param ip
	 * @param port
	 * @return
	 */
	@GetMapping("/addserver")
	public String addServer(@RequestParam String ip, @RequestParam short port) {
		SSLShellServerDetails details = new SSLShellServerDetails(ip, port, AccessTokenGenerator.getAccessToken());
		try {
			if (db.addClient(details)) {			
				return "{ status: 'succeeded', accessToken: '" + details.getAccessToken() + "' }";
			} else {
				return "{ status: 'failed' }";
			}
		} catch (DBConnectorException e) {
			return "{ status: 'failed' }";
		}
	}
	
	/**
	 * Remove a server from the API given a valid host, port and accessToken.
	 * @param ip
	 * @param port
	 * @param accessToken
	 * @return
	 */
	@GetMapping("/removeserver")
	public String removeServer(@RequestParam String ip, @RequestParam short port, @RequestParam String accessToken) {
		SSLShellServerDetails details = new SSLShellServerDetails(ip, port, accessToken);
		
		try {
			if (db.removeClient(details)) {
				return "{ status: 'succeeded' }";
			} else {			
				return "{ status: 'failed' }";
			}
		} catch (DBConnectorException e) {
			return "{ status: 'failed' }";
		}
	}
}
