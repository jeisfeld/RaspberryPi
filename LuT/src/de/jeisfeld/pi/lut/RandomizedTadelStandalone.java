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
 * Class used for sending randomized Tadel signals via LuT.
 */
public final class RandomizedTadelStandalone implements Runnable {
	/**
	 * The default channel.
	 */
	private static final int DEFAULT_CHANNEL = 0;
	/**
	 * The average duration of a signal.
	 */
	private static final long AVERAGE_SIGNAL_DURATION = 2000;
	/**
	 * The default frequency.
	 */
	private static final int DEFAULT_FREQUENCY = 100;
	/**
	 * The default wave.
	 */
	private static final int DEFAULT_WAVE = 0;
	/**
	 * The number of modes.
	 */
	private static final int MODE_COUNT = 4;

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
	 * Callback for mode change.
	 */
	private final OnModeChangeListener mListener;
	/**
	 * The base time for automatic power change.
	 */
	private long mPowerBaseTime = System.currentTimeMillis();

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

		new RandomizedTadelStandalone(channel).run();
	}

	/**
	 * Constructor.
	 *
	 * @param channel The channel where the signals should be sent.
	 * @throws IOException Connection issues.
	 */
	public RandomizedTadelStandalone(final int channel) throws IOException {
		this(channel, null);
	}

	/**
	 * Constructor.
	 *
	 * @param channel The channel where the signals should be sent.
	 * @param listener Callback for mode change.
	 * @throws IOException Connection issues.
	 */
	public RandomizedTadelStandalone(final int channel, final OnModeChangeListener listener) throws IOException {
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
				if (mListener != null) {
					mListener.onModeDetails(false, null, null, null, mMode, "", "");
				}
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
		boolean isPowered = false;
		int lastRunningProbability = 0;
		int lastOnDurationInput = 0;
		int lastOffDurationInput = 0;
		int power = 0;

		try {
			while (!mIsStopped) {
				ButtonStatus status = mChannelSender.getButtonStatus();
				int controlPower = status.getControl1Value();
				int runningProbability = status.getControl2Value();
				int frequency = status.getControl3Value();

				switch (mMode) {
				case 1:
					// constant power and frequency, both controllable. Serves to prepare base power for modes 2 and 3.
					power = controlPower;
					mListener.onModeDetails(true, power, frequency, DEFAULT_WAVE, mMode, "Fixed", "");
					mChannelSender.tadel(power, frequency, DEFAULT_WAVE);
					mPowerBaseTime = System.currentTimeMillis();
					break;
				case 2:
					// Random change between on/off level. Avg signal duration 2s. Power, frequency and Probability controllable.
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
						isPowered = random.nextInt(ButtonStatus.MAX_CONTROL_VALUE) < runningProbability;
					}

					power = getUpdatedPower(power, controlPower);

					mListener.onModeDetails(isPowered, power, frequency, DEFAULT_WAVE, mMode,
							"Avg Duration 2 sec",
							"Power time: " + String.format("%.1fs", (double) getMillisUntilChange(controlPower) / 1000) // MAGIC_NUMBER
									+ "\nPower direction: " + getChangeDirection(controlPower)
									+ "\nOn Probability: "
									+ String.format("%.3f", (double) runningProbability / ButtonStatus.MAX_CONTROL_VALUE));

					mChannelSender.tadel(isPowered ? power : 0, frequency, DEFAULT_WAVE);
					break;
				case 3: // MAGIC_NUMBER
					// Random change between on/off. On level and avg off/on duration controllable.
					status = mChannelSender.getButtonStatus();
					int onDurationInput = runningProbability;
					int offDurationInput = frequency;
					double avgOnDuration = Math.exp(0.016 * onDurationInput); // MAGIC_NUMBER seconds
					double avgOffDuration = Math.exp(0.016 * offDurationInput); // MAGIC_NUMBER seconds

					if (System.currentTimeMillis() > nextSignalChangeTime // BOOLEAN_EXPRESSION_COMPLEXITY
							|| isPowered && Math.abs(onDurationInput - lastOnDurationInput) > 2
							|| !isPowered && Math.abs(offDurationInput - lastOffDurationInput) > 2) {
						lastOnDurationInput = onDurationInput;
						lastOffDurationInput = offDurationInput;
						if (System.currentTimeMillis() > nextSignalChangeTime) {
							isPowered = !isPowered;
						}
						double avgDuration = 1000 * (isPowered ? avgOnDuration : avgOffDuration); // MAGIC_NUMBER
						int duration;
						try {
							duration = (int) (-avgDuration * Math.log(random.nextFloat()));
						}
						catch (Exception e) {
							duration = Integer.MAX_VALUE;
						}
						nextSignalChangeTime = System.currentTimeMillis() + duration;
					}
					power = getUpdatedPower(power, controlPower);

					mListener.onModeDetails(isPowered, power, DEFAULT_FREQUENCY, DEFAULT_WAVE, mMode,
							"Random On/Off",
							"Power time: " + String.format("%.1fs", (double) getMillisUntilChange(controlPower) / 1000) // MAGIC_NUMBER
									+ "\nPower direction: " + getChangeDirection(controlPower)
									+ "\nAvg On duration: " + String.format("%.1fs", avgOnDuration)
									+ "\nAvg Off duration: " + String.format("%.1fs", avgOffDuration));

					mChannelSender.tadel(isPowered ? power : 0, DEFAULT_FREQUENCY, DEFAULT_WAVE);
					break;
				default:
					mChannelSender.tadel(0, 0, 0, 0, true);
					mListener.onModeDetails(false, 0, null, null, mMode, "Off", "");
					Thread.sleep(Sender.QUERY_DURATION);
					mPowerBaseTime = System.currentTimeMillis();
					break;
				}
			}
			mChannelSender.tadel(0, 0, 0, 0, true);
		}
		catch (InterruptedException e) {
			Logger.error(e);
		}
	}

	/**
	 * Update the power based on previous power and power control status. Serves to use control for controlling dynamic change.
	 *
	 * @param oldPower The old power.
	 * @param controlPower The value of power control.
	 * @return The new power.
	 */
	private int getUpdatedPower(final int oldPower, final int controlPower) {
		int newPower = oldPower;
		int millisUntilChange = getMillisUntilChange(controlPower);
		if (System.currentTimeMillis() - mPowerBaseTime > millisUntilChange) {
			newPower += getChangeDirection(controlPower) * ((System.currentTimeMillis() - mPowerBaseTime) / millisUntilChange);
			mPowerBaseTime = System.currentTimeMillis();
		}
		if (newPower > ButtonStatus.MAX_CONTROL_VALUE) {
			newPower = ButtonStatus.MAX_CONTROL_VALUE;
		}
		else if (newPower < 0) {
			newPower = 0;
		}
		return newPower;
	}

	/**
	 * Get the change direction.
	 *
	 * @param controlPower The value of power control.
	 * @return The milliseconds until change.
	 */
	private int getChangeDirection(final int controlPower) {
		return (int) Math.signum((int) ((controlPower - 127) / 16.0)); // MAGIC_NUMBER 0 in middle range, otherwise +-1
	}

	/**
	 * Get milliseconds until change.
	 *
	 * @param controlPower The value of power control.
	 * @return The milliseconds until change.
	 */
	private int getMillisUntilChange(final int controlPower) {
		return (int) (150000 / Math.pow(1.04, Math.abs(controlPower - 127))); // MAGIC_NUMBER ca. 1 minute to 1 second
	}

	/**
	 * Stop this handler.
	 */
	public void stop() {
		mIsStopped = true;
	}
}
