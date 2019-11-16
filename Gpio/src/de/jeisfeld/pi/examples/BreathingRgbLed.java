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
	 * Flag indicating if the LED is switched on.
	 */
	private boolean mIsRunning = false;

	/**
	 * Main method.
	 *
	 * @param args The command line arguments (not required).
	 * @throws InterruptedException if interrupted
	 */
	public static void main(final String[] args) throws InterruptedException {
		new BreathingRgbLed().run();
	}

	/**
	 * Run the process putting listeners to the button and running the candle simulation.
	 *
	 * @throws InterruptedException if interrupted
	 */
	private void run() throws InterruptedException {
		final PwmOutputArray led = new PwmOutputArray(true, RaspiPin.GPIO_00, RaspiPin.GPIO_01, RaspiPin.GPIO_02);

		final DigitalInput button = new DigitalInput(RaspiPin.GPIO_05, false);

		button.addListener(new DigitalInput.OnStableStateChangeListener() {
			@Override
			public void handleTriggerOn() {
				mIsRunning = !mIsRunning;
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
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		Random random = new Random();
		while (true) {
			if (mIsRunning) {
				long duration = 10 + random.nextInt(200); // MAGIC_NUMBER
				double targetRed = random.nextDouble();
				double targetGreen = random.nextDouble() * targetRed * 0.6; // MAGIC_NUMBER
				double targetBlue = random.nextDouble() * targetGreen * 0.3; // MAGIC_NUMBER
				led.moveToValue(duration, targetRed, targetGreen, targetBlue);
			}
			else {
				led.moveToValue(100, 0, 0, 0); // MAGIC_NUMBER
			}
		}
	}
}
