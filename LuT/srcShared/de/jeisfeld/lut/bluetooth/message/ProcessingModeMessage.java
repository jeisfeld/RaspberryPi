package de.jeisfeld.lut.bluetooth.message;

/**
 * A processing mode message.
 */
public class ProcessingModeMessage extends Message {
	/**
	 * The current channel.
	 */
	private final int mChannel;
	/**
	 * Flag indicating if it is Lob or Tadel.
	 */
	private final boolean mIsTadel;
	/**
	 * The processing mode.
	 */
	private final int mMode;
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
	public ProcessingModeMessage(final String dataString) {
		String[] splitData = dataString.split(",", -1);
		mChannel = Integer.parseInt(splitData[0]);
		mIsTadel = Boolean.parseBoolean(splitData[1]);
		mMode = Integer.parseInt(splitData[2]);
		mModeName = Message.decode(splitData[3]); // MAGIC_NUMBER
		mDetails = Message.decode(splitData[4]); // MAGIC_NUMBER
	}

	/**
	 * Constructor.
	 *
	 * @param channel The current channel.
	 * @param isTadel Flag indicating if it is Lob or Tadel
	 * @param mode The processing mode.
	 * @param modeName The mode name.
	 * @param details The details.
	 */
	public ProcessingModeMessage(final int channel, final boolean isTadel, final int mode, final String modeName, final String details) {
		mChannel = channel;
		mIsTadel = isTadel;
		mMode = mode;
		mModeName = modeName;
		mDetails = details;
	}

	/**
	 * Constructor.
	 *
	 * @param channel The current channel.
	 * @param isTadel Flag indicating if it is Lob or Tadel
	 * @param mode The processing mode.
	 */
	public ProcessingModeMessage(final int channel, final boolean isTadel, final int mode) {
		this(channel, isTadel, mode, "", "");
	}

	@Override
	public final MessageType getType() {
		return MessageType.PROCESSING_MODE;
	}

	@Override
	protected final String getDataString() {
		return mChannel + "," + mIsTadel + "," + mMode + "," + Message.encode(mModeName) + "," + Message.encode(mDetails);
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
	 * Get the mode.
	 *
	 * @return The mode
	 */
	public int getMode() {
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
