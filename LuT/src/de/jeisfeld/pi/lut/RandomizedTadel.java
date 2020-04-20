package de.jeisfeld.pi.lut;

import java.io.IOException;
import java.util.Random;

import de.jeisfeld.pi.lut.core.ButtonStatus;
import de.jeisfeld.pi.lut.core.ButtonStatus.ButtonListener;
import de.jeisfeld.pi.lut.core.ChannelSender;
import de.jeisfeld.pi.lut.core.Sender;
import de.jeisfeld.pi.lut.core.ShutdownListener;
import de.jeisfeld.pi.util.Logger;

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
	 * The default frequency.
	 */
	private static final int DEFAULT_FREQUENCY = 255;
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
					mChannelSender.tadel(power, frequency, RandomizedTadel.DEFAULT_WAVE);
					mPowerBaseTime = System.currentTimeMillis();
					break;
				case 2:
					// Random change between on/off level. Avg signal duration 2s. Power, frequency and Probability controllable.
					if (System.currentTimeMillis() > nextSignalChangeTime
							|| Math.abs(runningProbability - lastRunningProbability) > 2) {
						lastRunningProbability = runningProbability;
						long duration;
						try {
							duration = (int) (-RandomizedTadel.AVERAGE_SIGNAL_DURATION * Math.log(random.nextFloat()));
						}
						catch (Exception e) {
							duration = Integer.MAX_VALUE;
						}
						nextSignalChangeTime = System.currentTimeMillis() + duration;
						isPowered = random.nextInt(ButtonStatus.MAX_CONTROL_VALUE) < runningProbability;
					}

					power = getUpdatedPower(power, controlPower);
					mChannelSender.tadel(isPowered ? power : 0, frequency, RandomizedTadel.DEFAULT_WAVE);
					break;
				case 3: // MAGIC_NUMBER
					// Random change between on/off. On level and avg off/on duration controllable.
					status = mChannelSender.getButtonStatus();
					int onDurationInput = runningProbability;
					int offDurationInput = frequency;

					if (System.currentTimeMillis() > nextSignalChangeTime // BOOLEAN_EXPRESSION_COMPLEXITY
							|| isPowered && Math.abs(onDurationInput - lastOnDurationInput) > 2
							|| !isPowered && Math.abs(offDurationInput - lastOffDurationInput) > 2) {
						lastOnDurationInput = onDurationInput;
						lastOffDurationInput = offDurationInput;
						if (System.currentTimeMillis() > nextSignalChangeTime) {
							isPowered = !isPowered;
						}
						double avgDuration = 1000 * Math.exp(0.016 * (isPowered ? onDurationInput : offDurationInput)); // MAGIC_NUMBER
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
					mChannelSender.tadel(isPowered ? power : 0, RandomizedTadel.DEFAULT_FREQUENCY, RandomizedTadel.DEFAULT_WAVE);
					break;
				default:
					mChannelSender.tadel(0, 0, 0);
					Thread.sleep(Sender.QUERY_DURATION);
					mPowerBaseTime = System.currentTimeMillis();
					break;
				}
			}
			mChannelSender.tadel(0, 0, 0);
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
		int deltaSgn = (int) Math.signum((int) ((controlPower - 127) / 16.0)); // MAGIC_NUMBER 0 in middle range, otherwise +-1
		int millisUntilChange = (int) (150000 / Math.pow(1.04, Math.abs(controlPower - 127))); // MAGIC_NUMBER ca. 1 minute to 1 second
		if (System.currentTimeMillis() - mPowerBaseTime > millisUntilChange) {
			newPower += deltaSgn * ((System.currentTimeMillis() - mPowerBaseTime) / millisUntilChange);
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
	 * Stop this handler.
	 */
	public void stop() {
		mIsStopped = true;
	}
}
