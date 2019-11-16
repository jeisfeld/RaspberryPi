package de.jeisfeld.pi.examples;

import com.pi4j.io.gpio.RaspiPin;

import de.jeisfeld.pi.gpio.DigitalOutput;

/**
 * A blinking LED.
 */
public class Blink {

	/**
	 * Main method.
	 *
	 * @param args The command line arguments (not required).
	 * @throws InterruptedException if interrupted
	 */
	public static void main(final String[] args) throws InterruptedException {
		new Blink().run();
	}

	/**
	 * Run the blinking LED.
	 *
	 * @throws InterruptedException if interrupted
	 */
	private void run() throws InterruptedException {
		final DigitalOutput outputPin = new DigitalOutput(RaspiPin.GPIO_00, true);

		outputPin.blink(1000); // MAGIC_NUMBER

		while (true) {
			Thread.sleep(60000); // MAGIC_NUMBER
		}
	}

}
