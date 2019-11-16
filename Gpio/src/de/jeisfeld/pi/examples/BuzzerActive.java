package de.jeisfeld.pi.examples;

import com.pi4j.io.gpio.RaspiPin;

import de.jeisfeld.pi.gpio.DigitalInput;
import de.jeisfeld.pi.gpio.DigitalOutput;

/**
 * An Buzzer that is ringing if a button is pressed.
 */
public class BuzzerActive {
	/**
	 * Main method.
	 *
	 * @param args The command line arguments (not required).
	 * @throws InterruptedException if interrupted
	 */
	public static void main(final String[] args) throws InterruptedException {
		new BuzzerActive().run();
	}

	/**
	 * Run the process handling buzzer LED button.
	 *
	 * @throws InterruptedException if interrupted
	 */
	private void run() throws InterruptedException {
		final DigitalOutput buzzer = new DigitalOutput(RaspiPin.GPIO_00, false);
		final DigitalInput button = new DigitalInput(RaspiPin.GPIO_01, true);

		button.addListener(new DigitalInput.OnStateChangeListener() {
			@Override
			public void handleStateChange(final boolean state) {
				buzzer.setValue(state);
			}
		});

		while (true) {
			Thread.sleep(60000); // MAGIC_NUMBER
		}
	}

}
