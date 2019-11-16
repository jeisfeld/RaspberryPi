package de.jeisfeld.pi.examples;

import java.io.IOException;
import java.util.Random;

import com.pi4j.io.gpio.RaspiPin;

import de.jeisfeld.pi.gpio.DigitalInput;
import de.jeisfeld.pi.gpio.PwmOutputArray;

/**
 * An RGB LED that is switchable by button and which simulates candle light.
 * Long press shuts down the server.
 */
public class BreathingRgbLed {
	/**
	 * The variation intensities.
	 */
	private static final double[] MODES = {-1, 0.8, 0.4, 0.2, 0.1, 0.05}; // MAGIC_NUMBER
	/**
	 * The current mode.
	 */
	private int mMode = 0;

	/**
	 * Main method.
	 *
	 * @param args The command line arguments (not required).
	 * @throws InterruptedException if interrupted
	 */
	public static void main(final String[] args) throws InterruptedException {
		new BreathingRgbLed().run(); // MAGIC_NUMBER
	}

	/**
	 * Run the process putting listeners to the button and running the candle simulation.
	 *
	 * @throws InterruptedException if interrupted
	 */
	private void run() throws InterruptedException {
		final PwmOutputArray led = new PwmOutputArray(true, RaspiPin.GPIO_00, RaspiPin.GPIO_05, RaspiPin.GPIO_02);

		final DigitalInput button = new DigitalInput(RaspiPin.GPIO_01, true);

		button.addListener(new DigitalInput.OnStableStateChangeListener() {
			@Override
			public void handleTriggerOn() {
				mMode = (mMode + 1) % BreathingRgbLed.MODES.length;
			}

			@Override
			public void handleTriggerOff() {
				// do nothing
			}
		});

		button.addListener(new DigitalInput.OnLongPressListener() {
			@Override
			public void handleLongTrigger() {
				try {
					Runtime.getRuntime().exec("sudo shutdown -h now");
				}
				catch (final IOException e) {
					e.printStackTrace();
				}
			}
		});

		final Random random = new Random();
		while (true) {
			final double variation = BreathingRgbLed.MODES[mMode];
			if (variation >= 0) {
				final long duration = 10 + random.nextInt(200); // MAGIC_NUMBER
				final double targetRed = 1 - variation * random.nextDouble();
				final double targetGreen = targetRed * (0.5 + (random.nextDouble() - 0.5) * variation) * 0.8; // MAGIC_NUMBER
				final double targetBlue = targetGreen * (0.5 + (random.nextDouble() - 0.5) * variation) * 0.4; // MAGIC_NUMBER
				led.moveToValue(duration, targetRed, targetGreen, targetBlue);
			}
			else {
				led.moveToValue(100, 0, 0, 0); // MAGIC_NUMBER
			}
		}
	}
}
