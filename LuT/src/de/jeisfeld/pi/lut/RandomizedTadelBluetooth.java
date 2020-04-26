package de.jeisfeld.pi.lut;

import java.io.IOException;
import java.util.Random;

import de.jeisfeld.lut.bluetooth.message.ProcessingBluetoothMessage;
import de.jeisfeld.pi.bluetooth.ConnectThread;
import de.jeisfeld.pi.lut.core.ChannelSender;
import de.jeisfeld.pi.lut.core.Sender;
import de.jeisfeld.pi.util.Logger;

/**
 * Class used for sending randomized Tadel signals via LuT.
 */
public final class RandomizedTadelBluetooth implements BluetoothRunnable {
	/**
	 * The average duration of a signal.
	 */
	private static final long AVERAGE_SIGNAL_DURATION = 2000;
	/**
	 * The default wave.
	 */
	private static final int DEFAULT_WAVE = 0;
	/**
	 * The max power.
	 */
	private static final int MAX_POWER = 255;

	/**
	 * The channel.
	 */
	private final int mChannel;
	/**
	 * The sender used for sending signals.
	 */
	private final ChannelSender mChannelSender;
	/**
	 * Flag indicating if the Lob is running.
	 */
	private boolean mIsRunning = false;
	/**
	 * The current running mode.
	 */
	private int mMode = 0;
	/**
	 * The power.
	 */
	private int mPower = 0;
	/**
	 * The min power.
	 */
	private int mControlPower = 0;
	/**
	 * The frequency.
	 */
	private int mFrequency = 0;
	/**
	 * The running probability.
	 */
	private double mRunningProbability = 0;
	/**
	 * The average on duration.
	 */
	private long mAvgOnDuration = 1;
	/**
	 * The average off duration.
	 */
	private long mAvgOffDuration = 1;
	/**
	 * The base time for automatic power change.
	 */
	private long mPowerBaseTime = System.currentTimeMillis();

	/**
	 * Constructor.
	 *
	 * @param message the triggering message.
	 * @throws IOException Connection issues.
	 */
	public RandomizedTadelBluetooth(final ProcessingBluetoothMessage message) throws IOException {
		mChannel = message.getChannel();
		Sender sender = Sender.getInstance();
		mChannelSender = sender.getChannelSender(mChannel);
		updateValues(message);
	}

	@Override
	public void updateValues(final ProcessingBluetoothMessage message) {
		if (message.getMode() != null) {
			mMode = message.getMode();
		}
		if (message.getPower() != null) {
			mControlPower = message.getPower();
		}
		if (message.getFrequency() != null) {
			mFrequency = message.getFrequency();
		}
		if (message.getRunningProbability() != null) {
			mRunningProbability = message.getRunningProbability();
		}
		if (message.getAvgOffDuration() != null) {
			mAvgOffDuration = message.getAvgOffDuration();
		}
		if (message.getAvgOnDuration() != null) {
			mAvgOnDuration = message.getAvgOnDuration();
		}
	}

	@Override
	public void run() {
		mIsRunning = true;

		Random random = new Random();
		long nextSignalChangeTime = System.currentTimeMillis();
		boolean isPowered = false;
		double lastRunningProbability = 0;
		double lastAvgOffDuration = 0;
		double lastAvgOnDuration = 0;

		try {
			while (mIsRunning) {
				switch (mMode) {
				case 1:
					// constant power and frequency, both controllable. Serves to prepare base power for modes 2 and 3.
					mPower = mControlPower;
					mChannelSender.tadel(mPower, mFrequency, DEFAULT_WAVE);
					mPowerBaseTime = System.currentTimeMillis();
					break;
				case 2:
					// Random change between on/off level. Avg signal duration 2s. Power, frequency and Probability controllable.
					if (System.currentTimeMillis() > nextSignalChangeTime || mRunningProbability != lastRunningProbability) {
						lastRunningProbability = mRunningProbability;
						long duration;
						try {
							duration = (int) (-AVERAGE_SIGNAL_DURATION * Math.log(random.nextFloat()));
						}
						catch (Exception e) {
							duration = Integer.MAX_VALUE;
						}
						nextSignalChangeTime = System.currentTimeMillis() + duration;
						isPowered = random.nextDouble() < mRunningProbability;
					}

					mPower = getUpdatedPower(mPower, mControlPower);
					mChannelSender.tadel(isPowered ? mPower : 0, mFrequency, DEFAULT_WAVE);
					break;
				case 3: // MAGIC_NUMBER
					// Random change between on/off. On level and avg off/on duration controllable.
					if (System.currentTimeMillis() > nextSignalChangeTime // BOOLEAN_EXPRESSION_COMPLEXITY
							|| isPowered && mAvgOnDuration != lastAvgOnDuration
							|| !isPowered && mAvgOffDuration != lastAvgOffDuration) {
						lastAvgOffDuration = mAvgOffDuration;
						lastAvgOnDuration = mAvgOnDuration;
						if (System.currentTimeMillis() > nextSignalChangeTime) {
							isPowered = !isPowered;
						}
						double avgDuration = isPowered ? mAvgOnDuration : mAvgOffDuration; // MAGIC_NUMBER
						int duration;
						try {
							duration = (int) (-avgDuration * Math.log(random.nextFloat()));
						}
						catch (Exception e) {
							duration = Integer.MAX_VALUE;
						}
						nextSignalChangeTime = System.currentTimeMillis() + duration;
					}
					mPower = getUpdatedPower(mPower, mControlPower);
					mChannelSender.tadel(isPowered ? mPower : 0, mFrequency, DEFAULT_WAVE);
					break;
				default:
					mChannelSender.tadel(0, 0, 0, 0, true);
					Thread.sleep(Sender.QUERY_DURATION);
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
		if (newPower > MAX_POWER) {
			newPower = MAX_POWER;
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

	@Override
	public void stop() {
		mIsRunning = false;
	}

	@Override
	public boolean isRunning() {
		return mIsRunning;
	}

	@Override
	public void sendStatus(final ConnectThread connectThread) {
		connectThread.write(new ProcessingBluetoothMessage(mChannel, true, mIsRunning, mControlPower, mFrequency, null, mMode,
				null, null, mRunningProbability, mAvgOffDuration, mAvgOnDuration));

	}

}
