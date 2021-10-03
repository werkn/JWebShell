package net.werkncode.spring.RestAPI.security;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Random;

/**
 * Generate a basic 32 character access token.  Hash collisions are possible.
 * @author werkn
 *
 */
public class AccessTokenGenerator {

	/**
	 * Generates a 32 bit randomized, MD5 hashed accessToken.
	 * @return String 32 bit randomized, MD5 hashed accessToken
	 */
	public static String getAccessToken() {
	
		try {
			StringBuilder nonHashedToken = new StringBuilder(32);
			Random rand = new Random();
			for (int i = 0; i < 32; i++) {
				//use UTF-8 Latin basic range (excluding space)
				nonHashedToken.append((char)(33 + (rand.nextInt(93))));	
			}
			
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] array = md.digest(nonHashedToken.toString().getBytes(Charset.forName("UTF-8")));
			StringBuffer hashedKey = new StringBuffer();
			
			for (int i = 0; i < array.length; ++i) {
				hashedKey.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
			}
	
			return hashedKey.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
			//do nothing the platforms were on should have access to MD5 MessageDigest
		}
		
		return null;
	}
	
	/**
	public static void main(String[] args) {
		for(int i = 0; i < 30; i++) {
			System.out.println(AccessTokenGenerator.getAccessToken());
		}

	}
	*/

}
