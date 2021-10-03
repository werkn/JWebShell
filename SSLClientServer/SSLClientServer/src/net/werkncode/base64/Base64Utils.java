package net.werkncode.base64;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Base64;

/**
 * Base64Utils is used to expose methods for converting files to and from
 * Base64 encoded formats for transmission of a UTF-8 character only stream.
 * @author werkn
 */
public class Base64Utils {

	/**
	 * Converts a byteArray to a base64 encoded string.
	 * 
	 * @param bytes bytes already encoded at UTF-8 base64
	 * @return
	 */
	public static String convertByteArrayToBase64(byte[] bytes) {
		Base64.Encoder encoder = Base64.getEncoder();  
		String encodedFileAsBase64String = null;
		
		//encode file
		encodedFileAsBase64String = encoder.encodeToString(bytes);
		
		return encodedFileAsBase64String;
	}
	
	/**
	 * Open a file from disk and encodes as a base64 string.
	 * 
	 * @param path Path to file
	 * @return String File as a base64 encoded string
	 * @throws IOException
	 */
	public static String getFileAsBase64(String path) throws IOException {
		Base64.Encoder encoder = Base64.getEncoder();  
		String encodedFileAsBase64String = null;
		byte[] fileAsBytes = null;
		
		//load a file, get bytes, encode it
		File file = new File(path);
		if (file != null && file.isFile()) {
			//read it into a byte array
			fileAsBytes = Files.readAllBytes(file.toPath());
			
			//encode file
			encodedFileAsBase64String = encoder.encodeToString(fileAsBytes);
		}
		
		return encodedFileAsBase64String; 
	}
	
	/**
	 * Saves a base64 encoded string to disk.
	 * 
	 * @param encodedFileAsBase64String File encoded as a base64 strign
	 * @param path Path to save file as
	 * @param replaceIfExists replace file if exists
	 * @return boolean true if saved, false otherwise
	 * @throws IOException
	 */
	public static boolean saveBase64File(String encodedFileAsBase64String, String path, boolean replaceIfExists) throws IOException {
		boolean fileSaved = false;
		
		Base64.Decoder decoder = Base64.getDecoder();  
		
		byte[] decodedFileAsBytes= null;
		decodedFileAsBytes = decoder.decode(encodedFileAsBase64String);
		
		File decodedFile = new File(path);
		
		//delete whatever is there
		if (replaceIfExists) {
			Files.deleteIfExists(decodedFile.toPath());
		}
		
		//convert encoded string back to bytes and save to disc
		Files.write(decodedFile.toPath(), decodedFileAsBytes, StandardOpenOption.CREATE);
		
		return fileSaved;
	}

}
