package de.jeisfeld.lut.bluetooth.message;

/**
 * A processing status message.
 */
public class ProcessingStandaloneMessage extends Message {
	/**
	 * The current channel.
	 */
	private final int mChannel;
	/**
	 * Flag indicating if it is Lob or Tadel.
	 */
	private final boolean mIsTadel;
	/**
	 * Flag if the power is active.
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
	 * The mode name.
	 */
	private final String mModeName;
	/**
	 * Details on processing mode.
	 */
	private final String mDetails;

	/**
	 * Constructor to reconstruct from String representation.
	 *
	 * @param dataString The data string.
	 */
	public ProcessingStandaloneMessage(final String dataString) {
		String[] splitData = dataString.split(SEP, -1);
		mChannel = stringToInt(splitData[0]);
		mIsTadel = Boolean.parseBoolean(splitData[1]);
		mIsActive = Boolean.parseBoolean(splitData[2]);
		mPower = stringToInt(splitData[3]); // MAGIC_NUMBER
		mFrequency = stringToInt(splitData[4]); // MAGIC_NUMBER
		mWave = stringToInt(splitData[5]); // MAGIC_NUMBER
		mMode = stringToInt(splitData[6]); // MAGIC_NUMBER
		mModeName = Message.decode(splitData[7]); // MAGIC_NUMBER
		mDetails = Message.decode(splitData[8]); // MAGIC_NUMBER
	}

	/**
	 * Constructor.
	 *
	 * @param channel The current channel.
	 * @param isTadel Flag indicating if it is Lob or Tadel
	 * @param isActive Flag indicating if power is active
	 * @param power The power.
	 * @param frequency The frequency.
	 * @param wave The wave.
	 * @param mode The processing mode.
	 * @param modeName The mode name.
	 * @param details The details.
	 */
	public ProcessingStandaloneMessage(final int channel, final boolean isTadel, final boolean isActive, final Integer power, // SUPPRESS_CHECKSTYLE
			final Integer frequency, final Integer wave, final Integer mode, final String modeName, final String details) {
		mChannel = channel;
		mIsTadel = isTadel;
		mIsActive = isActive;
		mPower = power;
		mFrequency = frequency;
		mWave = wave;
		mMode = mode;
		mModeName = modeName;
		mDetails = details;
	}

	@Override
	public final MessageType getType() {
		return MessageType.PROCESSING_STANDALONE;
	}

	@Override
	protected final String getDataString() {
		return mChannel + SEP + mIsTadel + SEP + mIsActive + SEP
				+ intToString(mPower) + SEP + intToString(mFrequency) + SEP + intToString(mWave) + SEP
				+ intToString(mMode) + SEP + Message.encode(mModeName) + SEP + Message.encode(mDetails);
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
	 * Get the mode name.
	 *
	 * @return The mode name
	 */
	public String getModeName() {
		return mModeName;
	}

	/**
	 * Get the details.
	 *
	 * @return The details
	 */
	public String getDetails() {
		return mDetails;
	}

}
