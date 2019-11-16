package de.jeisfeld.pi.gpio;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

/**
 * A PWM output.
 */
public class PwmOutput {
	/**
	 * The number of steps for hardware PWM.
	 */
	protected static final int STEPS_HARD = 1024;
	/**
	 * The number of steps for software PWM.
	 */
	protected static final int STEPS_SOFT = 100;
	/**
	 * The minimum planned duration of a step.
	 */
	protected static final double MIN_STEP_DURATION = 30;

	/**
	 * Flag indicating if it is software or hardware based PWM output.
	 */
	private boolean mIsSoft;
	/**
	 * Flag indicating the default value of this output.
	 */
	private boolean mIsHigh;
	/**
	 * The corresponding PIN Output.
	 */
	private GpioPinPwmOutput mPinOutput;
	/**
	 * The current value of the output.
	 */
	private double mValue = 0;

	/**
	 * Create a PWM output for a PIN.
	 *
	 * @param pin The output PIN
	 * @param isHigh Flag indicating the default value of this output.
	 */
	public PwmOutput(final Pin pin, final boolean isHigh) {
		this.mIsHigh = isHigh;
		if (RaspiPin.GPIO_01.equals(pin)) {
			mPinOutput = GpioFactory.getInstance().provisionPwmOutputPin(pin, isHigh ? STEPS_HARD : 0);
			mIsSoft = false;
		}
		else {
			mPinOutput = GpioFactory.getInstance().provisionSoftPwmOutputPin(pin, isHigh ? STEPS_SOFT : 0);
			mIsSoft = true;
		}
		ShutdownUtil.prepareShutdown(mPinOutput);
	}

	/**
	 * Create multiple PWM outputs.
	 *
	 * @param isHigh Flag indicating the default value of this output.
	 * @param pins The output PINs
	 * @return The corresponding digital outputs.
	 */
	public static PwmOutput[] createPwmOutputs(final boolean isHigh, final Pin... pins) {
		PwmOutput[] result = new PwmOutput[pins.length];
		for (int i = 0; i < pins.length; i++) {
			result[i] = new PwmOutput(pins[i], isHigh);
		}
		return result;
	}

	/**
	 * Set the value of the PWM output.
	 *
	 * @param value The value to be set. 0 corresponds to the default value.
	 */
	public void setValue(final double value) {
		mValue = value;
		if (!ShutdownUtil.isShutdown()) {
			mPinOutput.setPwm((int) ((mIsHigh ? 1 - mValue : mValue) * (mIsSoft ? STEPS_SOFT : STEPS_HARD)));
		}
	}

	/**
	 * Get the value of the output.
	 *
	 * @return the value of the output.
	 */
	protected double getValue() {
		return mValue;
	}

	/**
	 * Move to a new value over certain time.
	 *
	 * @param targetValue The target value.
	 * @param duration The duration.
	 */
	public void moveToValue(final double targetValue, final long duration) {
		long startTime = System.currentTimeMillis();
		double startValue = mValue;
		int steps = (int) Math.min(Math.abs(targetValue - startValue) * (mIsSoft ? STEPS_SOFT : STEPS_HARD), duration / MIN_STEP_DURATION);

		for (int i = 1; i <= steps; i++) {
			sleepIfRequired(startTime + duration * i / steps - System.currentTimeMillis());
			setValue(startValue + (targetValue - startValue) * i / steps);
		}
		sleepIfRequired(startTime + duration - System.currentTimeMillis());
	}

	/**
	 * Sleep for duration if bigger than 0.
	 *
	 * @param duration The duration.
	 */
	protected static void sleepIfRequired(final long duration) {
		if (duration > 0) {
			try {
				Thread.sleep(duration);
			}
			catch (InterruptedException e) {
				// Ignore
			}
		}
	}
}
