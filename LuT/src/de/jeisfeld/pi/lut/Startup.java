package de.jeisfeld.pi.lut;

import java.io.IOException;

import de.jeisfeld.pi.lut.core.ButtonStatus.ButtonListener;
import de.jeisfeld.pi.lut.core.Sender;

/**
 * Test class for LuT framework.
 */
public class Startup { // SUPPRESS_CHECKSTYLE
	/**
	 * Flag indicating if we run RandomizedLob or RandomizedTadel.
	 */
	private static boolean mIsTadel = false;

	/**
	 * Main method.
	 *
	 * @param args The command line arguments (not required).
	 * @throws IOException connection issues
	 * @throws InterruptedException if interrupted
	 */
	public static void main(final String[] args) throws IOException, InterruptedException { // SUPPRESS_CHECKSTYLE
		RandomizedLob lob = new RandomizedLob(0);
		RandomizedTadel tadel = new RandomizedTadel(0);

		Sender sender = Sender.getInstance();
		sender.setButton2Listener(new ButtonListener() {
			@Override
			public void handleButtonDown() {
				if (Startup.mIsTadel) {
					tadel.stop();
					lob.signal(1, true);
					new Thread(lob).start();
					Startup.mIsTadel = false;
				}
				else {
					lob.stop();
					lob.signal(2, true);
					new Thread(tadel).start();
					Startup.mIsTadel = true;
				}
			}
		});

		new Thread(lob).start();
	}

}
