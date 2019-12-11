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
public final class RandomizedTadel implements Runnable {
	/**
	 * The default channel.
	 */
	private static final int DEFAULT_CHANNEL = 0;
	/**
	 * The average duration of a signal.
	 */
	private static final long AVERAGE_SIGNAL_DURATION = 2000;
	/**
	 * The number of modes.
	 */
	private static final int MODE_COUNT = 3;

	/**
	 * The sender used for sending signals.
	 */
	private final ChannelSender mChannelSender;
	/**
	 * Flag indicating if this handler is stopped.
	 */
	private boolean mIsStopped = false;
	/**
	 * The current running mode.
	 */
	private int mMode = 0;

	/**
	 * Main method.
	 *
	 * @param args The command line arguments (not required).
	 * @throws IOException connection issues
	 */
	public static void main(final String[] args) throws IOException { // SUPPRESS_CHECKSTYLE
		int channel = RandomizedTadel.DEFAULT_CHANNEL;

		if (args.length > 0) {
			channel = Integer.parseInt(args[0]);
		}

		new RandomizedTadel(channel).run();
	}

	/**
	 * Constructor.
	 *
	 * @param channel The channel where the signals should be sent.
	 * @throws IOException Connection issues.
	 */
	public RandomizedTadel(final int channel) throws IOException {
		Sender sender = Sender.getInstance();
		sender.setButton2LongPressListener(new ShutdownListener());

		mChannelSender = sender.getChannelSender(channel);
	}

	/**
	 * Set the special button listeners.
	 */
	public void setButtonListeners() {
		mChannelSender.setButton1Listener(new ButtonListener() {
			@Override
			public void handleButtonDown() {
				mMode = (mMode + 1) % RandomizedTadel.MODE_COUNT;
			}
		});
	}

	@Override
	public void run() {
		setButtonListeners();
		mIsStopped = false;
		mMode = 0;

		Random random = new Random();
		long nextSignalChangeTime = System.currentTimeMillis();
		long mode2BaseTime = System.currentTimeMillis();
		boolean isPowered = false;
		int lastRunningProbability = 0;
		int power = 0;

		try {
			while (!mIsStopped) {
				ButtonStatus status = mChannelSender.getButtonStatus();
				int buttonPower = status.getControl1Value();
				int runningProbability = status.getControl2Value();
				int frequency = status.getControl3Value();

				// Calculate if tadel should be on or off
				if (mMode > 0) {
					if (System.currentTimeMillis() > nextSignalChangeTime
							|| Math.abs(runningProbability - lastRunningProbability) > 2) {
						lastRunningProbability = runningProbability;
						long duration = (int) (-RandomizedTadel.AVERAGE_SIGNAL_DURATION * Math.log(random.nextFloat()));
						nextSignalChangeTime = System.currentTimeMillis() + duration;
						isPowered = random.nextInt(ButtonStatus.MAX_CONTROL_VALUE) < runningProbability;
					}
				}

				switch (mMode) {
				case 1:
					power = buttonPower;
					mChannelSender.tadel(isPowered ? power : 0, frequency, 0);
					mode2BaseTime = System.currentTimeMillis();
					break;
				case 2:
					int deltaSgn = (int) Math.signum((int) ((buttonPower - 127) / 16.0)); // MAGIC_NUMBER 0 in middle range, otherwise +-1
					int millisUntilChange = (int) (150000 / Math.pow(1.04, Math.abs(buttonPower - 127))); // MAGIC_NUMBER ca. 1 minute to 1 second
					if (System.currentTimeMillis() - mode2BaseTime > millisUntilChange) {
						power += deltaSgn * ((System.currentTimeMillis() - mode2BaseTime) / millisUntilChange);
						mode2BaseTime = System.currentTimeMillis();
					}
					mChannelSender.tadel(isPowered ? power : 0, frequency, 0);
					break;
				default:
					mChannelSender.tadel(0, 0, 0);
					Thread.sleep(Sender.QUERY_DURATION);
					mode2BaseTime = System.currentTimeMillis();
					break;
				}

			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Stop this handler.
	 */
	public void stop() {
		mIsStopped = true;
	}
}
