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
	 * Constructor to reconstruct from String representation.
	 *
	 * @param dataString The data string.
	 */
	public ProcessingModeMessage(final String dataString) {
		String[] splitData = dataString.split(",");
		mChannel = Integer.parseInt(splitData[0]);
		mIsTadel = Boolean.parseBoolean(splitData[1]);
		mMode = Integer.parseInt(splitData[2]);
	}

	/**
	 * Constructor.
	 *
	 * @param channel The current channel.
	 * @param isTadel Flag indicating if it is Lob or Tadel
	 * @param mode The processing mode.
	 */
	public ProcessingModeMessage(final int channel, final boolean isTadel, final int mode) {
		mChannel = channel;
		mIsTadel = isTadel;
		mMode = mode;
	}

	@Override
	public final MessageType getType() {
		return MessageType.PROCESSING_MODE;
	}

	@Override
	public final String getDataString() {
		return mChannel + "," + mIsTadel + "," + mMode;
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

}
