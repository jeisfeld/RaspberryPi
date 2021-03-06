package de.jeisfeld.pi.lut;

import java.io.IOException;

import de.jeisfeld.pi.lut.core.ButtonStatus;
import de.jeisfeld.pi.lut.core.ButtonStatus.ButtonListener;
import de.jeisfeld.pi.lut.core.ButtonStatus.OnLongPressListener;
import de.jeisfeld.pi.lut.core.ChannelSender;
import de.jeisfeld.pi.lut.core.Sender;

/**
 * Test class for LuT framework.
 */
public class LobTest { // SUPPRESS_CHECKSTYLE
	/**
	 * Main method.
	 *
	 * @param args The command line arguments (not required).
	 * @throws IOException connection issues
	 * @throws InterruptedException if interrupted
	 */
	public static void main(final String[] args) throws IOException, InterruptedException { // SUPPRESS_CHECKSTYLE
		int testNo = 3; // MAGIC_NUMBER

		if (args.length > 0) {
			testNo = Integer.parseInt(args[0]);
		}

		// SYSTEMOUT:OFF
		LobTest.test(testNo, args); // MAGIC_NUMBER
	}

	/**
	 * Execute test.
	 *
	 * @param i test id.
	 * @param args the original command line arguments
	 * @throws IOException connection issues
	 * @throws InterruptedException if interrupted
	 */
	private static void test(final int i, final String[] args) throws IOException, InterruptedException {
		switch (i) {
		case 1:
			LobTest.test1();
			break;
		case 2:
			LobTest.test2();
			break;
		case 4: // MAGIC_NUMBER
			LobTest.test4();
			break;
		case 5: // MAGIC_NUMBER
			long duration = 1000; // MAGIC_NUMBER
			if (args.length >= 2) {
				duration = Long.parseLong(args[1]);
			}
			LobTest.test5(duration);
			break;
		default:
			LobTest.test3();
			break;
		}
	}

	/**
	 * Test of sending lob signals.
	 *
	 * @throws IOException connection issues
	 * @throws InterruptedException if interrupted
	 */
	private static void test1() throws IOException, InterruptedException {
		Sender sender = Sender.getInstance();
		ChannelSender channelSender = sender.getChannelSender(0);

		int maxSignal = 100; // MAGIC_NUMBER
		int delay = 50; // MAGIC_NUMBER

		for (int power = 1; power < maxSignal; power++) {
			channelSender.lob(power, delay, true);
		}

		channelSender.lob(1, maxSignal, delay * maxSignal);

		sender.close();
	}

	/**
	 * Reading the input.
	 *
	 * @throws IOException connection issues
	 * @throws InterruptedException if interrupted
	 */
	private static void test2() throws IOException, InterruptedException {
		Sender sender = Sender.getInstance();

		while (true) {
			ButtonStatus status = sender.getButtonStatus();
			System.out.println(status);
			Thread.sleep(100); // MAGIC_NUMBER
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
		ChannelSender[] channelSenders = {sender.getChannelSender(0), sender.getChannelSender(1)};
		ChannelSender[] channelSenderHolder = new ChannelSender[1];
		channelSenderHolder[0] = channelSenders[0];

		sender.setButton1Listener(new ButtonListener() {
			@Override
			public void handleButtonDown() {
				System.out.println("------------- Switching channel -------------");
				ChannelSender oldChannelSender = channelSenderHolder[0];
				channelSenderHolder[0] = oldChannelSender == channelSenders[0] ? channelSenders[1] : channelSenders[0];
				try {
					oldChannelSender.lob(0, 0, true);
				}
				catch (InterruptedException e) {
					// do nothing
				}
			}
		});

		double cyclePoint = 0;

		while (true) {
			ButtonStatus status = sender.getButtonStatus();
			int power = status.getControl1Value();
			int frequency = status.getControl2Value();
			int minPower = (status.getControl3Value() * power) / ButtonStatus.MAX_CONTROL_VALUE;

			int value = (int) ((1 - Math.cos(2 * Math.PI * cyclePoint)) / 2 * (power - minPower) + minPower);

			System.out.println(power + " - " + minPower + " - " + frequency + " - " + value);

			channelSenderHolder[0].lob(value);

			if (frequency > 0) {
				int factor = (frequency + 9) / 10; // MAGIC_NUMBER
				cyclePoint = (Math.round(cyclePoint * 2 * factor) + 1.0) / 2 / factor;
			}
			else {
				cyclePoint = 0.5; // MAGIC_NUMBER
			}
		}
	}

	/**
	 * Test button listeners.
	 *
	 * @throws IOException connection issues
	 * @throws InterruptedException if interrupted
	 */
	private static void test4() throws IOException, InterruptedException {
		Sender sender = Sender.getInstance();

		sender.setButton1Listener(new ButtonListener() {
			@Override
			public void handleButtonUp() {
				System.out.println("Button 1 up");
			}

			@Override
			public void handleButtonDown() {
				System.out.println("Button 1 down");
			}
		});

		sender.setButton2Listener(new ButtonListener() {
			@Override
			public void handleButtonUp() {
				System.out.println("Button 2 up");
			}

			@Override
			public void handleButtonDown() {
				System.out.println("Button 2 down");
			}
		});

		sender.setButton1LongPressListener(new OnLongPressListener() {
			@Override
			public void handleLongTrigger() {
				System.out.println("Button 1 long press");
			}
		});

		sender.setButton2LongPressListener(new OnLongPressListener() {
			@Override
			public void handleLongTrigger() {
				System.out.println("Button 2 long press");
			}
		});

		while (true) {
			Thread.sleep(100); // MAGIC_NUMBER
		}
	}

	/**
	 * Test of sending single lob signal.
	 *
	 * @param duration the duration of the signal.
	 * @throws IOException connection issues
	 * @throws InterruptedException if interrupted
	 */
	private static void test5(final long duration) throws IOException, InterruptedException {
		Sender sender = Sender.getInstance();
		ChannelSender channelSender = sender.getChannelSender(0);

		channelSender.lob(100, duration, false); // MAGIC_NUMBER

		sender.close();
	}
}
