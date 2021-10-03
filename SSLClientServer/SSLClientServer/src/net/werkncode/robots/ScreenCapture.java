package net.werkncode.robots;

import java.awt.AWTException;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * If the system has a GUI with AWT a screenshot is capture and serialized to
 * byte array.
 * 
 * @author werkn
 */
public class ScreenCapture {

	/**
	 * 
	 * @param displayIndex If the system has multiple displays which display to capture.
	 * @throws HeadlessException
	 * @throws IOException
	 * @throws AWTException
	 * @return Image capture and serialized to byte array 
	 */
	public static byte[] captureScreen(int displayIndex) throws HeadlessException, IOException, AWTException {

		//adapted from: https://www.baeldung.com/java-taking-screenshots
		//first attempt to capture a screenshot
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] displays = graphicsEnvironment.getScreenDevices();

		if (displayIndex >= 0 && displayIndex <= (displays.length -1)) {
		//for now we just capture the first display
		BufferedImage screenshot = new Robot().createScreenCapture(
				displays[displayIndex].getDefaultConfiguration().getBounds());
		//we use ByteArray to hold the image so we don't need to write it to disk first
		ByteArrayOutputStream screenshotAsBytes = new ByteArrayOutputStream();

		//write byte array into ByteArrayOutputStream
		ImageIO.write(screenshot, "png", screenshotAsBytes);
		
			return screenshotAsBytes.toByteArray();
		
		} else {
			return null;
		}
}

}
