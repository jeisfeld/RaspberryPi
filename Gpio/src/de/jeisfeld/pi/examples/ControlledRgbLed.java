package de.jeisfeld.pi.examples;

import java.io.IOException;

import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import de.jeisfeld.pi.gpio.Pcf8591;
import de.jeisfeld.pi.gpio.PwmOutputArray;

/**
 * An RGB LED that is switchable by button and which simulates candle light.
 * Long press shuts down the server.
 */
public class ControlledRgbLed {
	/**
	 * Main method.
	 *
	 * @param args The command line arguments (not required).
	 * @throws InterruptedException if interrupted
	 * @throws IOException in case of I/O issues
	 * @throws UnsupportedBusNumberException if BUS is not valid
	 */
	public static void main(final String[] args) throws InterruptedException, UnsupportedBusNumberException, IOException {
		new ControlledRgbLed().run(); // MAGIC_NUMBER
	}

	/**
	 * Run the process putting listeners to the button and running the candle simulation.
	 *
	 * @throws InterruptedException if interrupted
	 * @throws IOException in case of I/O issues
	 * @throws UnsupportedBusNumberException if BUS is not valid
	 */
	private void run() throws InterruptedException, UnsupportedBusNumberException, IOException {
		final PwmOutputArray led = new PwmOutputArray(true, RaspiPin.GPIO_03, RaspiPin.GPIO_02, RaspiPin.GPIO_00);
		final Pcf8591 device = new Pcf8591(2);

		while (true) {
			double red = device.read(Pcf8591.CHANNEL_0);
			double green = device.read(Pcf8591.CHANNEL_1);
			double blue = device.read(Pcf8591.CHANNEL_2);
			led.setValue(red, green, blue);
			Thread.sleep(100); // MAGIC_NUMBER
		}
	}

}
