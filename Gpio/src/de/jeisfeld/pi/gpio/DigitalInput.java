package de.jeisfeld.pi.gpio;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

/**
 * A digital input, e.g. addressed by a button or switch.
 */
public class DigitalInput {
	/**
	 * The corresponding PIN Input.
	 */
	private final GpioPinDigitalInput mPinInput;
	/**
	 * Flag indicating if this is pullUp input (i.e. by default high).
	 */
	private final boolean mIsPullUp;

	/**
	 * Create a digital input for a PIN.
	 *
	 * @param pin The input PIN
	 * @param isPullUp Flag indicating if this is pullUp input (i.e. by default high).
	 */
	public DigitalInput(final Pin pin, final boolean isPullUp) {
		mIsPullUp = isPullUp;
		mPinInput = GpioFactory.getInstance().provisionDigitalInputPin(pin, isPullUp ? PinPullResistance.PULL_UP : PinPullResistance.PULL_DOWN);
		ShutdownUtil.prepareShutdown(mPinInput);
	}

	/**
	 * Get the value of the input.
	 *
	 * @return true if in non-default state.
	 */
	public boolean getValue() {
		return mPinInput.getState() == (mIsPullUp ? PinState.LOW : PinState.HIGH);
	}

	/**
	 * Add a listener for changes of this input.
	 *
	 * @param listener The listener.
	 */
	public void addListener(final OnStateChangeListener listener) {
		mPinInput.addListener(new GpioPinListenerDigital() {
			@Override
			public void handleGpioPinDigitalStateChangeEvent(final GpioPinDigitalStateChangeEvent event) {
				listener.handleStateChange(event.getState().isHigh() ^ mIsPullUp);
			}
		});
	}

	/**
	 * Add a listener for stable changes of this input.
	 *
	 * @param listener The listener.
	 */
	public void addListener(final OnStableStateChangeListener listener) {
		mPinInput.addListener(new GpioPinListenerDigital() {
			private long mLastButtonTime = System.currentTimeMillis();
			private static final int SWITCH_MAX_DURATION = 20;

			@Override
			public void handleGpioPinDigitalStateChangeEvent(final GpioPinDigitalStateChangeEvent event) {
				if (System.currentTimeMillis() - mLastButtonTime > SWITCH_MAX_DURATION) {
					if (event.getState().isHigh() ^ mIsPullUp) {
						listener.handleTriggerOn();
					}
					else {
						listener.handleTriggerOff();
					}
				}
				mLastButtonTime = System.currentTimeMillis();
			}
		});
	}

	/**
	 * Add a listener for long presses of this input.
	 *
	 * @param listener The listener.
	 */
	public void addListener(final OnLongPressListener listener) {
		mPinInput.addListener(new GpioPinListenerDigital() {
			private long mLastButtonTime = System.currentTimeMillis();
			private Long mTriggerStartTime = null;
			private static final int SWITCH_MAX_DURATION = 20;

			@Override
			public void handleGpioPinDigitalStateChangeEvent(final GpioPinDigitalStateChangeEvent event) {
				if (System.currentTimeMillis() - mLastButtonTime > SWITCH_MAX_DURATION) {
					if (event.getState().isHigh() ^ mIsPullUp) {
						mTriggerStartTime = System.currentTimeMillis();
						new Thread() {
							private final long mLocalTriggerStartTime = mTriggerStartTime;

							@Override
							public void run() {
								try {
									Thread.sleep(listener.mDuration);
								}
								catch (InterruptedException e) {
									// ignore
								}
								if (mTriggerStartTime != null && mTriggerStartTime == mLocalTriggerStartTime && (mPinInput.isHigh() ^ mIsPullUp)) {
									listener.handleLongTrigger();
								}
							}
						}.start();
					}
					else {
						mTriggerStartTime = null;
					}
				}
				mLastButtonTime = System.currentTimeMillis();
			}
		});
	}

	/**
	 * A listener for state change of a digital input.
	 */
	public interface OnStateChangeListener {
		/**
		 * Callback called whenever the state changes (including transient changes).
		 *
		 * @param state true in case of an "on" trigger.
		 */
		void handleStateChange(boolean state);
	}

	/**
	 * A listener for stable state change of a digital input.
	 */
	public interface OnStableStateChangeListener {
		/**
		 * Callback in case of a clear "on" trigger.
		 */
		void handleTriggerOn();

		/**
		 * Callback in case of a clear "off" trigger.
		 */
		void handleTriggerOff();
	}

	/**
	 * A listener for long press to a digital input.
	 */
	public abstract static class OnLongPressListener {
		/**
		 * The default duration of a long press.
		 */
		private static final int DEFAULT_TRIGGER_DURATION = 5000;

		/**
		 * The duration of the long press.
		 */
		private final long mDuration;

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
	}

}
