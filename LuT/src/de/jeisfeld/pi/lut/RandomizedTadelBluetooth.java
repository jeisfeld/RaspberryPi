package de.jeisfeld.pi.lut;

import java.io.IOException;
import java.util.Random;

import de.jeisfeld.lut.bluetooth.message.Mode;
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
	 * The bluetooth connect thread.
	 */
	private final ConnectThread mConnectThread;
	/**
	 * The current running mode.
	 */
	private Mode mMode = Mode.OFF;
	/**
	 * The power.
	 */
	private int mPower = 0;
	/**
	 * The min power (as percentage of power).
	 */
	private double mMinPower = 0;
	/**
	 * The power change duration.
	 */
	private long mPowerChangeDuration = 0;
	/**
	 * The frequency.
	 */
	private int mFrequency = 0;
	/**
	 * The wave.
	 */
	private int mWave = 0;
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
	 * The pulse duration.
	 */
	private long mPulseDuration = 1;
	/**
	 * Flag indicating if power is on.
	 */
	private boolean mIsPowered = false;
	/**
	 * Flag indicating that system is in manual override mode.
	 */
	private boolean mIsManualOverride = false;
	/**
	 * Flag indicating that manual override is removed.
	 */
	private boolean mRemoveManualOverride = false;

	/**
	 * Constructor.
	 *
	 * @param message the triggering message.
	 * @param connectThread the bluetooth connect thread.
	 * @throws IOException Connection issues.
	 */
	public RandomizedTadelBluetooth(final ProcessingBluetoothMessage message, final ConnectThread connectThread) throws IOException {
		mConnectThread = connectThread;
		mChannel = message.getChannel();
		Sender sender = Sender.getInstance();
		mChannelSender = sender.getChannelSender(mChannel);
		updateValues(message);
	}

	@Override
	public void updateValues(final ProcessingBluetoothMessage message) {
		if (message.getMode() != null) {
			if (message.getMode() == Mode.MANUAL_OVERRIDE) {
				if (mMode == Mode.RANDOM_1 || mMode == Mode.RANDOM_2) {
					mIsManualOverride = message.getPulseDuration() > 0;
					if (mIsManualOverride) {
						mIsPowered = message.isHighPower();
					}
					else {
						mRemoveManualOverride = true;
					}
				}
			}
			else {
				mMode = message.getMode();
			}
			if (mMode == Mode.PULSE && message.isHighPower() != null) {
				mIsPowered = message.isHighPower();
			}
		}
		if (message.getPower() != null) {
			mPower = message.getPower();
		}
		if (message.getMinPower() != null) {
			mMinPower = message.getMinPower();
		}
		if (message.getPowerChangeDuration() != null) {
			mPowerChangeDuration = message.getPowerChangeDuration();
		}
		if (message.getFrequency() != null) {
			mFrequency = message.getFrequency();
		}
		if (message.getWave() != null) {
			mWave = message.getWave();
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
		if (message.getPulseDuration() != null) {
			mPulseDuration = message.getPulseDuration();
		}
	}

	@Override
	public void run() {
		mIsRunning = true;

		Random random = new Random();
		long nextSignalChangeTime = System.currentTimeMillis();
		double lastBluetoothMessageTime = 0;
		double lastRunningProbability = 0;
		double lastAvgOffDuration = 0;
		double lastAvgOnDuration = 0;

		try {
			while (mIsRunning) {
				int powerBefore = mPower;
				boolean isPoweredBefore = mIsPowered;
				if (mRemoveManualOverride) {
					nextSignalChangeTime = System.currentTimeMillis();
					mRemoveManualOverride = false;
				}
				switch (mMode) {
				case FIXED:
					// constant power and frequency, both controllable. Serves to prepare base power for modes 2 and 3.
					mPower = getUpdatedPower(mPower, mPowerChangeDuration);
					mChannelSender.tadel(mPower, mFrequency, mWave);
					mIsPowered = true;
					break;
				case RANDOM_1:
					// Random change between on/off level. Avg signal duration 2s. Power, frequency and Probability controllable.
					if (System.currentTimeMillis() > nextSignalChangeTime
							|| mRunningProbability != lastRunningProbability
							|| mIsManualOverride) {
						lastRunningProbability = mRunningProbability;
						long duration;
						try {
							duration = (int) (-AVERAGE_SIGNAL_DURATION * Math.log(random.nextFloat()));
						}
						catch (Exception e) {
							duration = Integer.MAX_VALUE;
						}
						nextSignalChangeTime = System.currentTimeMillis() + duration;
						if (!mIsManualOverride && !mRemoveManualOverride) {
							mIsPowered = random.nextDouble() < mRunningProbability;
						}
						mRemoveManualOverride = false;
					}
					mPower = getUpdatedPower(mPower, mPowerChangeDuration);
					mChannelSender.tadel(getEffectivePower(mIsPowered, mPower, mMinPower), mFrequency, mWave);
					break;
				case RANDOM_2: // MAGIC_NUMBER
					// Random change between on/off. On level and avg off/on duration controllable.
					if (System.currentTimeMillis() > nextSignalChangeTime // BOOLEAN_EXPRESSION_COMPLEXITY
							|| mIsPowered && mAvgOnDuration != lastAvgOnDuration
							|| !mIsPowered && mAvgOffDuration != lastAvgOffDuration
							|| mIsManualOverride) {
						lastAvgOffDuration = mAvgOffDuration;
						lastAvgOnDuration = mAvgOnDuration;
						if (System.currentTimeMillis() > nextSignalChangeTime && !mIsManualOverride) {
							mIsPowered = !mIsPowered;
						}
						double avgDuration = mIsPowered ? mAvgOnDuration : mAvgOffDuration; // MAGIC_NUMBER
						int duration;
						try {
							duration = (int) (-avgDuration * Math.log(random.nextFloat()));
						}
						catch (Exception e) {
							duration = Integer.MAX_VALUE;
						}
						nextSignalChangeTime = System.currentTimeMillis() + duration;
					}
					mPower = getUpdatedPower(mPower, mPowerChangeDuration);
					mChannelSender.tadel(getEffectivePower(mIsPowered, mPower, mMinPower), mFrequency, mWave);
					break;
				case PULSE:
					if (mIsPowered) {
						if (mPulseDuration > 0) {
							if (mPulseDuration == Long.MAX_VALUE) {
								nextSignalChangeTime = Long.MAX_VALUE;
							}
							else {
								nextSignalChangeTime = System.currentTimeMillis() + mPulseDuration;
							}
							mPulseDuration = 0;
						}
						else if (System.currentTimeMillis() > nextSignalChangeTime) {
							mIsPowered = false;
						}
					}
					else {
						mPulseDuration = 0;
						nextSignalChangeTime = 0;
					}
					mPower = getUpdatedPower(mPower, mPowerChangeDuration);
					mChannelSender.tadel(getEffectivePower(mIsPowered, mPower, mMinPower), mFrequency, mWave);
					break;
				default:
					mPower = 0;
					mPowerBaseTime = System.currentTimeMillis();
					mChannelSender.tadel(0, 0, 0, 0, true);
					Thread.sleep(Sender.QUERY_DURATION);
					break;
				}
				if (mPower != powerBefore || mIsPowered != isPoweredBefore
						|| System.currentTimeMillis() - lastBluetoothMessageTime > 5000) { // MAGIC_NUMBER
					mConnectThread.write(new ProcessingBluetoothMessage(
							mChannel, true, null, mPower, null, null, null, null, mIsPowered, null, null, null, null, null, null));
					lastBluetoothMessageTime = System.currentTimeMillis();
				}
			}
			mChannelSender.tadel(0, 0, 0, 0, true);
		}
		catch (InterruptedException e) {
			Logger.error(e);
		}
		finally {
			mConnectThread.write(new ProcessingBluetoothMessage(
					mChannel, true, null, 0, null, null, null, null, false, null, null, null, null, null, null));
		}
	}

	/**
	 * Update the power based on previous power and power change duration. Serves to use control for controlling dynamic change.
	 *
	 * @param oldPower The old power.
	 * @param powerChangeDuration The power change duration.
	 * @return The new power.
	 */
	private int getUpdatedPower(final int oldPower, final long powerChangeDuration) {
		if (powerChangeDuration == 0) {
			mPowerBaseTime = System.currentTimeMillis();
			return oldPower;
		}
		int newPower = oldPower;
		// int millisUntilChange = getMillisUntilChange(controlPower);
		if (System.currentTimeMillis() - mPowerBaseTime > Math.abs(powerChangeDuration)) {
			newPower += (int) ((System.currentTimeMillis() - mPowerBaseTime) / powerChangeDuration);
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
	 * Returns the effective power to be sent in randomized mode.
	 *
	 * @param isPowered The flag indicating if power is on.
	 * @param power The configured power.
	 * @param minPower The min power (as percentage of configured power).
	 * @return The effective power.
	 */
	private int getEffectivePower(final boolean isPowered, final int power, final double minPower) {
		return isPowered ? power : Math.max((int) (minPower * power), 1);
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
	public void sendStatus() {
		mConnectThread.write(new ProcessingBluetoothMessage(mChannel, true, mIsRunning, mPower, mFrequency, mWave, mMode,
				null, null, mPowerChangeDuration, null, mRunningProbability, mAvgOffDuration, mAvgOnDuration, null));
	}

}
