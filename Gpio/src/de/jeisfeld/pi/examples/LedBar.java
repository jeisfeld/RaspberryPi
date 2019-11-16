package de.jeisfeld.pi.examples;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

import de.jeisfeld.pi.gpio.DigitalOutput;
import de.jeisfeld.pi.gpio.ShutdownUtil;

/**
 * An LED bar showing a signal sequence.
 */
public class LedBar {
	/**
	 * The pins of the LED bar.
	 */
	public static final Pin[] PINS = {RaspiPin.GPIO_00, RaspiPin.GPIO_01, RaspiPin.GPIO_02, RaspiPin.GPIO_03, RaspiPin.GPIO_04,
			RaspiPin.GPIO_05, RaspiPin.GPIO_06, RaspiPin.GPIO_08, RaspiPin.GPIO_09, RaspiPin.GPIO_10};

	/**
	 * Main method.
	 *
	 * @param args The command line arguments (not required).
	 * @throws InterruptedException if interrupted
	 */
	public static void main(final String[] args) throws InterruptedException {
		new LedBar().run();
	}

	/**
	 * Run the LED bar.
	 *
	 * @throws InterruptedException if interrupted
	 */
	private void run() throws InterruptedException {
		final DigitalOutput[] leds = DigitalOutput.createDigitalOutputs(true, PINS);

		while (true) {
			display(leds, 0b0000000000, 20); // MAGIC_NUMBER
			display(leds, 0b0000110000, 40); // MAGIC_NUMBER
			display(leds, 0b0001111000, 60); // MAGIC_NUMBER
			display(leds, 0b0011111100, 60); // MAGIC_NUMBER
			display(leds, 0b0111111110, 40); // MAGIC_NUMBER
			display(leds, 0b1111111111, 20); // MAGIC_NUMBER
			display(leds, 0b0111111110, 40); // MAGIC_NUMBER
			display(leds, 0b0011111100, 60); // MAGIC_NUMBER
			display(leds, 0b0001111000, 60); // MAGIC_NUMBER
			display(leds, 0b0000110000, 40); // MAGIC_NUMBER
		}
	}

	/**
	 * Display a pattern on the LED bar.
	 *
	 * @param leds The LEDs
	 * @param state The LED status in binary representation
	 * @param duration The duration that this status should hold.
	 * @throws InterruptedException when interrupted.
	 */
	private static void display(final DigitalOutput[] leds, final int state, final int duration) throws InterruptedException {
		if (ShutdownUtil.isShutdown()) {
			return;
		}
		for (int i = 0; i < leds.length; i++) {
			boolean isSelected = ((state >> i) & 1) > 0;
			leds[i].setValue(isSelected);
		}
		Thread.sleep(duration);
	}

}
