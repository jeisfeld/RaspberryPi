package de.jeisfeld.pi.examples;

import com.pi4j.io.gpio.RaspiPin;

import de.jeisfeld.pi.gpio.DigitalInput;
import de.jeisfeld.pi.gpio.DigitalOutput;

/**
 * An LED that is on if a button is pressed.
 */
public class ButtonLed {
	/**
	 * Main method.
	 *
	 * @param args The command line arguments (not required).
	 * @throws InterruptedException if interrupted
	 */
	public static void main(final String[] args) throws InterruptedException {
		new ButtonLed().run();
	}

	/**
	 * Run the process handling the LED button.
	 *
	 * @throws InterruptedException if interrupted
	 */
	private void run() throws InterruptedException {
		final DigitalOutput led = new DigitalOutput(RaspiPin.GPIO_00, true);
		final DigitalInput button = new DigitalInput(RaspiPin.GPIO_05, false);

		button.addListener(new DigitalInput.OnStateChangeListener() {
			@Override
			public void handleStateChange(final boolean state) {
				led.setValue(state);
			}
		});

		while (true) {
			Thread.sleep(60000); // MAGIC_NUMBER
		}
	}

}
