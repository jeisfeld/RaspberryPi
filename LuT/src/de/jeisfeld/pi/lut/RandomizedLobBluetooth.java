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
 * Class used for sending randomized Lob signals via LuT.
 */
public final class RandomizedLobBluetooth implements BluetoothRunnable {
	/**
	 * The average duration of a signal.
	 */
	private static final long AVERAGE_SIGNAL_DURATION = 2000;

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
	 * The cycle length.
	 */
	private int mCycleLength = 0;
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
	 * The pulse duration.
	 */
	private long mPulseDuration = 1;
	/**
	 * Flag indicating if power is high.
	 */
	private boolean mIsHighPower = false;

	/**
	 * Constructor.
	 *
	 * @param message the triggering message.
	 * @param connectThread the bluetooth connect thread.
	 * @throws IOException Connection issues.
	 */
	public RandomizedLobBluetooth(final ProcessingBluetoothMessage message, final ConnectThread connectThread) throws IOException {
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
					mIsHighPower = !mIsHighPower;
				}
			}
			else {
				mMode = message.getMode();
			}
			if (mMode == Mode.PULSE && message.isHighPower() != null) {
				mIsHighPower = message.isHighPower();
			}
		}
		if (message.getPower() != null) {
			mPower = message.getPower();
		}
		if (message.getMinPower() != null) {
			mMinPower = message.getMinPower();
		}
		if (message.getCycleLength() != null) {
			mCycleLength = message.getCycleLength();
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

		// variables for mode 1
		double cyclePoint = 0;

		// variables for mode 2-3
		Random random = new Random();
		long nextSignalChangeTime = System.currentTimeMillis();
		double lastBluetoothMessageTime = 0;
		double lastRunningProbability = 0;
		double lastAvgOffDuration = 0;
		double lastAvgOnDuration = 0;

		try {
			while (mIsRunning) {
				int powerBefore = mPower;
				boolean isPoweredBefore = mIsHighPower;
				switch (mMode) {
				case WAVE:
					// Wave up and down
					int value = (int) ((1 - Math.cos(2 * Math.PI * cyclePoint)) / 2 * mPower * (1 - mMinPower) + mPower * mMinPower);
					mChannelSender.lob(value);

					if (mCycleLength > 0) {
						cyclePoint = (Math.round(cyclePoint * 2 * mCycleLength) + 1.0) / 2 / mCycleLength;
					}
					else {
						cyclePoint = 0.5; // MAGIC_NUMBER
					}
					mIsHighPower = true;
					break;
				case RANDOM_1:
					// Random change between high/low level. Avg signal duration 2s. Levels and Probability controllable.
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
						mIsHighPower = random.nextDouble() < mRunningProbability;
					}

					mChannelSender.lob(mIsHighPower ? mPower : (int) (mMinPower * mPower));
					break;
				case RANDOM_2: // MAGIC_NUMBER
					// Random change between on/off. On level and avg off/on duration controllable.
					if (System.currentTimeMillis() > nextSignalChangeTime // BOOLEAN_EXPRESSION_COMPLEXITY
							|| mIsHighPower && mAvgOnDuration != lastAvgOnDuration
							|| !mIsHighPower && mAvgOffDuration != lastAvgOffDuration) {
						lastAvgOffDuration = mAvgOffDuration;
						lastAvgOnDuration = mAvgOnDuration;
						if (System.currentTimeMillis() > nextSignalChangeTime) {
							mIsHighPower = !mIsHighPower;
						}
						double avgDuration = mIsHighPower ? mAvgOnDuration : mAvgOffDuration;
						int duration;
						try {
							duration = (int) (-avgDuration * Math.log(random.nextFloat()));
						}
						catch (Exception e) {
							duration = Integer.MAX_VALUE;
						}
						nextSignalChangeTime = System.currentTimeMillis() + duration;
					}

					mChannelSender.lob(mIsHighPower ? mPower : (int) (mMinPower * mPower));
					break;
				case PULSE:
					if (mIsHighPower) {
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
							mIsHighPower = false;
						}
					}
					else {
						mPulseDuration = 0;
						nextSignalChangeTime = 0;
					}
					mChannelSender.lob(mIsHighPower ? mPower : (int) (mMinPower * mPower));
					break;
				default:
					mChannelSender.lob(0, 0, true);
					mIsHighPower = false;
					Thread.sleep(Sender.QUERY_DURATION);
					break;
				}
				if (mPower != powerBefore || mIsHighPower != isPoweredBefore
						|| System.currentTimeMillis() - lastBluetoothMessageTime > 5000) { // MAGIC_NUMBER
					mConnectThread.write(new ProcessingBluetoothMessage(
							mChannel, false, null, mPower, null, null, null, null, mIsHighPower, null, null, null, null, null, null));
					lastBluetoothMessageTime = System.currentTimeMillis();
				}
			}
			mChannelSender.lob(0, 0, true);
		}
		catch (InterruptedException e) {
			Logger.error(e);
		}
		finally {
			mConnectThread.write(new ProcessingBluetoothMessage(
					mChannel, false, null, 0, null, null, null, null, false, null, null, null, null, null, null));
		}
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
		mConnectThread.write(new ProcessingBluetoothMessage(mChannel, false, mIsRunning, mPower, null, null, mMode,
				mMinPower, null, null, mCycleLength, mRunningProbability, mAvgOffDuration, mAvgOnDuration, null));

	}

}
