package de.jeisfeld.pi.lut;

import java.io.IOException;

import de.jeisfeld.pi.lut.core.ButtonStatus;
import de.jeisfeld.pi.lut.core.ChannelSender;
import de.jeisfeld.pi.lut.core.Sender;

/**
 * Test class for LuT framework.
 */
public class TadelTest { // SUPPRESS_CHECKSTYLE
	/**
	 * Main method.
	 *
	 * @param args The command line arguments (not required).
	 * @throws IOException connection issues
	 * @throws InterruptedException if interrupted
	 */
	public static void main(final String[] args) throws IOException, InterruptedException { // SUPPRESS_CHECKSTYLE
		// SYSTEMOUT:OFF
		TadelTest.test1();
	}

	/**
	 * Test of manipulating lob via input controls.
	 *
	 * @throws IOException connection issues
	 * @throws InterruptedException if interrupted
	 */
	private static void test1() throws IOException, InterruptedException {
		Sender sender = Sender.getInstance();
		ChannelSender channelSender = sender.getChannelSender(1);

		while (true) {
			ButtonStatus status = sender.getButtonStatus();
			int power = status.getControl1Value();
			int frequency = status.getControl2Value();
			int wave = status.getControl3Value();

			System.out.println(power + " - " + frequency + " - " + wave);
			channelSender.tadel(power, frequency, wave, 100); // MAGIC_NUMBER
		}
	}

}
