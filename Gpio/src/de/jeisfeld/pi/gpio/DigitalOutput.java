package de.jeisfeld.pi.gpio;

import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

/**
 * A digital output.
 */
public class DigitalOutput {
	/**
	 * The corresponding PIN Output.
	 */
	private GpioPinDigitalOutput mPinOutput;
	/**
	 * Flag indicating the default value of this output.
	 */
	private boolean mIsHigh;

	/**
	 * Create a digital output for a PIN.
	 *
	 * @param pin The output PIN
	 * @param isHigh Flag indicating the default value of this output.
	 */
	public DigitalOutput(final Pin pin, final boolean isHigh) {
		this.mIsHigh = isHigh;
		mPinOutput = GpioFactory.getInstance().provisionDigitalOutputPin(pin, isHigh ? PinState.HIGH : PinState.LOW);
		ShutdownUtil.prepareShutdown(mPinOutput);
	}

	/**
	 * Create multiple digital outputs.
	 *
	 * @param isHigh Flag indicating the default value of this output.
	 * @param pins The output PINs
	 * @return The corresponding digital outputs.
	 */
	public static DigitalOutput[] createDigitalOutputs(final boolean isHigh, final Pin... pins) {
		DigitalOutput[] result = new DigitalOutput[pins.length];
		for (int i = 0; i < pins.length; i++) {
			result[i] = new DigitalOutput(pins[i], isHigh);
		}
		return result;
	}

	/**
	 * Set the value of the digital output.
	 *
	 * @param value The value to be set. False corresponds to the default value.
	 */
	public void setValue(final boolean value) {
		if (!ShutdownUtil.isShutdown()) {
			if (value ^ mIsHigh) {
				mPinOutput.high();
			}
			else {
				mPinOutput.low();
			}
		}
	}

	/**
	 * Blink on this output.
	 *
	 * @param duration The duration of a blink phase.
	 */
	public void blink(final long duration) {
		if (!ShutdownUtil.isShutdown()) {
			mPinOutput.blink(duration);
		}
	}

}
