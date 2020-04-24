package de.jeisfeld.lut.bluetooth.message;

/**
 * A standalone status message.
 */
public class StandaloneStatusMessage extends Message {
	/**
	 * The active flag.
	 */
	private final boolean mIsActive;

	/**
	 * Constructor.
	 *
	 * @param dataString The data string.
	 */
	public StandaloneStatusMessage(final String dataString) {
		mIsActive = Boolean.parseBoolean(dataString);
	}

	/**
	 * Constructor.
	 *
	 * @param isActive the active flag.
	 */
	public StandaloneStatusMessage(final boolean isActive) {
		mIsActive = isActive;
	}

	@Override
	public final MessageType getType() {
		return MessageType.STANDALONE_STATUS;
	}

	@Override
	protected final String getDataString() {
		return Boolean.toString(mIsActive);
	}

	/**
	 * Get the active flag.
	 *
	 * @return The active flag.
	 */
	public boolean isActive() {
		return mIsActive;
	}

}
