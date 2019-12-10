package de.jeisfeld.pi.lut;

import java.io.IOException;

import de.jeisfeld.pi.lut.core.Sender;
import de.jeisfeld.pi.lut.core.ShutdownListener;

/**
 * Test class for LuT framework.
 */
public class Startup { // SUPPRESS_CHECKSTYLE
	/**
	 * Main method.
	 *
	 * @param args The command line arguments (not required).
	 * @throws IOException connection issues
	 * @throws InterruptedException if interrupted
	 */
	public static void main(final String[] args) throws IOException, InterruptedException { // SUPPRESS_CHECKSTYLE
		Startup.run();
	}

	/**
	 * Test of script started on startup listening to button.
	 *
	 * @throws IOException connection issues
	 * @throws InterruptedException if interrupted
	 */
	private static void run() throws IOException, InterruptedException {
		Sender sender = Sender.getInstance();

		sender.setButton2LongPressListener(new ShutdownListener());

		while (true) {
			Thread.sleep(100); // MAGIC_NUMBER
		}
	}

}
