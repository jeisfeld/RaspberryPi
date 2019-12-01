package de.jeisfeld.pi.lut;

import java.io.IOException;

import de.jeisfeld.pi.lut.core.ButtonStatus;
import de.jeisfeld.pi.lut.core.ButtonStatus.ButtonListener;
import de.jeisfeld.pi.lut.core.ChannelSender;
import de.jeisfeld.pi.lut.core.Sender;

/**
 * Test class for LuT framework.
 */
public class TadelTest { // SUPPRESS_CHECKSTYLE
	/**
	 * Flag indicating if the Tadel is active.
	 */
	private static boolean mIsActive = true;

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

		sender.setButton1Listener(new ButtonListener() {
			@Override
			public void handleButtonUp() {
				// do nothing
			}

			@Override
			public void handleButtonDown() {
				TadelTest.mIsActive = !TadelTest.mIsActive;
			}
		});

		sender.setButton2Listener(new ButtonListener() {
			@Override
			public void handleButtonUp() {
				// do nothing
			}

			@Override
			public void handleButtonDown() {
				try {
					Runtime.getRuntime().exec("sudo shutdown -h now");
				}
				catch (IOException e) {
					// ignore
				}
			}
		});

		while (true) {
			ButtonStatus status = sender.getButtonStatus();
			int power = status.getControl1Value();
			int frequency = status.getControl2Value() / 2;
			int wave = status.getControl3Value() / 10; // MAGIC_NUMBER

			System.out.println(power + " - " + frequency + " - " + wave);
			if (TadelTest.mIsActive) {
				channelSender.tadel(power, frequency, wave, Sender.SEND_DURATION);
			}
			else {
				channelSender.tadel(0, 0, 0, Sender.SEND_DURATION);
			}
		}
	}

}
