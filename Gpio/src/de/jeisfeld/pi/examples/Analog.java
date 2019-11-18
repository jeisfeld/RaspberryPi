package de.jeisfeld.pi.examples;

import java.io.IOException;

import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import de.jeisfeld.pi.gpio.Pcf8591;

/**
 * A sample for reading/writing analog data from PCF8591.
 */
public class Analog {
	/**
	 * Main method.
	 *
	 * @param args The command line arguments (not required).
	 * @throws InterruptedException if interrupted
	 * @throws IOException in case of I/O issues
	 * @throws UnsupportedBusNumberException if BUS is not valid
	 */
	public static void main(final String[] args) throws InterruptedException, IOException, UnsupportedBusNumberException {
		new Analog().run();
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

		while (true) {
			final int response = device.readByte();
			// SYSTEMOUT:OFF
			System.out.println("Read value: " + response);
			// SYSTEMOUT:ON
			final byte request = (byte) (128 + response / 4);
			device.write(request);

			Thread.sleep(100); // MAGIC_NUMBER
		}
	}

}
