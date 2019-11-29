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
		Sender sender = Sender.getInstance();
		ChannelSender channelSender = sender.getChannelSender(1);
		double cyclePoint = 0;
		long lastTime = System.currentTimeMillis();

		while (true) {
			ButtonStatus status = sender.readInputs();
			int power = status.getControl1Value();
			int frequency = status.getControl2Value();

			int value = (int) ((1 - Math.cos(cyclePoint)) / 2 * power);

			channelSender.lob(value, 10); // MAGIC_NUMBER

			long newTime = System.currentTimeMillis();
			long timeDiff = newTime - lastTime;
			lastTime = newTime;

			if (frequency > 0) {
				cyclePoint += 0.02 * timeDiff / frequency;
			}
			else {
				cyclePoint = Math.PI;
			}

		}
	}

}
