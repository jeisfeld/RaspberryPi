package de.jeisfeld.pi.examples;

import com.pi4j.io.gpio.RaspiPin;

import de.jeisfeld.pi.gpio.M74hc595;

/**
 * An LED bar showing a signal sequence, being addressed voa MC74HC595.
 */
public class LedBar2 {
	/**
	 * The MC74HC595 managing the LED bar.
	 */
	private M74hc595 mM74hc595;

	/**
	 * Main method.
	 *
	 * @param args The command line arguments (not required).
	 * @throws InterruptedException if interrupted
	 */
	public static void main(final String[] args) throws InterruptedException {
		new LedBar2().run();
	}

	/**
	 * Run the LED bar.
	 *
	 * @throws InterruptedException if interrupted
	 */
	private void run() throws InterruptedException {
		mM74hc595 = new M74hc595(RaspiPin.GPIO_00, RaspiPin.GPIO_02, RaspiPin.GPIO_03, false);

		while (true) {
			display(0b10000000, 100); // MAGIC_NUMBER
			display(0b01000000, 100); // MAGIC_NUMBER
			display(0b00100000, 100); // MAGIC_NUMBER
			display(0b00010000, 100); // MAGIC_NUMBER
			display(0b00011000, 40); // MAGIC_NUMBER
			display(0b00111100, 60); // MAGIC_NUMBER
			display(0b01111110, 60); // MAGIC_NUMBER
			display(0b11111111, 80); // MAGIC_NUMBER
			display(0b01111110, 60); // MAGIC_NUMBER
			display(0b00111100, 60); // MAGIC_NUMBER
			display(0b00011000, 40); // MAGIC_NUMBER
			display(0b00001000, 100); // MAGIC_NUMBER
			display(0b00000100, 100); // MAGIC_NUMBER
			display(0b00000010, 100); // MAGIC_NUMBER
			display(0b00000001, 100); // MAGIC_NUMBER
		}
	}

	/**
	 * Display a data byte for a certain duration.
	 *
	 * @param dataByte The data byte.
	 * @param duration The duration in milliseconds.
	 * @throws InterruptedException if interrupted.
	 */
	private void display(final int dataByte, final int duration) throws InterruptedException {
		mM74hc595.setByte(dataByte);
		Thread.sleep(duration);
	}

}
