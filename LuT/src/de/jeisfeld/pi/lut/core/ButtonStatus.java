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
	 * Long press Listener for button 1.
	 */
	private OnLongPressListener mButton1LongPressListener = null;
	/**
	 * Long press Listener for button 2.
	 */
	private OnLongPressListener mButton2LongPressListener = null;

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
	 * Set long press listener for button 1.
	 *
	 * @param listener The listener.
	 */
	protected void setButton1LongPressListener(final OnLongPressListener listener) {
		mButton1LongPressListener = listener;
		mButton1LongPressListener.mButtonNo = 1;
	}

	/**
	 * Set long press listener for button 2.
	 *
	 * @param listener The listener.
	 */
	protected void setButton2LongPressListener(final OnLongPressListener listener) {
		mButton2LongPressListener = listener;
		mButton2LongPressListener.mButtonNo = 2;
	}

	/**
	 * Set the value of button1 pressing and trigger listener if applicable.
	 *
	 * @param isButton1Pressed new value.
	 */
	private void setButton1Pressed(final boolean isButton1Pressed) {
		if (isButton1Pressed != mIsButton1Pressed) {
			if (mButton1Listener != null) {
				new Thread() {
					@Override
					public void run() {
						if (isButton1Pressed) {
							mButton1Listener.handleButtonDown();
						}
						else {
							mButton1Listener.handleButtonUp();
						}
					}
				}.start();
			}
			if (mButton1LongPressListener != null) {
				if (isButton1Pressed) {
					mButton1LongPressListener.getNewWaitingThread(ButtonStatus.this).start();
				}
				else {
					mButton1LongPressListener.reset();
				}
			}
			mIsButton1Pressed = isButton1Pressed;
		}
	}

	/**
	 * Set the value of button2 pressing and trigger listener if applicable.
	 *
	 * @param isButton2Pressed new value.
	 */
	private void setButton2Pressed(final boolean isButton2Pressed) {
		if (isButton2Pressed != mIsButton2Pressed) {
			if (mButton2Listener != null) {
				new Thread() {
					@Override
					public void run() {
						if (isButton2Pressed) {
							mButton2Listener.handleButtonDown();
						}
						else {
							mButton2Listener.handleButtonUp();
						}
					}
				}.start();
			}
			if (mButton2LongPressListener != null) {
				if (isButton2Pressed) {
					mButton2LongPressListener.getNewWaitingThread(ButtonStatus.this).start();
				}
				else {
					mButton2LongPressListener.reset();
				}
			}
			mIsButton2Pressed = isButton2Pressed;
		}
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
		default void handleButtonUp() {
			// do nothing
		}
	}

	/**
	 * A listener for long press to a button.
	 */
	public abstract static class OnLongPressListener {
		/**
		 * The default duration of a long press.
		 */
		private static final int DEFAULT_TRIGGER_DURATION = 2500;

		/**
		 * The duration of the long press.
		 */
		private final long mDuration;
		/**
		 * The last start time of a trigger.
		 */
		private Long mTriggerStartTime;
		/**
		 * The referring button number.
		 */
		private int mButtonNo;

		/**
		 * Constructor.
		 *
		 * @param duration The duration of the long press.
		 */
		public OnLongPressListener(final long duration) {
			mDuration = duration;
		}

		/**
		 * Constructor.
		 */
		public OnLongPressListener() {
			this(OnLongPressListener.DEFAULT_TRIGGER_DURATION);
		}

		/**
		 * Callback called in case of a long "on" trigger.
		 */
		public abstract void handleLongTrigger();

		/**
		 * Get a thread waiting for long press.
		 *
		 * @param buttonStatus The referring ButtonStatus.
		 * @return a WaitingThread.
		 */
		private WaitingThread getNewWaitingThread(final ButtonStatus buttonStatus) {
			return new WaitingThread(buttonStatus);
		}

		/**
		 * Reset, so that the ongoing threads will not trigger any more.
		 */
		private void reset() {
			mTriggerStartTime = null;
		}

		/**
		 * The thread used for waiting for long press.
		 */
		private final class WaitingThread extends Thread {
			/**
			 * The start time of the thread.
			 */
			private final long mLocalTriggerStartTime;
			/**
			 * The referring buttonStatus.
			 */
			private final ButtonStatus mButtonStatus;

			/**
			 * A thread waiting for long press.
			 *
			 * @param buttonStatus The referring ButtonStatus.
			 */
			private WaitingThread(final ButtonStatus buttonStatus) {
				super();
				mTriggerStartTime = System.currentTimeMillis();
				mLocalTriggerStartTime = mTriggerStartTime;
				mButtonStatus = buttonStatus;
			}

			private boolean isButtonPressed() {
				return mButtonNo == 2 ? mButtonStatus.isButton2Pressed() : mButtonStatus.isButton1Pressed();
			}

			@Override
			public void run() {
				try {
					Thread.sleep(mDuration);
				}
				catch (InterruptedException e) {
					// ignore
				}
				if (mTriggerStartTime != null && mTriggerStartTime == mLocalTriggerStartTime && isButtonPressed()) {
					handleLongTrigger();
				}
			}
		}

	}
}
