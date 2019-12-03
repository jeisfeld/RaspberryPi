package de.jeisfeld.pi.lut;

import java.io.IOException;
import java.util.Random;

import de.jeisfeld.pi.lut.core.ButtonStatus;
import de.jeisfeld.pi.lut.core.ButtonStatus.ButtonListener;
import de.jeisfeld.pi.lut.core.ChannelSender;
import de.jeisfeld.pi.lut.core.Sender;
import de.jeisfeld.pi.lut.core.ShutdownListener;

/**
 * Class used for sending randomized Lob signals via LuT.
 */
public final class RandomizedLob implements Runnable {
	/**
	 * The default channel.
	 */
	private static final int DEFAULT_CHANNEL = 0;
	/**
	 * The average duration of a signal.
	 */
	private static final long AVERAGE_SIGNAL_DURATION = 2000;
	/**
	 * The sender used for sending signals.
	 */
	private final ChannelSender mChannelSender;
	/**
	 * Flag indicating if the Lob is running.
	 */
	private boolean mIsRunning = false;

	/**
	 * Main method.
	 *
	 * @param args The command line arguments (not required).
	 * @throws IOException connection issues
	 */
	public static void main(final String[] args) throws IOException { // SUPPRESS_CHECKSTYLE
		int channel = RandomizedLob.DEFAULT_CHANNEL;

		if (args.length > 0) {
			channel = Integer.parseInt(args[0]);
		}

		new RandomizedLob(channel).run();
	}

	/**
	 * Constructor.
	 *
	 * @param channel The channel where the signals should be sent.
	 * @throws IOException Connection issues.
	 */
	private RandomizedLob(final int channel) throws IOException {
		Sender sender = Sender.getInstance();
		sender.setButton2Listener(new ShutdownListener());

		sender.setButton1Listener(new ButtonListener() {

			@Override
			public void handleButtonUp() {
				// do nothing
			}

			@Override
			public void handleButtonDown() {
				mIsRunning = !mIsRunning;
				if (!mIsRunning) {
					try {
						mChannelSender.lob(0);
					}
					catch (IOException | InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});

		mChannelSender = sender.getChannelSender(channel);
	}

	@Override
	public void run() {
		Random random = new Random();
		long nextSignalChangeTime = System.currentTimeMillis();
		boolean isHighPower = false;
		int lastFrequency = 0;
		try {
			while (true) {
				if (!mIsRunning) {
					Thread.sleep(Sender.QUERY_DURATION);
					continue;
				}

				ButtonStatus status = mChannelSender.getButtonStatus();
				int maxPower = status.getControl1Value();
				int frequency = status.getControl2Value();
				int basePower = status.getControl3Value() * maxPower / ButtonStatus.MAX_CONTROL_VALUE;

				if (System.currentTimeMillis() > nextSignalChangeTime || Math.abs(frequency - lastFrequency) > 10) { // MAGIC_NUMBER
					lastFrequency = frequency;
					long duration = (int) (-RandomizedLob.AVERAGE_SIGNAL_DURATION * Math.log(random.nextFloat()));
					nextSignalChangeTime = System.currentTimeMillis() + duration;
					isHighPower = random.nextInt(ButtonStatus.MAX_CONTROL_VALUE) < frequency;
				}

				mChannelSender.lob(isHighPower ? maxPower : basePower);
			}
		}
		catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}

}
