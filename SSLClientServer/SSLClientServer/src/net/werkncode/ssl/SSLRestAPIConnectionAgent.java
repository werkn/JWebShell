package net.werkncode.ssl;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Class representing an HTTP Get/Post parameter.
 * 
 * ie: https://localhost?key1=value1&key2=value2 <---- these
 * 
 * @author werkn
 *
 */
class RequestParam {
	public RequestParam(String key, String value) {
		this.key = key;
		this.value = value;
	}
	protected String key;
	protected String value;
}

/**
 * Helper class for connecting over HTTPS to RestAPI and issueing
 * get commands that are mapped to adding, removing and getting lists of available
 * SSLShellServers
 * @author werkn
 *
 */
public class SSLRestAPIConnectionAgent {
	
	private final static String API_USERNAME = "server";
	private final static String API_PASSWORD = "password1234";
	
	private static String get(String restApiHostAndPort, String endpoint, ArrayList<RequestParam> params) {
		String response = "";
		
		try {
			String getUrl = restApiHostAndPort + endpoint;

			// build the get request
			if (params != null) {
				for (int i = 0; i < params.size(); i++) {
					if (i == 0) {
						getUrl += "?" + params.get(i).key + "=" + params.get(i).value;
					} else {
						getUrl += "&" + params.get(i).key + "=" + params.get(i).value;
					}
				}
			}

			URL url = new URL(getUrl);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			String encoding = Base64.getEncoder().encodeToString((API_USERNAME + ":" + API_PASSWORD).getBytes("UTF-8"));

			connection.setRequestMethod("GET");
			connection.setDoOutput(true);
			connection.setDoInput(true);

			connection.addRequestProperty(response, encoding);
			connection.setRequestProperty("Authorization", "Basic " + encoding);

			int responseCode = connection.getResponseCode();

			if (responseCode == 200) {
				InputStream content = (InputStream) connection.getInputStream();
				BufferedReader in = new BufferedReader(new InputStreamReader(content));
				String line;
				while ((line = in.readLine()) != null) {
					response += line;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return response;
	}
	
	/**
	 * Get a list of available SSLShellServers (returned as JSON)
	 * 
	 * @param restApiHostAndPort
	 * @return
	 */
	public static String getAvailableShellServers(String restApiHostAndPort) {
		try {
			JSONArray serverList = new JSONArray(get(restApiHostAndPort, "/getserverlist", null));
			return serverList.toString();
		} catch (JSONException e) {
			return "[]";
		}
	}
	
	//add a shell server to the remote API (making it available to connect to)
	public static String addShellServer(String restApiHostAndPort, final String ip, final int port) {
		
		ArrayList<RequestParam> params = new ArrayList<RequestParam>();
		params.add(new RequestParam("ip", ip));
		params.add(new RequestParam("port", ""+port));
		
		return get(restApiHostAndPort, "/addserver", params);
	}
	
	//remove a shell server from the remote API (making it available to connect to)
	public static String removeShellServer(String restApiHostAndPort, final String ip, final int port, final String accessToken) {

		ArrayList<RequestParam> params = new ArrayList<RequestParam>();
		params.add(new RequestParam("ip", ip));
		params.add(new RequestParam("port", ""+port));
		params.add(new RequestParam("accessToken", ""+accessToken));
		
		return get(restApiHostAndPort, "/removeserver", params);
	}
	
	/**
	public static void main(String[] args) {
		//configure SSL for testing
		System.setProperty("javax.net.ssl.trustStore", "../../SSLGenerator/export/one-way-ssl/ssl-client-trust-store.jts");
        System.setProperty("javax.net.ssl.trustStorePassword", "123456"); 
		
        System.out.println("Init server list (should be empty JSON array)");
		System.out.println(getAvailableShellServers());
		//System.out.println(addShellServer("192.168.0.1", 4444));
		//remember to set access token to value returned from addShellServer
		//System.out.println(removeShellServer("192.168.0.1", 4444, "ea840b6b7e07d9af3fc46e97db3bce02"));
		//System.out.println(getAvailableShellServers());
	}
	*/
		
	
}
