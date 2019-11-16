package de.jeisfeld.pi.lut;

import java.io.IOException;

/**
 * Test class for LuT framework.
 */
public class Test { // SUPPRESS_CHECKSTYLE
	/**
	 * Main method.
	 *
	 * @param args The command line arguments (not required).
	 * @throws InterruptedException if interrupted
	 */
	public static void main(final String[] args) throws IOException, InterruptedException { // SUPPRESS_CHECKSTYLE
		ChannelSender sender = Sender.getInstance().getChannelSender(1);

		sender.lob((byte) 1, (byte) 49, 5000); // MAGIC_NUMBER

		Thread.sleep(1000); // MAGIC_NUMBER
		sender.lobOff();
		sender.close();
	}

}
