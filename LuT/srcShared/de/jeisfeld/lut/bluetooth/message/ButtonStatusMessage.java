package de.jeisfeld.lut.bluetooth.message;

/**
 * A button status message.
 */
public class ButtonStatusMessage extends Message {
	/**
	 * Flag indicating if button 1 is pressed.
	 */
	private final boolean mIsButton1Pressed;
	/**
	 * Flag indicating if button 2 is pressed.
	 */
	private final boolean mIsButton2Pressed;
	/**
	 * Value of control 1.
	 */
	private final int mControl1Value;
	/**
	 * Value of control 2.
	 */
	private final int mControl2Value;
	/**
	 * Value of control 3.
	 */
	private final int mControl3Value;

	/**
	 * Constructor.
	 *
	 * @param dataString The data string.
	 */
	public ButtonStatusMessage(final String dataString) {
		String[] splitData = dataString.split(SEP);

		mIsButton1Pressed = Boolean.parseBoolean(splitData[0]);
		mIsButton2Pressed = Boolean.parseBoolean(splitData[1]);
		mControl1Value = Integer.parseInt(splitData[2]);
		mControl2Value = Integer.parseInt(splitData[3]); // MAGIC_NUMBER
		mControl3Value = Integer.parseInt(splitData[4]); // MAGIC_NUMBER
	}

	@Override
	public final MessageType getType() {
		return MessageType.BUTTON_STATUS;
	}

	@Override
	protected final String getDataString() {
		return mIsButton1Pressed + SEP + mIsButton2Pressed + SEP + mControl1Value + SEP + mControl2Value + SEP + mControl3Value;
	}

	/**
	 * Get information if button 1 is pressed.
	 *
	 * @return information if button 1 is pressed.
	 */
	public boolean isButton1Pressed() {
		return mIsButton1Pressed;
	}

	/**
	 * Get information if button 2 is pressed.
	 *
	 * @return information if button 2 is pressed.
	 */
	public boolean isButton2Pressed() {
		return mIsButton2Pressed;
	}

	/**
	 * Get the value of control 1.
	 *
	 * @return The value of control 1 in range 0..255
	 */
	public int getControl1Value() {
		return mControl1Value;
	}

	/**
	 * Get the value of control 2.
	 *
	 * @return The value of control 2 in range 0..255
	 */
	public int getControl2Value() {
		return mControl2Value;
	}

	/**
	 * Get the value of control 2.
	 *
	 * @return The value of control 2 in range 0..255
	 */
	public int getControl3Value() {
		return mControl3Value;
	}

}
