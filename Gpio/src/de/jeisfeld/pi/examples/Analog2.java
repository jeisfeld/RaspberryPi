package de.jeisfeld.pi.examples;

import java.io.IOException;

import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import de.jeisfeld.pi.gpio.Pcf8591;
import de.jeisfeld.pi.gpio.PwmOutput;

/**
 * A sample for reading analog data from PCF8591 and writing to PWM.
 */
public class Analog2 {
	/**
	 * Main method.
	 *
	 * @param args The command line arguments (not required).
	 * @throws InterruptedException if interrupted
	 * @throws IOException in case of I/O issues
	 * @throws UnsupportedBusNumberException if BUS is not valid
	 */
	public static void main(final String[] args) throws InterruptedException, IOException, UnsupportedBusNumberException {
		new Analog2().run();
	}

	/**
	 * Run the blinking LED.
	 *
	 * @throws InterruptedException if interrupted
	 * @throws IOException in case of I/O issues
	 * @throws UnsupportedBusNumberException if BUS is not valid
	 */
	private void run() throws InterruptedException, IOException, UnsupportedBusNumberException {
		final Pcf8591 device = new Pcf8591();
		PwmOutput led = new PwmOutput(RaspiPin.GPIO_02, false);

		while (true) {
			final double response = device.read();
			// SYSTEMOUT:OFF
			System.out.println("Read value: " + response);
			// SYSTEMOUT:ON

			double request = 2 * response - 1;

			led.setValue(request);

			Thread.sleep(100); // MAGIC_NUMBER
		}
	}

}
