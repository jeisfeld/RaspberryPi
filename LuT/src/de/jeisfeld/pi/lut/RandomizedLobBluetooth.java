package de.jeisfeld.pi.lut;

import java.io.IOException;
import java.util.Random;

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
	private int mMinPower = 0;
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
	 * Constructor.
	 *
	 * @param message the triggering message.
	 * @throws IOException Connection issues.
	 */
	public RandomizedLobBluetooth(final ProcessingBluetoothMessage message) throws IOException {
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
	}

	@Override
	public void run() {
		mIsRunning = true;

		// variables for mode 1
		double cyclePoint = 0;

		// variables for mode 2-3
		Random random = new Random();
		long nextSignalChangeTime = System.currentTimeMillis();
		boolean isHighPower = false;
		double lastRunningProbability = 0;
		double lastAvgOffDuration = 0;
		double lastAvgOnDuration = 0;

		try {
			while (mIsRunning) {
				switch (mMode) {
				case 1:
					// Wave up and down
					int value = (int) ((1 - Math.cos(2 * Math.PI * cyclePoint)) / 2 * (mPower - mMinPower) + mMinPower);
					mChannelSender.lob(value);

					if (mCycleLength > 0) {
						cyclePoint = (Math.round(cyclePoint * 2 * mCycleLength) + 1.0) / 2 / mCycleLength;
					}
					else {
						cyclePoint = 0.5; // MAGIC_NUMBER
					}
					break;
				case 2:
					// Random change between high/low level. Avg signal duration 2s. Levels and Probability controllable.
					if (System.currentTimeMillis() > nextSignalChangeTime
							|| mRunningProbability != lastRunningProbability) {
						lastRunningProbability = mRunningProbability;
						long duration;
						try {
							duration = (int) (-AVERAGE_SIGNAL_DURATION * Math.log(random.nextFloat()));
						}
						catch (Exception e) {
							duration = Integer.MAX_VALUE;
						}
						nextSignalChangeTime = System.currentTimeMillis() + duration;
						isHighPower = random.nextDouble() < mRunningProbability;
					}

					mChannelSender.lob(isHighPower ? mPower : mMinPower);
					break;
				case 3: // MAGIC_NUMBER
					// Random change between on/off. On level and avg off/on duration controllable.
					if (System.currentTimeMillis() > nextSignalChangeTime // BOOLEAN_EXPRESSION_COMPLEXITY
							|| isHighPower && mAvgOnDuration != lastAvgOnDuration
							|| !isHighPower && mAvgOffDuration != lastAvgOffDuration) {
						lastAvgOffDuration = mAvgOffDuration;
						lastAvgOnDuration = mAvgOnDuration;
						if (System.currentTimeMillis() > nextSignalChangeTime) {
							isHighPower = !isHighPower;
						}
						double avgDuration = isHighPower ? mAvgOnDuration : mAvgOffDuration;
						int duration;
						try {
							duration = (int) (-avgDuration * Math.log(random.nextFloat()));
						}
						catch (Exception e) {
							duration = Integer.MAX_VALUE;
						}
						nextSignalChangeTime = System.currentTimeMillis() + duration;
					}

					mChannelSender.lob(isHighPower ? mPower : mMinPower);
					break;
				default:
					mChannelSender.lob(0);
					Thread.sleep(Sender.QUERY_DURATION);
					break;
				}

			}
			mChannelSender.lob(0);
		}
		catch (InterruptedException e) {
			Logger.error(e);
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
	public void sendStatus(final ConnectThread connectThread) {
		connectThread.write(new ProcessingBluetoothMessage(mChannel, false, mIsRunning, mPower, null, null, mMode,
				mMinPower, mCycleLength, mRunningProbability, mAvgOffDuration, mAvgOnDuration));

	}

}
