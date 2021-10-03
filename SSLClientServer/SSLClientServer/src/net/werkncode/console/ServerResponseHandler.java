package net.werkncode.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.System.*;

//handles input stream from server
public class ServerResponseHandler implements Runnable {

    //charReader here is used to read InputStream of Socket,
    //which in this case is tied to our server
    private BufferedReader charReader;
    private AtomicBoolean shutdown;  //flagged outside thread to force a shutdown

    /**
     * @param shutdown An atomic boolean provided by parent thread to signal termination of this thread and all children
     * @param charReader Character input stream from remote SSLShellServer shell processes STDOUT/STDERR
     */
    public ServerResponseHandler(AtomicBoolean shutdown, BufferedReader charReader) {
        this.shutdown = shutdown;
        this.charReader = charReader;
    }

    @Override
    public void run() {
        try {
            String line;
            
            while ((line = charReader.readLine()) != null && !shutdown.get()) {
                //watch for commands coming back from the server
            	//check if the line contains !shell.cmd
	        	if (line.contains(CommandHandler.ISSUE_CMD_STRING)) {
	        		System.out.println(">>SSHConsoleClient: Processing command (local)...");
	        		System.out.println("----------------------------------------------");
	        		//server commands will require no callback  to original stream
	        		CommandHandler.handleCommand(line, null);
	        		System.out.println("----------------------------------------------");
	        		System.out.println(">>SSHConsoleClient: Done Processing command (local) <<");
	        	} else {
	        		out.printf("%s\n", line);
	        	}

            	
            }
        } catch (IOException e) {
            //handle remote connection closed
            err.printf("Connection to server closed.\n");
            shutdown.set(true);

            //sleep this thread for 5 seconds to allow graceful shutdown in other threads
            try {
                Thread.sleep(5000);
            } catch (InterruptedException iE) {
                iE.printStackTrace();
            }
        }
    }
}
