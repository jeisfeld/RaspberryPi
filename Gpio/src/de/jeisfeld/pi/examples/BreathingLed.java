package de.jeisfeld.pi.examples;

import java.util.Random;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

import de.jeisfeld.pi.gpio.PwmOutput;
import de.jeisfeld.pi.gpio.ShutdownUtil;

/**
 * A single LED which simulates candle light.
 */
public class BreathingLed {
	/**
	 * Main method.
	 *
	 * @param args The command line arguments (not required).
	 * @throws InterruptedException if interrupted
	 */
	public static void main(final String[] args) throws InterruptedException {
		new BreathingLed().run();
	}

	/**
	 * Run the breathing LED.
	 *
	 * @throws InterruptedException if interrupted
	 */
	public void run() throws InterruptedException {
		final Pin pin = RaspiPin.GPIO_02;

		final PwmOutput led = new PwmOutput(pin, false);

		ShutdownUtil.prepareShutdown();

		Random random = new Random();
		while (true) {
			long duration = 10 + random.nextInt(200); // MAGIC_NUMBER
			double targetValue = random.nextDouble();
			led.moveToValue(targetValue, duration);
		}
	}
}
