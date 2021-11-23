package de.jeisfeld.lut.bluetooth.message;

/**
 * A processing trigger message.
 */
public class ProcessingBluetoothMessage extends Message {
	/**
	 * The current channel.
	 */
	private final int mChannel;
	/**
	 * Flag indicating if it is Lob or Tadel.
	 */
	private final boolean mIsTadel;
	/**
	 * Flag if the process is active.
	 */
	private final Boolean mIsActive;
	/**
	 * The power.
	 */
	private final Integer mPower;
	/**
	 * The frequency.
	 */
	private final Integer mFrequency;
	/**
	 * The wave.
	 */
	private final Integer mWave;
	/**
	 * The processing mode.
	 */
	private final Integer mMode;
	/**
	 * The minimum power.
	 */
	private final Double mMinPower;
	/**
	 * Flag indicating if the signal is on high power.
	 */
	private final Boolean mIsHighPower;
	/**
	 * The duration in ms after which power goes up (for positive values) or down (for negative values).
	 */
	private final Long mPowerChangeDuration;
	/**
	 * The cycle length.
	 */
	private final Integer mCycleLength;
	/**
	 * The running probability.
	 */
	private final Double mRunningProbability;
	/**
	 * The average off duration.
	 */
	private final Long mAvgOffDuration;
	/**
	 * The average on duration.
	 */
	private final Long mAvgOnDuration;
	/**
	 * The duration of a one time pulse.
	 */
	private final Long mPulseDuration;

	/**
	 * Constructor to reconstruct from String representation.
	 *
	 * @param dataString The data string.
	 */
	public ProcessingBluetoothMessage(final String dataString) {
		String[] splitData = dataString.split(SEP, -1);
		mChannel = stringToInt(splitData[0]);
		mIsTadel = stringToBoolean(splitData[1]);
		mIsActive = stringToBoolean(splitData[2]);
		mPower = stringToInt(splitData[3]); // MAGIC_NUMBER
		mFrequency = stringToInt(splitData[4]); // MAGIC_NUMBER
		mWave = stringToInt(splitData[5]); // MAGIC_NUMBER
		mMode = stringToInt(splitData[6]); // MAGIC_NUMBER
		mMinPower = stringToDouble(splitData[7]); // MAGIC_NUMBER
		mIsHighPower = stringToBoolean(splitData[8]); // MAGIC_NUMBER
		mPowerChangeDuration = stringToLong(splitData[9]); // MAGIC_NUMBER
		mCycleLength = stringToInt(splitData[10]); // MAGIC_NUMBER
		mRunningProbability = stringToDouble(splitData[11]); // MAGIC_NUMBER
		mAvgOffDuration = stringToLong(splitData[12]); // MAGIC_NUMBER
		mAvgOnDuration = stringToLong(splitData[13]); // MAGIC_NUMBER
		mPulseDuration = stringToLong(splitData[14]); // MAGIC_NUMBER
	}

	/**
	 * Constructor.
	 *
	 * @param channel The current channel.
	 * @param isTadel Flag indicating if it is Lob or Tadel
	 * @param isActive Flag indicating if process is active
	 * @param power The power.
	 * @param frequency The frequency.
	 * @param wave The wave.
	 * @param mode The processing mode.
	 * @param minPower The minimum power.
	 * @param isHighPower Flag indicating if the signal is on high power.
	 * @param powerChangeDuration The duration in ms after which power goes up (for positive values) or down (for negative values).
	 * @param cycleLength The cycle length.
	 * @param runningProbability The running probability.
	 * @param avgOffDuration The average off duration.
	 * @param avgOnDuration The average on duration.
	 * @param pulseDuration The duration of a one time pulse.
	 */
	public ProcessingBluetoothMessage(final int channel, final boolean isTadel, final Boolean isActive, final Integer power, // SUPPRESS_CHECKSTYLE
			final Integer frequency, final Integer wave, final Mode mode, final Double minPower, final Boolean isHighPower,
			final Long powerChangeDuration, final Integer cycleLength, final Double runningProbability,
			final Long avgOffDuration, final Long avgOnDuration, final Long pulseDuration) {
		mChannel = channel;
		mIsTadel = isTadel;
		mIsActive = isActive;
		mPower = power;
		mFrequency = frequency;
		mWave = wave;
		mMode = mode == null ? null : isTadel ? mode.getTadelValue() : mode.getLobValue();
		mMinPower = minPower;
		mIsHighPower = isHighPower;
		mPowerChangeDuration = powerChangeDuration;
		mCycleLength = cycleLength;
		mRunningProbability = runningProbability;
		mAvgOffDuration = avgOffDuration;
		mAvgOnDuration = avgOnDuration;
		mPulseDuration = pulseDuration;
	}

	@Override
	public final MessageType getType() {
		return MessageType.PROCESSING_BLUETOOTH;
	}

	@Override
	protected final String getDataString() {
		return mChannel + SEP + mIsTadel + SEP + booleanToString(mIsActive) + SEP
				+ intToString(mPower) + SEP + intToString(mFrequency) + SEP + intToString(mWave) + SEP
				+ intToString(mMode) + SEP + doubleToString(mMinPower) + SEP + booleanToString(mIsHighPower) + SEP
				+ longToString(mPowerChangeDuration) + SEP + intToString(mCycleLength) + SEP + doubleToString(mRunningProbability) + SEP
				+ longToString(mAvgOffDuration) + SEP + longToString(mAvgOnDuration) + SEP + longToString(mPulseDuration);
	}

	/**
	 * Get the channel.
	 *
	 * @return The channel
	 */
	public int getChannel() {
		return mChannel;
	}

	/**
	 * Get information if it is Lob or Tadel.
	 *
	 * @return True if Tadel
	 */
	public boolean isTadel() {
		return mIsTadel;
	}

	/**
	 * Get information if power is active.
	 *
	 * @return True if active
	 */
	public Boolean isActive() {
		return mIsActive;
	}

	/**
	 * Get the power.
	 *
	 * @return The power
	 */
	public Integer getPower() {
		return mPower;
	}

	/**
	 * Get the frequency.
	 *
	 * @return The frequency
	 */
	public Integer getFrequency() {
		return mFrequency;
	}

	/**
	 * Get the wave.
	 *
	 * @return The wave
	 */
	public Integer getWave() {
		return mWave;
	}

	/**
	 * Get the mode.
	 *
	 * @return The mode
	 */
	public Mode getMode() {
		if (mMode == null) {
			return null;
		}
		else if (mIsTadel) {
			return Mode.fromTadelValue(mMode);
		}
		else {
			return Mode.fromLobValue(mMode);
		}
	}

	/**
	 * Get the min power.
	 *
	 * @return The min power
	 */
	public Double getMinPower() {
		return mMinPower;
	}

	/**
	 * Get information if the signal is on high power.
	 *
	 * @return True if the signal is on high power.
	 */
	public Boolean isHighPower() {
		return mIsHighPower;
	}

	/**
	 * Get the power change duration.
	 *
	 * @return The power change duration
	 */
	public Long getPowerChangeDuration() {
		return mPowerChangeDuration;
	}

	/**
	 * Get the cycle length.
	 *
	 * @return The cycle length
	 */
	public Integer getCycleLength() {
		return mCycleLength;
	}

	/**
	 * Get the running probability.
	 *
	 * @return The running probability
	 */
	public Double getRunningProbability() {
		return mRunningProbability;
	}

	/**
	 * Get the average off duration.
	 *
	 * @return The average off duration
	 */
	public Long getAvgOffDuration() {
		return mAvgOffDuration;
	}

	/**
	 * Get the average on duration.
	 *
	 * @return The average on duration
	 */
	public Long getAvgOnDuration() {
		return mAvgOnDuration;
	}

	/**
	 * Get the duration of a one time pulse.
	 *
	 * @return The duration of a one time pulse.
	 */
	public Long getPulseDuration() {
		return mPulseDuration;
	}

}
