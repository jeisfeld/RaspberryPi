package de.jeisfeld.pi.lut;

/**
 * Status of eWeb buttons.
 */
public final class ButtonStatus {
	/**
	 * Flag indicating if button 1 is pressed.
	 */
	private boolean mIsButton1Pressed;
	/**
	 * Flag indicating if button 2 is pressed.
	 */
	private boolean mIsButton2Pressed;
	/**
	 * Value of control 1.
	 */
	private int mControl1Value;
	/**
	 * Value of control 2.
	 */
	private int mControl2Value;
	/**
	 * Value of control 3.
	 */
	private int mControl3Value;

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

	/**
	 * Set the digital results from the serial String.
	 *
	 * @param resultString The serial response String read from eWeb.
	 * @return true if successful
	 */
	protected boolean setDigitalResult(final String resultString) {
		if (resultString.contains("S")) {
			int index = resultString.indexOf("S");
			int newLineIndex = resultString.indexOf('\r', index);
			String reducedResultString = resultString.substring(index + 1, newLineIndex);
			if (reducedResultString.length() >= 2) { // MAGIC_NUMBER
				mIsButton1Pressed = reducedResultString.charAt(0) == '1';
				mIsButton2Pressed = reducedResultString.charAt(1) == '1';
			}
			return true;
		}
		return false;
	}

	/**
	 * Set the analog results from the serial String.
	 *
	 * @param resultString The serial response String read from eWeb.
	 * @return true if successful
	 */
	protected boolean setAnalogResult(final String resultString) {
		if (resultString.contains("A")) {
			int index = resultString.indexOf("A");
			int newLineIndex = resultString.indexOf('\r', index);
			String reducedResultString = resultString.substring(index + 1, newLineIndex);
			String[] controlValues = reducedResultString.split(";");
			if (controlValues.length >= 3) { // MAGIC_NUMBER
				mControl1Value = Integer.parseInt(controlValues[0]);
				mControl2Value = Integer.parseInt(controlValues[1]);
				mControl3Value = Integer.parseInt(controlValues[2]);
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return mIsButton1Pressed + "," + mIsButton2Pressed + "," + mControl1Value + "," + mControl2Value + "," + mControl3Value;
	}

}
