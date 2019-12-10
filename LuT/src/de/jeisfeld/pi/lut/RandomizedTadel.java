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
	 * The sender used for sending signals.
	 */
	private final ChannelSender mChannelSender;
	/**
	 * Flag indicating if the Lob is running.
	 */
	private boolean mIsRunning = false;
	/**
	 * Flag indicating if this handler is stopped.
	 */
	private boolean mIsStopped = false;

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
	private RandomizedTadel(final int channel) throws IOException {
		Sender sender = Sender.getInstance();
		sender.setButton2LongPressListener(new ShutdownListener());

		sender.setButton1Listener(new ButtonListener() {
			@Override
			public void handleButtonDown() {
				mIsRunning = !mIsRunning;
				if (!mIsRunning) {
					mChannelSender.tadel(0, 0, 0);
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
		int lastRunningProbability = 0;
		int extraPower = 0;
		try {
			while (true) {
				if (!mIsStopped) {
					Thread.sleep(Sender.QUERY_DURATION);
					extraPower = 0;
					continue;
				}

				ButtonStatus status = mChannelSender.getButtonStatus();
				int power = status.getControl1Value();
				int runningProbability = status.getControl2Value();
				int frequency = status.getControl3Value();

				if (System.currentTimeMillis() > nextSignalChangeTime || Math.abs(runningProbability - lastRunningProbability) > 10) { // MAGIC_NUMBER
					lastRunningProbability = runningProbability;
					long duration = (int) (-RandomizedTadel.AVERAGE_SIGNAL_DURATION * Math.log(random.nextFloat()));
					nextSignalChangeTime = System.currentTimeMillis() + duration;
					isHighPower = random.nextInt(ButtonStatus.MAX_CONTROL_VALUE) < runningProbability;
					if (isHighPower) {
						extraPower++;
					}
				}
				mChannelSender.tadel(isHighPower ? power + extraPower : 0, frequency, 0);
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
