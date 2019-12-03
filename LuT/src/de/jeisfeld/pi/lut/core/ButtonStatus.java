package de.jeisfeld.pi.lut.core;

/**
 * Status of eWeb buttons.
 */
public final class ButtonStatus {
	/**
	 * The max value of controls.
	 */
	public static final int MAX_CONTROL_VALUE = 255;
	/**
	 * Flag indicating if button 1 is pressed.
	 */
	private boolean mIsButton1Pressed;
	/**
	 * Flag indicating if button 2 is pressed.
	 */
	private boolean mIsButton2Pressed;
	/**
	 * Flag indicating if buttons are updated.
	 */
	private boolean mIsDigitalUpdated;
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
	 * Flag indicating if controls are updated.
	 */
	private boolean mIsAnalogUpdated;
	/**
	 * Listener for button 1.
	 */
	private ButtonListener mButton1Listener = null;
	/**
	 * Listener for button 2.
	 */
	private ButtonListener mButton2Listener = null;

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
	 * Set listener for button 1.
	 *
	 * @param listener The listener.
	 */
	protected void setButton1Listener(final ButtonListener listener) {
		mButton1Listener = listener;
	}

	/**
	 * Set listener for button 2.
	 *
	 * @param listener The listener.
	 */
	protected void setButton2Listener(final ButtonListener listener) {
		mButton2Listener = listener;
	}

	/**
	 * Set the value of button1 pressing and trigger listener if applicable.
	 *
	 * @param isButton1Pressed new value.
	 */
	private void setButton1Pressed(final boolean isButton1Pressed) {
		if (mButton1Listener != null && isButton1Pressed != mIsButton1Pressed) {
			if (isButton1Pressed) {
				mButton1Listener.handleButtonDown();
			}
			else {
				mButton1Listener.handleButtonUp();
			}
		}

		mIsButton1Pressed = isButton1Pressed;
	}

	/**
	 * Set the value of button2 pressing and trigger listener if applicable.
	 *
	 * @param isButton2Pressed new value.
	 */
	private void setButton2Pressed(final boolean isButton2Pressed) {
		if (mButton2Listener != null && isButton2Pressed != mIsButton2Pressed) {
			if (isButton2Pressed) {
				mButton2Listener.handleButtonDown();
			}
			else {
				mButton2Listener.handleButtonUp();
			}
		}

		mIsButton2Pressed = isButton2Pressed;
	}

	/**
	 * Update values from other ButtonStatus.
	 *
	 * @param other The other ButtonStatus.
	 */
	public void updateWith(final ButtonStatus other) {
		if (other.mIsDigitalUpdated) {
			setButton1Pressed(other.mIsButton1Pressed);
			setButton2Pressed(other.mIsButton2Pressed);
			mIsDigitalUpdated = true;
		}
		if (other.mIsAnalogUpdated) {
			mControl1Value = other.mControl1Value;
			mControl2Value = other.mControl2Value;
			mControl3Value = other.mControl3Value;
			mIsAnalogUpdated = true;
		}
	}

	/**
	 * Set the digital results from the serial String.
	 *
	 * @param resultString The serial response String read from eWeb.
	 * @return true if successful
	 */
	public boolean setDigitalResult(final String resultString) {
		if (resultString != null && resultString.contains("S")) {
			int index = resultString.indexOf("S");
			int newLineIndex = resultString.indexOf('\r', index);
			String reducedResultString = resultString.substring(index + 1, newLineIndex);
			if (reducedResultString.length() >= 2) { // MAGIC_NUMBER
				setButton1Pressed(reducedResultString.charAt(0) == '1');
				setButton2Pressed(reducedResultString.charAt(1) == '1');
				mIsDigitalUpdated = true;
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
	public boolean setAnalogResult(final String resultString) {
		if (resultString != null && resultString.contains("A")) {
			int index = resultString.indexOf("A");
			int newLineIndex = resultString.indexOf('\r', index);
			String reducedResultString = resultString.substring(index + 1, newLineIndex);
			String[] controlValues = reducedResultString.split(";");
			if (controlValues.length >= 3) { // MAGIC_NUMBER
				mControl1Value = Integer.parseInt(controlValues[0]);
				mControl2Value = Integer.parseInt(controlValues[1]);
				mControl3Value = Integer.parseInt(controlValues[2]);
				mIsAnalogUpdated = true;
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return mIsButton1Pressed + "," + mIsButton2Pressed + "," + mControl1Value + "," + mControl2Value + "," + mControl3Value;
	}

	/**
	 * A listener for button press or release.
	 */
	public interface ButtonListener {
		/**
		 * Callback in case of a clear "on" trigger.
		 */
		void handleButtonDown();

		/**
		 * Callback in case of a clear "off" trigger.
		 */
		void handleButtonUp();
	}
}
