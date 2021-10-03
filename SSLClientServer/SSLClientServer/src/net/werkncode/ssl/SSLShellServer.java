package net.werkncode.ssl;

import java.io.*;
import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import org.json.JSONException;
import org.json.JSONObject;

import net.werkncode.console.CommandHandler;
import net.werkncode.net.AutoSelectNetworkInterface;

import static java.lang.System.out;

/**
 * Redirect input from reader to print writer, effectively think
 * of as piping one input to an output stream.
 * 
 * We do some input handling in this class to handle commands sent from the 
 * remote SSLConsoleClient input stream.
 * 
 * @author werkn
 *
 */
class RedirectBufferedReaderToPrintWriter implements Runnable {

	BufferedReader remoteShellInput;
	PrintWriter remoteShellOutput; //used to send commands / data back to client
	PrintWriter localShellOutput;
	AtomicBoolean shutdown;
	String accessToken;
	
	RedirectBufferedReaderToPrintWriter(AtomicBoolean shutdown, BufferedReader src, PrintWriter dest) {
		this.remoteShellInput = src;
		this.remoteShellOutput = null;
		this.localShellOutput = dest;		
		this.shutdown = shutdown;
	}
	
	RedirectBufferedReaderToPrintWriter(String accessToken, AtomicBoolean shutdown, BufferedReader srcIn, PrintWriter srcOut, PrintWriter dest) {
		this.accessToken = accessToken;
		this.remoteShellInput = srcIn;
		this.remoteShellOutput = srcOut;
		this.localShellOutput = dest;
		this.shutdown = shutdown;
	}
	
	@Override
	public void run() {
		//first line sent from server will be accessToken as JSON
		boolean accessTokenIsValid = false;
		String line;
        try {
			while ((line = remoteShellInput.readLine()) != null && !shutdown.get()) {

				//first line set will be accessToken, check it is valid
				//make sure accessToken isn't null (ie: threads not handling input)
				if (!accessTokenIsValid && accessToken != null ) {
					if (line != null && line.contains(accessToken)) {
						accessTokenIsValid = true;
						System.out.println("AccessToken is valid.  Waiting for commands...");
						remoteShellOutput.println("AccessToken is valid.  Waiting for commands...");
					} else {
						System.out.println("AccessToken was not valid.  Shutting down.");
						remoteShellOutput.println("AccessToken was not valid.  Shutting down.");
						shutdown.set(true);
						System.exit(1);
					}
				} else {
				
					//only handle the remote shell in the thread managing remote shell input
					if (remoteShellOutput != null) {
						
						if (line.trim().contains("exit_session")) {	
							shutdown.set(true);
							break;
						}
						
						
						//check if the line contains !shell.cmd
			        	if (line.contains(CommandHandler.ISSUE_CMD_STRING)) {
			        		System.out.println(">>SSHShellServer: Processing command from remote...");
			        		System.out.println("----------------------------------------------");
			        		CommandHandler.handleCommand(line, remoteShellOutput);
			        		System.out.println("----------------------------------------------");
			        		System.out.println(">>SSHShellServer: Done Processing command from remote <<");
			        	} else {
			        		localShellOutput.println(line);
			        	}
			        	
					} else {
						localShellOutput.println(line);
					}
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
}

/**
 * Spawns a system shell and wires input->output (socket), output->input
 * Effectively streaming the local shell back to remote.
 * 
 * @author werkn
 *
 */
class ShellConnectionHandler implements Runnable {

    private SSLSocket socket;
    private AtomicBoolean shutdown;
    private String accessToken;
    
    ShellConnectionHandler(AtomicBoolean shutdown, SSLSocket socket, String accessToken) {
        this.shutdown = shutdown;
        this.socket = socket;
        this.accessToken = accessToken;
    }

    @Override
    public void run() {

        //create input/output character streams for the socket
        try (
                //INPUT and OUTPUT are character streams meaning we cannot send binary data
                BufferedReader inCharacterStream =
                        new BufferedReader(new InputStreamReader(socket.getInputStream()));

                PrintWriter outCharacterStream =
                        new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
        		
        ) {
        	
            out.printf("Client '%s' connected.\n", socket.getInetAddress().getHostAddress());

            outCharacterStream.println("Spawning a shell....");
            
            ProcessBuilder builder = new ProcessBuilder();
            if (System.getProperty("os.name")
            		  .toLowerCase().startsWith("windows")) {
                builder.command("powershell.exe");
            } else {
                builder.command("sh");
            }
            builder.directory(new File(System.getProperty("user.home")));
            
            Process process = builder.start();
            
            //get process input stream (we write incoming commands from client to this
            try (
            		BufferedReader shellStdOut = 
            			new BufferedReader(new InputStreamReader(process.getInputStream()));
            		
            		BufferedReader shellStdErr = 
                			new BufferedReader(new InputStreamReader(process.getErrorStream()));
            		
            		PrintWriter shellStdIn =
                            new PrintWriter(new OutputStreamWriter(process.getOutputStream()), true);
            ) {
            	//redirect socket commands to shell stdin
            	new Thread(new RedirectBufferedReaderToPrintWriter(accessToken, shutdown, inCharacterStream, outCharacterStream, shellStdIn)).start();
            	
            	//redirect shell stdout to socket input
            	new Thread(new RedirectBufferedReaderToPrintWriter(shutdown, shellStdOut, outCharacterStream)).start();
            	
            	//redirect shell stderr to socket input
            	new Thread(new RedirectBufferedReaderToPrintWriter(shutdown, shellStdErr, outCharacterStream)).start();
            	
            	
            	//block thread while process is active
            	while (process.isAlive()) {
            		if (shutdown.get()) {
            			process.destroy();
            		}
            	}
            	
            	System.out.println("Shell exited!");
	        
    		} catch (Exception e) {
    			
    		}

            out.printf("Client '%s' has left server\n", socket.getInetAddress().getHostAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

/**
 * SSLShellServer provides a local system shell to a remote SSLConsoleClient.
 * 
 * It is threaded and can serve multiple SSLConsoleClients at once.
 * 
 * Once launched the SSLShellServer registers itself to the remote RestAPI,
 * retrieving a secure accessToken that must be provided by clients the access
 * the server.  Furthermore the system makes use one one-way SSL to the secure the connection
 * to/from the RestAPI and to/from the remote SSLClientConsole.
 * 
 * @author werkn
 */
public class SSLShellServer {
	
	private AtomicBoolean shutdown;
    private ExecutorService executorService;

    private void configureSSL(String keyStorePath, String keyStorePassword, 
    		String trustStorePath, String trustStorePassword) {
    	//Server requires the keystore and the truststore (just reuse the client trust store)
    	//this is due to the fact that the remote RestAPI is using the same private key as 
    	//this server
        System.setProperty("javax.net.ssl.keyStore", keyStorePath);
        System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword);
        System.setProperty("javax.net.ssl.trustStore", trustStorePath);
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
    }
    
    public SSLShellServer(String restApiAddress, int port) {
    	configureSSL("../../SSLGenerator/export/one-way-ssl/ssl-server-keystore.jks", "123456",
    			"../../SSLGenerator/export/one-way-ssl/ssl-client-trust-store.jts", "123456");
   
        //creates threads as needed, caches old threads
        executorService = Executors.newCachedThreadPool();

        shutdown = new AtomicBoolean(false);
        
        start(restApiAddress, port);
    }

    public void start(String restApiHost, int port) {
        int serverPort = port % (int)(Math.pow(2L,16L)-1);  //keep in port range of 0 - 65_535

        SSLServerSocketFactory sslServerSocketFactory =
        		(SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        
        //find a networkinterface that can reach the RestAPI
    	InetAddress interfaceAddress = AutoSelectNetworkInterface.getInterfaceAddressWithAccessToRemote(restApiHost);
    	
    	if (interfaceAddress != null) {    	
        	//bind socket to address where remote RestAPI is reachable
    		System.out.println(String.
    				format("Remote API found on interface with address: %s, port: %d... binding to this address.", interfaceAddress, port));
    	} else {
    		System.out.println("Unable to reach remote RestAPI from any connected NetworkInterface... is API up?");
    		System.exit(1);
    	}
        try (
    	        SSLServerSocket serverSocket =
    	                (SSLServerSocket) sslServerSocketFactory
    	                //bind to network interface, on port serverPort and backlog (queue up only 1 connection)
    	                .createServerSocket(serverPort, 1, interfaceAddress);
        ) {
        	
        	
            out.println("Server up.  Waiting for connections on localhost: " + serverPort + "...");

            try {
	            String responseAsJSON = SSLRestAPIConnectionAgent.addShellServer("https://"+restApiHost+":8443",
	        			serverSocket.getInetAddress().getHostAddress(),
	        			serverSocket.getLocalPort());
	        	JSONObject serverResponse = new JSONObject(responseAsJSON);
	        	
	        	if (!serverResponse.get("status").toString().contains("succeeded") ) {
	        		//unable to get a token from the server, shutdown
	        		System.out.println("Unable to get a secure access token/connect to Rest API, shutting down server.");
	        		System.exit(1);
	        	}
	        	
	        	//final here to have it accessible in shutdown hook anonymous class
	        	final String accessToken = serverResponse.getString("accessToken");;
	        	System.out.println("Added self to server.");
	          
	        	//server must be forced closed (SIGKILL)
	            //add a shutdown hook so when we SIGKILL we can 
	        	//remove ourself from the REST API
	            Runtime.getRuntime().addShutdownHook(new Thread() {
	            	
	            	@Override
	            	public void run() {
	            		//attempt to remove ourselves from the sever
	                	//remove self from REST API
	                	System.out.println("SIGKILL/SIGINT detected, trying to remove self from server...");
	            		SSLRestAPIConnectionAgent.removeShellServer("https://"+restApiHost+":8443",serverSocket.getInetAddress().getHostAddress(),
	                			serverSocket.getLocalPort(), accessToken);
	            	}
	            	
	            });
	
	            //watch System.in for shutdown command
	            new Thread() {
	            	
	            	@Override
	            	public void run() {
	            		try {
	            			//Pressing ENTER will shutdown the server
	            			System.in.read();
	            			shutdown.set(true);
	            			
	            			//force close waiting connection
							serverSocket.close();
						} catch (IOException e) {
						}
	            	}
	            	
	            }.start();
	            
	            while (true) {
	            	
	            	//shutdownSessionFlag is watched in all child threads to
	            	//orchestrate shutdown
	            	AtomicBoolean shutdownSessionFlag = new AtomicBoolean();
	                executorService.execute(new ShellConnectionHandler(shutdownSessionFlag,
	                        (SSLSocket)serverSocket.accept(), accessToken));
	                out.println("Connection allowed.  Waiting for more connections...");
	                
	                if (shutdown.get()) {
	                	//our shutdown hook will be called automatically, just break out of loop
	                	break;
	                }
	            }      
            } catch (JSONException e) {
            	//unable to get a token from the server, shutdown
        		System.out.println("Unable to get a secure access token/connect to Rest API, shutting down server.  Is the API up?");
        		System.exit(1);
            }

        } catch (IOException e) {
        	//ignore this will always fired as we interrupt serverSocket.accept() above
        	//to bring down the server, that being said connection issues are also caught here
        	//if were seeing connection issues uncomment this stack trace
        	//e.printStackTrace();
        }

        shutdown();
    }

    public void shutdown() {
        if (executorService != null) {
            //will not actually shutdown active threads but prevents new ones from being started
            executorService.shutdown();

            //attempt to force/stop shutdown all active threads (this however can be ignored by the thread)
            //returns a list of tasks that were due to start but did not
            for (Runnable notStartedTask : executorService.shutdownNow()) {
                out.println("Task for class '" + notStartedTask.getClass() + "' submitted but not started by ExecutorService");
            }
        }
    }

    private static void printHelp() {
    	System.out.println("Too few arguments, supply host and port in that order.");
    }
    
    public static void main(String ...args) {
    	//args[0] is remoteHostApiAddress 
    	//and args[1] is port to listen on
    	if (args.length == 2) {
	    	String remoteRestApiAddress = args[0];
	    	int port = 4444;
	    	
	    	try {
	    		port = Integer.parseInt(args[1]);
	    	} catch (NumberFormatException e) {
	    		port = 4444;
	    	}
	    	
	    	if (remoteRestApiAddress == null || remoteRestApiAddress.length() == 0) {
	    		printHelp();
	    		System.exit(1);
	    	}
	    	
	    	new SSLShellServer(remoteRestApiAddress, port);
    	} else {
    		printHelp();
    		System.exit(1);
    	}
    	
    	
   
    }
}
