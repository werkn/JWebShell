package net.werkncode.console;
import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.System.*;

//handles console input from user and subsequent send to server
public class ConsoleInputHandler implements Runnable {

    //charPrintWriter is used to print to output stream, in this case it is
    //going to be the output stream of a Socket
    private PrintWriter destPrintWriter;
    
    //Console is used for console input
    private BufferedReader console;

    private AtomicBoolean shutdown; //used to force close thread

    private String accessToken;
    
    /**
     * 
     * @param shutdown An atomic boolean managed in parent thread for sending termination to this thread and all child threads
     * @param accessToken A secure access token used to access the remote SSLShellServer retrieved from RestAPI
     * @param destPrintWriter Destination SSLShellServer process input stream
     */
    public ConsoleInputHandler(AtomicBoolean shutdown, String accessToken, PrintWriter destPrintWriter) {

    	this.shutdown = shutdown;
        
        console = new BufferedReader(new InputStreamReader(System.in));
        if (console == null) {
            err.println("No console!  Application requires access to System.console()");
            exit(1);
        }
        this.destPrintWriter = destPrintWriter;
        this.accessToken = accessToken;
    }

    @Override
    public void run() {
        String line;
        boolean sentAccessToken = false;
        while (!shutdown.get()) {
        	try {
        		if (!sentAccessToken) {
        			destPrintWriter.println(accessToken);
        			sentAccessToken = true;
        		} else {
	        		
					line = console.readLine();
					
					if (line.trim().contains("exit_session")) {
						//send to server as well
						destPrintWriter.println("exit_session");
						
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						shutdown.set(true);
						break;
					}
					
					
					//check if the line contains !shell.cmd
		        	if (line.contains(CommandHandler.ISSUE_CMD_STRING)) {
		        		System.out.println(">>SSHConsoleClient: Processing command (local)...");
		        		System.out.println("----------------------------------------------");
		        		CommandHandler.handleCommand(line, destPrintWriter);
		        		System.out.println("----------------------------------------------");
		        		System.out.println(">>SSHConsoleClient: Done Processing command (local) <<");
		        	} else {
		        		destPrintWriter.println(line);  //NOTE:  We must set Writer instance to auto-flush in contstr
		        	}
        		}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

}
