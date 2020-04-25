package de.jeisfeld.lut.bluetooth.message;

/**
 * A processing trigger message.
 */
public class ProcessingTriggerMessage extends Message {
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
	private final boolean mIsActive;
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
	private final Integer mMinPower;
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
	private final Double mAvgOffDuration;
	/**
	 * The average on duration.
	 */
	private final Double mAvgOnDuration;

	/**
	 * Constructor to reconstruct from String representation.
	 *
	 * @param dataString The data string.
	 */
	public ProcessingTriggerMessage(final String dataString) {
		String[] splitData = dataString.split(SEP, -1);
		mChannel = stringToInt(splitData[0]);
		mIsTadel = Boolean.parseBoolean(splitData[1]);
		mIsActive = Boolean.parseBoolean(splitData[2]);
		mPower = stringToInt(splitData[3]); // MAGIC_NUMBER
		mFrequency = stringToInt(splitData[4]); // MAGIC_NUMBER
		mWave = stringToInt(splitData[5]); // MAGIC_NUMBER
		mMode = stringToInt(splitData[6]); // MAGIC_NUMBER
		mMinPower = stringToInt(splitData[7]); // MAGIC_NUMBER
		mCycleLength = stringToInt(splitData[8]); // MAGIC_NUMBER
		mRunningProbability = stringToDouble(splitData[9]); // MAGIC_NUMBER
		mAvgOffDuration = stringToDouble(splitData[10]); // MAGIC_NUMBER
		mAvgOnDuration = stringToDouble(splitData[11]); // MAGIC_NUMBER
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
	 * @param cycleLength The cycle length.
	 * @param runningProbability The running probability.
	 * @param avgOffDuration The average off duration.
	 * @param avgOnDuration The average on duration.
	 */
	public ProcessingTriggerMessage(final int channel, final boolean isTadel, final boolean isActive, final Integer power, // SUPPRESS_CHECKSTYLE
			final Integer frequency, final Integer wave, final Integer mode, final Integer minPower, final int cycleLength,
			final Double runningProbability, final Double avgOffDuration, final Double avgOnDuration) {
		mChannel = channel;
		mIsTadel = isTadel;
		mIsActive = isActive;
		mPower = power;
		mFrequency = frequency;
		mWave = wave;
		mMode = mode;
		mMinPower = minPower;
		mCycleLength = cycleLength;
		mRunningProbability = runningProbability;
		mAvgOffDuration = avgOffDuration;
		mAvgOnDuration = avgOnDuration;
	}

	@Override
	public final MessageType getType() {
		return MessageType.PROCESSING_STATUS;
	}

	@Override
	protected final String getDataString() {
		return mChannel + SEP + mIsTadel + SEP + mIsActive + SEP
				+ intToString(mPower) + SEP + intToString(mFrequency) + SEP + intToString(mWave) + SEP
				+ intToString(mMode) + SEP + intToString(mMinPower) + SEP + intToString(mCycleLength) + SEP
				+ doubleToString(mRunningProbability) + SEP + doubleToString(mAvgOffDuration) + SEP + doubleToString(mAvgOnDuration);
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
	public boolean isActive() {
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
	public Integer getMode() {
		return mMode;
	}

	/**
	 * Get the min power.
	 *
	 * @return The min power
	 */
	public Integer getMinPower() {
		return mMinPower;
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
	public Double getAvgOffDuration() {
		return mAvgOffDuration;
	}

	/**
	 * Get the average on duration.
	 *
	 * @return The average on duration
	 */
	public Double getAvgOnDuration() {
		return mAvgOnDuration;
	}

	/**
	 * Convert integer to String.
	 *
	 * @param i the integer
	 * @return the String
	 */
	private static String intToString(final Integer i) {
		return i == null ? "" : i.toString();
	}

	/**
	 * Convert String back to integer.
	 *
	 * @param s The string.
	 * @return the integer.
	 */
	private static Integer stringToInt(final String s) {
		return (s == null || s.isEmpty()) ? null : Integer.parseInt(s);
	}

	/**
	 * Convert double to String.
	 *
	 * @param d the double
	 * @return the String
	 */
	private static String doubleToString(final Double d) {
		return d == null ? "" : d.toString();
	}

	/**
	 * Convert String back to double.
	 *
	 * @param s The string.
	 * @return the double.
	 */
	private static Double stringToDouble(final String s) {
		return (s == null || s.isEmpty()) ? null : Double.parseDouble(s);
	}

}
