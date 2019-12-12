package de.jeisfeld.pi.lut;

import java.io.IOException;

import de.jeisfeld.pi.lut.core.ButtonStatus.ButtonListener;
import de.jeisfeld.pi.lut.core.ButtonStatus.OnLongPressListener;
import de.jeisfeld.pi.lut.core.Sender;
import de.jeisfeld.pi.lut.core.ShutdownListener;

/**
 * Test class for LuT framework.
 */
public class Startup { // SUPPRESS_CHECKSTYLE
	/**
	 * Flag indicating if we run RandomizedLob or RandomizedTadel.
	 */
	private static boolean mIsTadel = false;
	/**
	 * The used channel.
	 */
	private static int mChannel = 0;

	/**
	 * Main method.
	 *
	 * @param args The command line arguments (not required).
	 * @throws IOException connection issues
	 * @throws InterruptedException if interrupted
	 */
	public static void main(final String[] args) throws IOException, InterruptedException { // SUPPRESS_CHECKSTYLE
		RandomizedLob[] lobs = {new RandomizedLob(0), new RandomizedLob(1)};
		RandomizedTadel[] tadels = {new RandomizedTadel(0), new RandomizedTadel(1)};

		Sender sender = Sender.getInstance();
		sender.setButton2LongPressListener(new ShutdownListener(4000)); // MAGIC_NUMBER

		sender.setButton2Listener(new ButtonListener() {
			@Override
			public void handleButtonDown() {
				Startup.mChannel = (Startup.mChannel + 1) % 2;

				if (Startup.mIsTadel) {
					for (RandomizedTadel tadel : tadels) {
						tadel.stop();
					}
					lobs[Startup.mChannel].signal(2, true);
					new Thread(tadels[Startup.mChannel]).start();
				}
				else {
					for (RandomizedLob lob : lobs) {
						lob.stop();
					}
					lobs[Startup.mChannel].signal(1, true);
					new Thread(lobs[Startup.mChannel]).start();
				}
			}
		});

		sender.setButton1LongPressListener(new OnLongPressListener() {
			@Override
			public void handleLongTrigger() {
				if (Startup.mIsTadel) {
					for (RandomizedTadel tadel : tadels) {
						tadel.stop();
					}
					lobs[Startup.mChannel].signal(1, true);
					new Thread(lobs[Startup.mChannel]).start();
					Startup.mIsTadel = false;
				}
				else {
					for (RandomizedLob lob : lobs) {
						lob.stop();
					}
					lobs[Startup.mChannel].signal(2, true);
					new Thread(tadels[Startup.mChannel]).start();
					Startup.mIsTadel = true;
				}
			}
		});

		new Thread(lobs[Startup.mChannel]).start();
	}

}
