package de.jeisfeld.pi.gpio;

import com.pi4j.io.gpio.Pin;

/**
 * A PWM output array.
 */
public class PwmOutputArray {
	/**
	 * The corresponding PIN Outputs.
	 */
	private PwmOutput[] mOutputs;

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
	 * @param values The values to be set. 0 corresponds to the default values.
	 */
	public void setValue(final double... values) {
		for (int i = 0; i < mOutputs.length; i++) {
			mOutputs[i].setValue(values[i]);
		}
	}

	/**
	 * Move to a new value over certain time.
	 *
	 * @param duration The duration.
	 * @param targetValues The target value.
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
		PwmOutput.sleepIfRequired(startTime + duration - System.currentTimeMillis());
	}

}
