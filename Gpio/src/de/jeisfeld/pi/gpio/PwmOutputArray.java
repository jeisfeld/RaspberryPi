package de.jeisfeld.pi.gpio;

import java.awt.Color;
import java.util.Arrays;

import com.pi4j.io.gpio.Pin;

/**
 * A PWM output array.
 */
public class PwmOutputArray {
	/**
	 * Multiplier to convert from double to byte.
	 */
	private static final double BYTE_DIVISOR = 255.0;
	/**
	 * The corresponding PIN Outputs.
	 */
	private final PwmOutput[] mOutputs;

	/**
	 * Create a PWM output array for an array of PINs.
	 *
	 * @param isHigh Flag indicating the default value of the outputs.
	 * @param pins The output PINs
	 */
	public PwmOutputArray(final boolean isHigh, final Pin... pins) {
		mOutputs = new PwmOutput[pins.length];

		for (int i = 0; i < pins.length; i++) {
			mOutputs[i] = new PwmOutput(pins[i], isHigh);
		}
	}

	/**
	 * Set the values of the PWM outputs.
	 *
	 * @param values The values to be set. Values between 0 and 1.
	 */
	public void setValue(final double... values) {
		for (int i = 0; i < mOutputs.length; i++) {
			mOutputs[i].setValue(values[i]);
		}
	}

	/**
	 * Set the values of the PWM outputs.
	 *
	 * @param values The values to be set. Values between 0 and 255.
	 */
	public void setByteValue(final int... values) {
		setValue(Arrays.stream(values).mapToDouble(x -> x / PwmOutputArray.BYTE_DIVISOR).toArray());
	}

	/**
	 * Set the values of the PWM outputs.
	 *
	 * @param color the RGB color to be set.
	 */
	public void setColor(final Color color) {
		setByteValue(color.getRed(), color.getGreen(), color.getBlue());
	}

	/**
	 * Move to new values over certain time.
	 *
	 * @param duration The duration.
	 * @param targetValues The target values in range 0...1
	 */
	public void moveToValue(final long duration, final double... targetValues) {
		long startTime = System.currentTimeMillis();
		double[] startValues = new double[mOutputs.length];
		for (int i = 0; i < mOutputs.length; i++) {
			startValues[i] = mOutputs[i].getValue();
		}
		double maxDiff = 0;
		for (int i = 0; i < mOutputs.length; i++) {
			maxDiff = Math.max(maxDiff, Math.abs(targetValues[i] - startValues[i]));
		}

		int steps = (int) Math.min(maxDiff * PwmOutput.STEPS_HARD, duration / PwmOutput.MIN_STEP_DURATION);

		for (int step = 1; step <= steps; step++) {
			PwmOutput.sleepIfRequired(startTime + duration * step / steps - System.currentTimeMillis());
			double[] currentValues = new double[mOutputs.length];
			for (int i = 0; i < mOutputs.length; i++) {
				currentValues[i] = startValues[i] + (targetValues[i] - startValues[i]) * step / steps;
			}
			setValue(currentValues);
		}
		if (steps == 0) {
			setValue(targetValues);
		}
		PwmOutput.sleepIfRequired(startTime + duration - System.currentTimeMillis());
	}

	/**
	 * Move to new values over certain time.
	 *
	 * @param duration The duration.
	 * @param targetValues The target values in range 0...255
	 */
	public void moveToByteValue(final long duration, final int... targetValues) {
		moveToValue(duration, Arrays.stream(targetValues).mapToDouble(x -> x / PwmOutputArray.BYTE_DIVISOR).toArray());
	}

	/**
	 * Move to new values over certain time.
	 *
	 * @param duration The duration.
	 * @param color The target RGB color.
	 */
	public void moveToColor(final long duration, final Color color) {
		moveToByteValue(duration, color.getRed(), color.getGreen(), color.getBlue());
	}

}
