package de.jeisfeld.pi.lut;

import java.io.IOException;

import de.jeisfeld.pi.lut.Sender.ReadType;

/**
 * Test class for LuT framework.
 */
public class Test { // SUPPRESS_CHECKSTYLE
	/**
	 * Main method.
	 *
	 * @param args The command line arguments (not required).
	 * @throws IOException connection issues
	 * @throws InterruptedException if interrupted
	 */
	public static void main(final String[] args) throws IOException, InterruptedException { // SUPPRESS_CHECKSTYLE
		// SYSTEMOUT:OFF
		Test.test3();
	}

	/**
	 * Test of sending lob signals.
	 *
	 * @throws IOException connection issues
	 * @throws InterruptedException if interrupted
	 */
	private static void test1() throws IOException, InterruptedException {
		Sender sender = Sender.getInstance();
		ChannelSender channelSender = sender.getChannelSender(1);

		int maxSignal = 100; // MAGIC_NUMBER
		int delay = 20; // MAGIC_NUMBER

		for (int power = 1; power < maxSignal; power++) {
			System.out.println("                                       " + power);
			channelSender.lob(power, delay);
		}

		sender.close();
	}

	/**
	 * Reading the input as fast as possible.
	 *
	 * @throws IOException connection issues
	 */
	private static void test2() throws IOException {
		Sender sender = Sender.getInstance();
		long lastTime = System.currentTimeMillis();

		while (true) {
			ButtonStatus status = sender.readInputs(ReadType.ALL);
			long newTime = System.currentTimeMillis();
			System.out.println(status + " - " + (newTime - lastTime));
			lastTime = newTime;
		}
	}

	/**
	 * Test of manipulating lob via input controls.
	 *
	 * @throws IOException connection issues
	 * @throws InterruptedException if interrupted
	 */
	private static void test3() throws IOException, InterruptedException {
		Sender sender = Sender.getInstance();
		ChannelSender channelSender = sender.getChannelSender(1);
		double cyclePoint = Math.PI;
		long lastTime = System.currentTimeMillis();

		while (true) {
			ButtonStatus status = sender.readInputs(ReadType.ANALOG);
			int power = status.getControl1Value();
			int frequency = status.getControl2Value();

			int value = (int) ((1 - Math.cos(cyclePoint)) / 2 * power);

			System.out.println(power + " - " + frequency + " - " + value);

			channelSender.lob(value, 0); // MAGIC_NUMBER

			long newTime = System.currentTimeMillis();
			long timeDiff = newTime - lastTime;
			lastTime = newTime;

			if (frequency > 0) {
				cyclePoint += 0.02 * timeDiff / frequency; // MAGIC_NUMBER
			}
			else {
				cyclePoint = Math.PI;
			}
		}
	}

}
