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
	 * The power to be used for giving a signal.
	 */
	private static final int SIGNAL_POWER = 50;
	/**
	 * The duration of a signal.
	 */
	private static final int SIGNAL_DURATION = Sender.SEND_DURATION;
	/**
	 * The waiting time after the last signal.
	 */
	private static final int LONG_SIGNAL_WAIT_DURATION = 500;
	/**
	 * The number of modes.
	 */
	private static final int MODE_COUNT = 3;

	/**
	 * The sender used for sending signals.
	 */
	private final ChannelSender mChannelSender;
	/**
	 * Flag indicating if the Lob is running.
	 */
	private boolean mIsRunning = true;
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
	public RandomizedLob(final int channel) throws IOException {
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
				mMode = (mMode + 1) % RandomizedLob.MODE_COUNT;
				signal(mMode + 1, false);
			}
		});
	}

	/**
	 * Give a signal via vibrating.
	 *
	 * @param count The count of vibrations in the signal.
	 * @param isLong Indicator of long signal.
	 */
	public void signal(final int count, final boolean isLong) {
		mIsRunning = false;
		try {
			mChannelSender.lob(0, RandomizedLob.SIGNAL_DURATION);
			for (int i = 0; i < count; i++) {
				mChannelSender.lob(RandomizedLob.SIGNAL_POWER, isLong ? RandomizedLob.LONG_SIGNAL_WAIT_DURATION : RandomizedLob.SIGNAL_DURATION);
				mChannelSender.lob(0, i == count - 1 ? RandomizedLob.LONG_SIGNAL_WAIT_DURATION : RandomizedLob.SIGNAL_DURATION);
			}
		}
		catch (InterruptedException e) {
			// ignore
		}
		mIsRunning = true;
	}

	@Override
	public void run() {
		setButtonListeners();
		mIsStopped = false;
		mIsRunning = true;
		mMode = 0;

		ButtonStatus status;

		// variables for mode 1
		double cyclePoint = 0;

		// variables for mode 2
		Random random = new Random();
		long nextSignalChangeTime = System.currentTimeMillis();
		boolean isHighPower = false;
		int lastRunningProbability = 0;

		try {
			while (!mIsStopped) {
				if (!mIsRunning || mMode < 0) {
					Thread.sleep(Sender.QUERY_DURATION);
					continue;
				}

				switch (mMode) {
				case 1:
					status = mChannelSender.getButtonStatus();
					int power = status.getControl1Value();
					int frequency = status.getControl2Value();
					int minPower = (status.getControl3Value() * power) / ButtonStatus.MAX_CONTROL_VALUE;

					int value = (int) ((1 - Math.cos(2 * Math.PI * cyclePoint)) / 2 * (power - minPower) + minPower);
					mChannelSender.lob(value);

					if (frequency > 0) {
						int factor = (frequency + 9) / 10; // MAGIC_NUMBER
						cyclePoint = (Math.round(cyclePoint * 2 * factor) + 1.0) / 2 / factor;
					}
					else {
						cyclePoint = 0.5; // MAGIC_NUMBER
					}
					break;
				case 2:
					status = mChannelSender.getButtonStatus();
					int maxPower = status.getControl1Value(); // 180
					int runningProbability = status.getControl2Value(); // 75
					int basePower = status.getControl3Value() * maxPower / ButtonStatus.MAX_CONTROL_VALUE; // 25

					if (System.currentTimeMillis() > nextSignalChangeTime
							|| Math.abs(runningProbability - lastRunningProbability) > 10) { // MAGIC_NUMBER
						lastRunningProbability = runningProbability;
						long duration = (int) (-RandomizedLob.AVERAGE_SIGNAL_DURATION * Math.log(random.nextFloat()));
						nextSignalChangeTime = System.currentTimeMillis() + duration;
						isHighPower = random.nextInt(ButtonStatus.MAX_CONTROL_VALUE) < runningProbability;
					}
					mChannelSender.lob(isHighPower ? maxPower : basePower);
					break;
				default:
					mChannelSender.lob(0);
					Thread.sleep(Sender.QUERY_DURATION);
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
