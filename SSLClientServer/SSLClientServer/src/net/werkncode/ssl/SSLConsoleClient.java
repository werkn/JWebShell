package net.werkncode.ssl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import net.werkncode.console.ConsoleInputHandler;
import net.werkncode.console.ServerResponseHandler;

/**
 * SSLConsoleClient is used to connect to our RestAPI and retrieve a list
 * of known SSLShellServer and accessTokens needed to connect to them.
 * 
 * Once a remote SSLShellServer has been selected by the user a SSL session
 * is established to the remote shell and the accessToken is verified.
 * 
 * If the accessToken is valid, the connection is allowed and a shell is spawned.
 * 
 * If the accessToken is not valid the SSL connection is closed and no shell is spawned.
 * 
 * @author werkn
 *
 */
public class SSLConsoleClient {

    private AtomicBoolean shutdown;

    private ExecutorService executorService;

    private void configureSSL(String trustStorePath, String trustStorePassword) {
        System.setProperty("javax.net.ssl.trustStore", trustStorePath);
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);   
    }
    
    /**
     * @param restApiHost Remote RestAPI IPAddress/hostname
     */
    public SSLConsoleClient(String restApiHost) {

    	//note: this is a relative URL from where the class files are executed,
    	//if we see errors regarding keystore issues, it's likely that the 
    	//path to scripts/certs is incorrect
    	configureSSL("../../SSLGenerator/export/one-way-ssl/ssl-client-trust-store.jts", "123456");
    	
        shutdown = new AtomicBoolean(false);

        //we want a fixedThreadPool with 2 threads one for reading the server socket
        //and another for sending console input to the server socket
        executorService = Executors.newFixedThreadPool(2);
 
        //select SSLShellServer to connect to       
        JSONArray availableShellServers = new JSONArray(SSLRestAPIConnectionAgent.getAvailableShellServers("https://"+restApiHost+":8443"));
        
        if (availableShellServers.length() == 0) {
        	System.out.println("No servers available.  Exiting...");
        	shutdown.set(true);
        	return;
        }
        
        for (int i = 0; i < availableShellServers.length(); i++) {
        	System.out.println("(" + i + ")" + " -> " +
        			((JSONObject) availableShellServers.get(i)).get("ip").toString()
        			+ ":" +
        			((JSONObject) availableShellServers.get(i)).getInt("port"));
        	
        }
        
        boolean validServerSelected = false;
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        int serverIndex = 0;
        while (!validServerSelected) {
        	try {
				serverIndex = Integer.parseInt(console.readLine().charAt(0)+"");
				
				if (serverIndex >= 0 && (serverIndex <= availableShellServers.length() - 1)) {
					validServerSelected = true;					
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (NumberFormatException e2) {
				System.out.println("Invalid selection!  Enter only a single digit.");
			}
        }
        
        connect(((JSONObject) availableShellServers.get(serverIndex)).get("ip").toString(), 
    			((JSONObject) availableShellServers.get(serverIndex)).getInt("port"),
    			((JSONObject) availableShellServers.get(serverIndex)).get("accessToken").toString());
    }
    
    /**
     * 
     * @param host Remote hostname/ip
     * @param port Port to access the remote SSLShellServer on (obtained from RestAPI)
     * @param accessToken Secure accessToken to use when connecting (obtained from RestAPI)
     */
    public void connect(String host, int port, String accessToken) {

    	SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        
        try (
        		SSLSocket socket = (SSLSocket) sslSocketFactory.createSocket(host, (int) (port % (Math.pow(2L,16L)-1)));
            
                BufferedReader charInputStream = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter charStreamWriter = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        )
        {
            //For executor service to capture the state of each task (thread)
            //we need to capture as a Future
            Future<?>[] tasksState = new Future<?>[2];
            //thread for reading server output
            tasksState[0] = executorService.submit(new ServerResponseHandler(shutdown,
                    charInputStream));

            //thread for reading console input and sending to server
            tasksState[1] = executorService.submit(new ConsoleInputHandler(shutdown, accessToken,
                    charStreamWriter));

            //check if there done or cancelled
            while (!shutdown.get()) {
                try {
                    Thread.sleep(2000);
                    //check that any of the tasks is cancelled or done (which means we've
                    //lost connection/console and need to shutdown)
                    for (Future<?> taskState : tasksState) {
                        if (taskState.isCancelled() || taskState.isDone()) {
                            System.out.println("Shutting down!");
                            shutdown.set(true);
                            //sleep this thread for 5 seconds to allow graceful shutdown in thread
                            Thread.sleep(5000);
                            break;
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        executorService.shutdown();
        System.out.println("Shutdown complete!");
    }

    private static void printHelp() {
    	System.out.println("Too few arguments, supply host and port in that order.");
    }
    
    public static void main(String ...args) {
    	//args[0] is remoteHostApiAddress 
    	if (args.length == 1) {
	    	String remoteRestApiAddress = args[0];
	    	
	    	if (remoteRestApiAddress == null || remoteRestApiAddress.length() == 0) {
	    		printHelp();
	    		System.exit(1);
	    	}
	    	
	    	new SSLConsoleClient(remoteRestApiAddress);
    	} else {
    		printHelp();
    		System.exit(1);
    	}
    }

}
