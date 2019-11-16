package de.jeisfeld.pi.examples;

import com.pi4j.io.gpio.RaspiPin;

import de.jeisfeld.pi.gpio.DigitalInput;
import de.jeisfeld.pi.gpio.SoftToneOutput;

/**
 * An Buzzer that is ringing if a button is pressed.
 */
public class BuzzerPassive {
	/**
	 * Flag indicating if the buzzer is running.
	 */
	private boolean mIsRunning = false;

	/**
	 * Main method.
	 *
	 * @param args The command line arguments (not required).
	 * @throws InterruptedException if interrupted
	 */
	public static void main(final String[] args) throws InterruptedException {
		new BuzzerPassive().run();
	}

	/**
	 * Run the process handling buzzer LED button.
	 *
	 * @throws InterruptedException if interrupted
	 */
	private void run() throws InterruptedException {
		final SoftToneOutput buzzer = new SoftToneOutput(RaspiPin.GPIO_00);
		final DigitalInput button = new DigitalInput(RaspiPin.GPIO_01, true);

		button.addListener(new DigitalInput.OnStateChangeListener() {
			@Override
			public void handleStateChange(final boolean state) {
				mIsRunning = state;
				buzzer.setFrequency(state ? 400 : 0); // MAGIC_NUMBER
			}
		});

		while (true) {
			if (mIsRunning) {
				buzzer.moveToValue(1000, 200); // MAGIC_NUMBER
				buzzer.moveToValue(400, 200); // MAGIC_NUMBER
			}
			else {
				buzzer.setFrequency(0);
				Thread.sleep(100); // MAGIC_NUMBER
			}
		}
	}

}
