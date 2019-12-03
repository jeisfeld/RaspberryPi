package de.jeisfeld.pi.lut;

import java.io.IOException;
import java.util.Random;

import de.jeisfeld.pi.lut.core.ButtonStatus;
import de.jeisfeld.pi.lut.core.ButtonStatus.ButtonListener;
import de.jeisfeld.pi.lut.core.ButtonStatus.OnLongPressListener;
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
	private RandomizedLob(final int channel) throws IOException {
		Sender sender = Sender.getInstance();
		sender.setButton2LongPressListener(new ShutdownListener());

		sender.setButton1Listener(new ButtonListener() {
			@Override
			public void handleButtonDown() {
				mMode = (mMode + 1) % RandomizedLob.MODE_COUNT;
				signal(mMode + 1);
			}
		});

		sender.setButton1LongPressListener(new OnLongPressListener() {

			@Override
			public void handleLongTrigger() {
				mMode = -1;
				mIsRunning = false;
				mChannelSender.lob(0);
			}
		});

		mChannelSender = sender.getChannelSender(channel);
	}

	/**
	 * Give a signal via vibrating.
	 *
	 * @param count The count of vibrations in the signal.
	 */
	private void signal(final int count) {
		mIsRunning = false;
		try {
			// mChannelSender.cleanupCommandQueue();
			mChannelSender.lob(0, RandomizedLob.SIGNAL_DURATION);
			for (int i = 0; i < count; i++) {
				mChannelSender.lob(RandomizedLob.SIGNAL_POWER, RandomizedLob.SIGNAL_DURATION);
				mChannelSender.lob(0, RandomizedLob.SIGNAL_DURATION);
			}
		}
		catch (InterruptedException e) {
			// ignore
		}
		mIsRunning = true;
	}

	@Override
	public void run() {
		Random random = new Random();
		long nextSignalChangeTime = System.currentTimeMillis();
		boolean isHighPower = false;
		int lastRunningProbability = 0;
		try {
			while (true) {
				if (!mIsRunning || mMode < 0) {
					Thread.sleep(Sender.QUERY_DURATION);
					continue;
				}

				ButtonStatus status = mChannelSender.getButtonStatus();
				int maxPower = status.getControl1Value(); // 180
				int runningProbability = status.getControl2Value(); // 75
				int basePower = status.getControl3Value() * maxPower / ButtonStatus.MAX_CONTROL_VALUE; // 25

				if (System.currentTimeMillis() > nextSignalChangeTime || Math.abs(runningProbability - lastRunningProbability) > 10) { // MAGIC_NUMBER
					lastRunningProbability = runningProbability;
					long duration = (int) (-RandomizedLob.AVERAGE_SIGNAL_DURATION * Math.log(random.nextFloat()));
					nextSignalChangeTime = System.currentTimeMillis() + duration;
					isHighPower = random.nextInt(ButtonStatus.MAX_CONTROL_VALUE) < runningProbability;
				}

				mChannelSender.lob(isHighPower ? maxPower : basePower);
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
