package de.jeisfeld.pi.gpio;

import com.pi4j.io.gpio.Pin;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.SoftTone;

/**
 * An output for a passive buzzer.
 */
public class SoftToneOutput {
	/**
	 * The minimum planned duration of a step.
	 */
	protected static final double MIN_STEP_DURATION = 1;
	/**
	 * The ID of the corresponding PIN Output.
	 */
	private final int mPinId;
	/**
	 * The current logarithmic frequency.
	 */
	private double mLogFrequency = -1;

	/**
	 * Create a PWM output for a PIN.
	 *
	 * @param pin The output PIN
	 */
	public SoftToneOutput(final Pin pin) {
		mPinId = pin.getAddress();
		Gpio.wiringPiSetup();
		SoftTone.softToneCreate(mPinId);
		ShutdownUtil.prepareShutdown();
	}

	/**
	 * Set the frequency of the Buzzer output.
	 *
	 * @param frequency The frequency of the buzzer.
	 */
	public void setFrequency(final int frequency) {
		if (!ShutdownUtil.isShutdown()) {
			if (frequency > 0) {
				SoftTone.softToneWrite(mPinId, frequency);
				mLogFrequency = Math.log(frequency);
			}
			else {
				SoftTone.softToneWrite(mPinId, frequency);
				mLogFrequency = -1;
			}
		}
	}

	/**
	 * Move to a new value over certain time.
	 *
	 * @param targetFrequency The target value.
	 * @param duration The duration.
	 */
	public void moveToValue(final int targetFrequency, final long duration) {
		final long startTime = System.currentTimeMillis();
		final double startLogFrequency = mLogFrequency;
		final double targetLogFrequency = Math.log(targetFrequency);
		final int steps = (int) (duration / SoftToneOutput.MIN_STEP_DURATION);

		for (int i = 1; i <= steps; i++) {
			SoftToneOutput.sleepIfRequired(startTime + duration * i / steps - System.currentTimeMillis());
			setFrequency((int) Math.exp(startLogFrequency + (targetLogFrequency - startLogFrequency) * i / steps));
		}
		SoftToneOutput.sleepIfRequired(startTime + duration - System.currentTimeMillis());
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
			catch (final InterruptedException e) {
				// Ignore
			}
		}
	}
}
