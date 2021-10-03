package net.werkncode.console;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.json.JSONObject;

import net.werkncode.base64.Base64Utils;
import net.werkncode.robots.ScreenCapture;


/**
 * Represents a command and its associated help message.
 */
class Command {
	public String command;
	public String helpMessage;
	
	public Command(String command, String helpMessage) {
		this.command = command;
		this.helpMessage = helpMessage;
	}
}

/**
 * CommandHandler is multi-purpose, essentially it reads provided streams
 * for both the client and the server and then either pushes its own commands back to 
 * other stream (so server -> client, client -> server).
 * 
 * This means that for the SSLConsoleClient we need to handle commands in both the 
 * ConsoleInputHandler (for commands we enter directly in via the keybd) or in the case of
 * commands like 'download' that cause the server to upload base64 data back as a response
 * and effectively issue commands back to us.
 * 
 * For the SSLShellServer we only need to listen on the thread that is handling user input.
 * 
 * Remember that the SSLShellSever is routing 3 main streams
 * Remote STDIN -> SSLShell STDIN (We only handle sending commands back to client in this thread)
 * Remote STDOUT <- SSLShell STDOUT
 * Remote STDERR <- SSLShell STDERR
 */
public class CommandHandler {

	//Commands take this form
	// shell.cmd = { cmd: "upload", arg: "path" };
	public static final String ISSUE_CMD_STRING = "!shell.cmd";
	
	private static ArrayList<Command> commands = new ArrayList<Command>();
	
	//static block init commands for the system
	static {
		commands.add(new Command("!shell.cmd { cmd:'download', args:'filename' }", "Download file (if exists) from remote to this system.  Usage: !shell.cmd { cmd:'download', args:'filename' }"));
		commands.add(new Command("!shell.cmd { cmd:'client-upload', args:'./files/testing/client-screenshot.png' }", "Upload file (if exists) from this system to remote.  Usage: !shell.cmd { cmd:'client-upload', args:'./files/testing/client-screenshot.png' }"));
		commands.add(new Command("!shell.cmd { cmd:'screenshot', args:'capture.png' }", "Download a screenshot from the remote (if it has a GUI).  Usage: !shell.cmd { cmd:'screenshot', args:'filename' }\""));
		commands.add(new Command("!shell.cmd { cmd:'help' }", "print help message, command list.    Usage: !shell.cmd { cmd:'help' }"));
		commands.add(new Command("exit_session", "Disconnect from remote."));
	}
	
	/**
	 * Handle a command encountered in either ConsoleInputHandler input stream or ServerResponseHandler
	 * output stream.
	 * 
	 * For SSLShellServer we also watch incoming stream from console client for commands and push responses
	 * back to the console clients remote socket output stream.
	 * 
	 * @param commandString Command captured from console input / or remote shell response.
	 * @param destPrintWriter Destination socket output stream (which is piped to process STDIN)
	 * @return
	 */
	public static boolean handleCommand(String commandString, PrintWriter destPrintWriter) {
		boolean commandFound = false;

		try {
			
			int jsonObjOpenBracketPos = commandString.lastIndexOf("{");
			String commandJson = commandString.substring(jsonObjOpenBracketPos, commandString.indexOf("}", jsonObjOpenBracketPos) + 1); 

			//attempt to convert commandString to JSON object
			JSONObject command = new JSONObject(commandJson);

			if (command.length() >= 1 || command.length() <= 3) {  

				//handle single part commands
				if (command.length() == 1) {

					switch(command.get("cmd").toString()) {
					case "help":
						printShellHelp();
						commandFound = true;
						break;
					case "exit":
						System.out.println("Shutting down application....");
						commandFound = true;
						break;
					}

				//handle commands with 2 arguments
				} else if (command.length() == 2) {
					switch (command.get("cmd").toString()) {
					
					//download commands push an upload command to the server, which pushes
					//the file back to the SSLConsoleClient input stream as 
					//JSON Object with file data as Base64 encoded string
					case "download":
						System.out.println("Downloading file....");
						
						String remoteFilename = (String) command.get("args");
						String downloadCommand = ISSUE_CMD_STRING + 
								"{ cmd:'server-upload', args:'" + remoteFilename + "' }";
						destPrintWriter.println(downloadCommand);						
						commandFound = true;
						break;
					
					//upload commands are pushed to the stream and are interpreted
					//to mean, read file from disk and pass to stream as JSON object
					//on SSLConsoleClient we call this directly, but on the server
					//we request an upload to client by passing 'download' 
					case "client-upload":
						commandFound = true;
						//without exhausting the stack/OOM we can probably safely shove
						//about 250 - 500kbs into base64 and transmit 
						//so we'll reject anything >250kb
						//the other issue is JSON library possibly having issues with
						//large string which effectively the following is
						//we could rewrite this to stream but works as is for now
						//so \_(0_o)_/ *meh*, it might break if memory is low/small stack size
						File clientFileToUpload = new File((String) command.get("args"));
						
						if (clientFileToUpload != null && clientFileToUpload.length() > 250*1024) {
							System.out.println("ERROR: File is too large to upload, max size is 250KB, size of file is " 
									+ clientFileToUpload.length() / 1024 + "KBs...");
							break;
						} else if (!clientFileToUpload.isFile()) {
							System.out.println("ERROR: File does not exist or is not a normal file (ie: it cannot be a directory, pipe, socket, or device file");
							break;
						}
						
						System.out.println("Uploading file to server....");
						
						String clientFileToUploadBase64 = Base64Utils.getFileAsBase64(clientFileToUpload.toString());
						String serverDestFilename = new File(command.get("args").toString()).getName();
						String clientUploadCommand = ISSUE_CMD_STRING + 
								"{ cmd:'process-base64', data:'" + clientFileToUploadBase64 
								+ "', filename: '" + serverDestFilename  + "' }";
						destPrintWriter.println(clientUploadCommand);						
						break;
					case "server-upload":
						commandFound = true;
						File serverFileToUpload = new File((String) command.get("args"));
						
						if (serverFileToUpload != null && serverFileToUpload.length() > 250*1024) {
							destPrintWriter.println("ERROR: File is too large to upload, max size is 250KB, size of file is " 
									+ serverFileToUpload.length() / 1024 + "KBs...");
							break;
						} else if (!serverFileToUpload.isFile()) {
							destPrintWriter.println("ERROR: File does not exist or is not a normal file (ie: it cannot be a directory, pipe, socket, or device file");
							break;
						}
						
						System.out.println("Uploading file to client....");
						
						String serverFileToUploadBase64 = Base64Utils.getFileAsBase64(serverFileToUpload.toString());
						String clientDestFilename = new File(command.get("args").toString()).getName();
						String serverUploadCommand = ISSUE_CMD_STRING + 
								"{ cmd:'process-base64', data:'" + serverFileToUploadBase64 
								+ "', filename: '" + clientDestFilename  + "' }";
						destPrintWriter.println(serverUploadCommand);						
						break;
					case "screenshot":						
						System.out.println("Requesting screenshot....");
						String screenshotFilename = (String) command.get("args");
						String screenshotCommand = ISSUE_CMD_STRING + 
								"{ cmd:'server-screenshot-upload', args:'" + screenshotFilename + "' }";
						destPrintWriter.println(screenshotCommand);						
						commandFound = true;
						break;
					case "server-screenshot-upload":

						//adapted from: https://www.baeldung.com/java-taking-screenshots
						//first attempt to capture a screenshot
						try {
							System.out.println("Uploading screenshot to server....");
	
							byte[] screenshotAsBytes = ScreenCapture.captureScreen(0); 
							if (screenshotAsBytes != null) {
								String screenshotToUploadBase64 = Base64Utils.convertByteArrayToBase64(
										screenshotAsBytes);
								String screenshotDestFilename = new File(command.get("args").toString()).getName();
								String screenshotUploadCommand = ISSUE_CMD_STRING + 
										"{ cmd:'process-base64', data:'" + screenshotToUploadBase64 
										+ "', filename: '" + screenshotDestFilename  + "' }";
								destPrintWriter.println(screenshotUploadCommand);
							} else {
								//server has no display, report back to client
								destPrintWriter.println("ERROR: Unable to capture screenshot, server is headless, lacks AWT or displayIndex does not exist.");
							}
						} catch (Exception e) {
							//server has no display, report back to client
							destPrintWriter.println("ERROR: Unable to capture screenshot, server is headless, lacks AWT or displayIndex does not exist.");
						}
						commandFound = true;
						break;
					}
					
				//commands with 3 arguments
				} else if (command.length() == 3) {
					switch (command.get("cmd").toString()) {
					case "process-base64":
						System.out.println("Processing base64...");
						//extract filename.extension
						File destFile = new File(command.getString("filename"));
						Base64Utils.saveBase64File(command.getString("data"), "./files/downloads/"+destFile.getName(), true);
						commandFound = true;
						break;		
					}
				}
			} 
			

			if (!commandFound) { 
				System.out.println("Command not found.  Try running: !shell.cmd { cmd:'help' } "); 
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Command was malformed, only the following are supported:");
			printShellHelp();
		}

		return commandFound;
	}
	
	/**
	 * Print available commands and their associated help.
	 */
	private static void printShellHelp() {
		System.out.println(String.format("\n%-20s | %s" , "Command", "Usage" ));
		System.out.println(String.format("-=-=-=-=-=-=-==-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-"));
		for(Command command : commands) {
			System.out.println(String.format("%-20s | %s" , command.command, command.helpMessage));
			
		}
		System.out.println();
	}
}
