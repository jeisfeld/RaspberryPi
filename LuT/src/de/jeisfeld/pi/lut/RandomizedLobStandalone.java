package de.jeisfeld.pi.lut;

import java.io.IOException;
import java.util.Random;

import de.jeisfeld.pi.lut.Startup.OnModeChangeListener;
import de.jeisfeld.pi.lut.core.ButtonStatus;
import de.jeisfeld.pi.lut.core.ButtonStatus.ButtonListener;
import de.jeisfeld.pi.lut.core.ChannelSender;
import de.jeisfeld.pi.lut.core.Sender;
import de.jeisfeld.pi.util.Logger;

/**
 * Class used for sending randomized Lob signals via LuT.
 */
public final class RandomizedLobStandalone implements Runnable {
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
	private static final int MODE_COUNT = 4;

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
	 * Callback for mode change.
	 */
	private final OnModeChangeListener mListener;

	/**
	 * Main method.
	 *
	 * @param args The command line arguments (not required).
	 * @throws IOException connection issues
	 */
	public static void main(final String[] args) throws IOException { // SUPPRESS_CHECKSTYLE
		int channel = DEFAULT_CHANNEL;

		if (args.length > 0) {
			channel = Integer.parseInt(args[0]);
		}

		new RandomizedLobStandalone(channel).run();
	}

	/**
	 * Constructor.
	 *
	 * @param channel The channel where the signals should be sent.
	 * @throws IOException Connection issues.
	 */
	public RandomizedLobStandalone(final int channel) throws IOException {
		this(channel, null);
	}

	/**
	 * Constructor.
	 *
	 * @param channel The channel where the signals should be sent.
	 * @param listener Callback for mode change.
	 * @throws IOException Connection issues.
	 */
	public RandomizedLobStandalone(final int channel, final OnModeChangeListener listener) throws IOException {
		Sender sender = Sender.getInstance();
		mChannelSender = sender.getChannelSender(channel);
		mListener = listener;
	}

	/**
	 * Set the special button listeners.
	 */
	public void setButtonListeners() {
		mChannelSender.setButton1Listener(new ButtonListener() {
			@Override
			public void handleButtonDown() {
				mMode = (mMode + 1) % MODE_COUNT;
				signal(mMode + 1, false);
				if (mListener != null) {
					mListener.onModeDetails(false, null, null, null, mMode, "", "");
				}
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
			mChannelSender.lob(0, SIGNAL_DURATION, false);
			for (int i = 0; i < count; i++) {
				mChannelSender.lob(SIGNAL_POWER, isLong ? LONG_SIGNAL_WAIT_DURATION : SIGNAL_DURATION, false);
				mChannelSender.lob(0, i == count - 1 ? LONG_SIGNAL_WAIT_DURATION : SIGNAL_DURATION, false);
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
		int power = 0;
		int minPower = 0;

		// variables for mode 1
		double cyclePoint = 0;

		// variables for mode 2-3
		Random random = new Random();
		long nextSignalChangeTime = System.currentTimeMillis();
		boolean isHighPower = false;
		int lastRunningProbability = 0;
		int lastOnDurationInput = 0;
		int lastOffDurationInput = 0;

		try {
			while (!mIsStopped) {
				if (!mIsRunning || mMode < 0) {
					Thread.sleep(Sender.QUERY_DURATION);
					continue;
				}
				status = mChannelSender.getButtonStatus();
				power = status.getControl1Value();
				minPower = (status.getControl3Value() * power) / ButtonStatus.MAX_CONTROL_VALUE;

				switch (mMode) {
				case 1:
					// Wave up and down
					int cycleLength = (status.getControl2Value() + 9) / 10; // MAGIC_NUMBER

					int value = (int) ((1 - Math.cos(2 * Math.PI * cyclePoint)) / 2 * (power - minPower) + minPower);
					mListener.onModeDetails(value > 0, value, null, null, mMode,
							"Wave", "Length: " + cycleLength + "\nPower range: [" + minPower + "," + power + "]");
					mChannelSender.lob(value);

					if (cycleLength > 0) {
						cyclePoint = (Math.round(cyclePoint * 2 * cycleLength) + 1.0) / 2 / cycleLength;
					}
					else {
						cyclePoint = 0.5; // MAGIC_NUMBER
					}
					break;
				case 2:
					// Random change between high/low level. Avg signal duration 2s. Levels and Probability controllable.
					int runningProbability = status.getControl2Value();

					if (System.currentTimeMillis() > nextSignalChangeTime
							|| Math.abs(runningProbability - lastRunningProbability) > 2) {
						lastRunningProbability = runningProbability;
						long duration;
						try {
							duration = (int) (-AVERAGE_SIGNAL_DURATION * Math.log(random.nextFloat()));
						}
						catch (Exception e) {
							duration = Integer.MAX_VALUE;
						}
						nextSignalChangeTime = System.currentTimeMillis() + duration;
						isHighPower = random.nextInt(ButtonStatus.MAX_CONTROL_VALUE) < runningProbability;
					}

					mListener.onModeDetails(true, isHighPower ? power : minPower, null, null, mMode,
							"Avg Duration 2 sec", "Is High Power: " + isHighPower
									+ "\nHigh Power: " + power + "\nLow Power: " + minPower
									+ "\nHigh Probability: " + String.format("%.3f", (double) runningProbability / ButtonStatus.MAX_CONTROL_VALUE));

					mChannelSender.lob(isHighPower ? power : minPower);
					break;
				case 3: // MAGIC_NUMBER
					// Random change between on/off. On level and avg off/on duration controllable.
					int onDurationInput = status.getControl2Value();
					int offDurationInput = status.getControl3Value();
					double avgOnDuration = Math.exp(0.016 * status.getControl2Value()); // MAGIC_NUMBER seconds
					double avgOffDuration = Math.exp(0.016 * status.getControl3Value()); // MAGIC_NUMBER seconds

					if (System.currentTimeMillis() > nextSignalChangeTime // BOOLEAN_EXPRESSION_COMPLEXITY
							|| isHighPower && Math.abs(onDurationInput - lastOnDurationInput) > 2
							|| !isHighPower && Math.abs(offDurationInput - lastOffDurationInput) > 2) {
						lastOnDurationInput = onDurationInput;
						lastOffDurationInput = offDurationInput;
						if (System.currentTimeMillis() > nextSignalChangeTime) {
							isHighPower = !isHighPower;
						}
						double avgDuration = 1000 * (isHighPower ? avgOnDuration : avgOffDuration); // MAGIC_NUMBER milliseconds
						int duration;
						try {
							duration = (int) (-avgDuration * Math.log(random.nextFloat()));
						}
						catch (Exception e) {
							duration = Integer.MAX_VALUE;
						}
						nextSignalChangeTime = System.currentTimeMillis() + duration;
					}

					mListener.onModeDetails(isHighPower, power, null, null, mMode,
							"Random On/Off", "Avg On duration: " + String.format("%.1fs", avgOnDuration)
									+ "\nAvg Off duration: " + String.format("%.1fs", avgOffDuration));

					mChannelSender.lob(isHighPower ? power : 0);
					break;
				default:
					mChannelSender.lob(0, 0, true);
					mListener.onModeDetails(false, 0, null, null, mMode, "Off", "");
					Thread.sleep(Sender.QUERY_DURATION);
					break;
				}

			}
			mChannelSender.lob(0, 0, true);
		}
		catch (InterruptedException e) {
			Logger.error(e);
		}
	}

	/**
	 * Stop this handler.
	 */
	public void stop() {
		mIsStopped = true;
	}

}
