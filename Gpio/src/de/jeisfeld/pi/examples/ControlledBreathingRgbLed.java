package de.jeisfeld.pi.examples;

import java.awt.Color;
import java.io.IOException;
import java.util.Random;

import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.i2c.I2CFactory.UnsupportedBusNumberException;

import de.jeisfeld.pi.gpio.DigitalInput;
import de.jeisfeld.pi.gpio.Pcf8591;
import de.jeisfeld.pi.gpio.PwmOutputArray;

/**
 * An RGB LED that is switchable by button and which simulates candle light.
 * Long press shuts down the server.
 */
public class ControlledBreathingRgbLed {
	/**
	 * Flag indicating if the light should be on.
	 */
	private int mMode = 1;

	/**
	 * Main method.
	 *
	 * @param args The command line arguments (not required).
	 * @throws InterruptedException if interrupted
	 * @throws IOException in case of I/O issues
	 * @throws UnsupportedBusNumberException if BUS is not valid
	 */
	public static void main(final String[] args) throws InterruptedException, UnsupportedBusNumberException, IOException {
		new ControlledBreathingRgbLed().run(); // MAGIC_NUMBER
	}

	/**
	 * Run the process putting listeners to the button and running the candle simulation.
	 *
	 * @throws InterruptedException if interrupted
	 * @throws IOException in case of I/O issues
	 * @throws UnsupportedBusNumberException if BUS is not valid
	 */
	private void run() throws InterruptedException, UnsupportedBusNumberException, IOException {
		configureButton();
		final PwmOutputArray led = new PwmOutputArray(true, RaspiPin.GPIO_03, RaspiPin.GPIO_02, RaspiPin.GPIO_00);
		final Pcf8591 device = new Pcf8591(2);

		final Random random = new Random();
		while (true) {
			double variation = device.read(Pcf8591.CHANNEL_1); // Typical good value: 0.5
			variation = variation * variation;
			double rulerBrightness = device.read(Pcf8591.CHANNEL_0);
			double randomForBrightness = random.nextDouble() - 0.5; // MAGIC_NUMBER
			randomForBrightness = randomForBrightness * randomForBrightness * randomForBrightness * 4 + 0.5; // MAGIC_NUMBER
			double brightness = rulerBrightness * (1 - variation * randomForBrightness);
			int duration = (int) (100 - 90 * variation + random.nextDouble() * (500 - 200 * variation)); // MAGIC_NUMBER

			switch (mMode) {
			case 1:
				// Mode 1: uses color temperature
				double rulerTemperature = device.read(Pcf8591.CHANNEL_2) * 2 + 6.8; // MAGIC_NUMBER. Good value: 0.3 -> 7.4
				double logTemperature = rulerTemperature + (random.nextDouble() - 0.5) * variation; // MAGIC_NUMBER

				Color color = ControlledBreathingRgbLed.convertColorTemperature(Math.exp(logTemperature), brightness);
				led.moveToColor(duration, color);
				break;
			case 2:
				// Mode 2: uses HSB range
				double baseColor = ((device.read(Pcf8591.CHANNEL_2) - 0.5) / 3) % 1; // MAGIC_NUMBER

				double randomForHue = random.nextDouble() - 0.5; // MAGIC_NUMBER
				double hue = (baseColor + variation * randomForHue * randomForHue * randomForHue * 0.3) % 1; // MAGIC_NUMBER
				double randomForSaturation = random.nextDouble();
				double saturation = 0.85 + (0.15 - randomForSaturation * randomForSaturation * randomForSaturation) * variation; // MAGIC_NUMBER

				Color hsbColor = Color.getHSBColor((float) hue, (float) saturation, (float) brightness);
				led.moveToColor(duration, hsbColor);
				break;
			default:
				led.moveToValue(100, 0, 0, 0); // MAGIC_NUMBER
				break;
			}

		}
	}

	/**
	 * Configure the button that may be used to change the light mode or to shutdown the system.
	 */
	private void configureButton() {
		final DigitalInput button = new DigitalInput(RaspiPin.GPIO_29, true);

		button.addListener(new DigitalInput.OnStableStateChangeListener() {
			@Override
			public void handleTriggerOn() {
				mMode = (mMode + 1) % 3; // MAGIC_NUMBER
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

	}

	/**
	 * Convert color temperature into RGB color.
	 *
	 * @param temperature The color temperature in Kelvin.
	 * @param brightness The brighntess (between 0 and 1)
	 * @return The resulting RGB color.
	 */
	private static Color convertColorTemperature(final double temperature, final double brightness) {
		double red, green, blue;
		if (temperature < 6600) { // MAGIC_NUMBER
			red = 1;
			green = 0.39 * Math.log(temperature / 100) - 0.634; // MAGIC_NUMBER
			blue = 0.543 * Math.log(temperature / 100 - 10) - 1.186; // MAGIC_NUMBER
		}
		else {
			red = 1.269 * Math.pow(temperature / 100 - 60, -0.1332); // MAGIC_NUMBER
			green = 1.145 * Math.pow(temperature / 100 - 60, 0.0755); // MAGIC_NUMBER
			blue = 1;
		}
		red = ControlledBreathingRgbLed.ensureInRange01(red) * brightness;
		green = ControlledBreathingRgbLed.ensureInRange01(green) * brightness;
		blue = ControlledBreathingRgbLed.ensureInRange01(blue) * brightness;
		return new Color((float) red, (float) green, (float) blue);
	}

	/**
	 * Ensure that a double value is between 0 and 1.
	 *
	 * @param value The value
	 * @return The corresponding value between 0 and 1.
	 */
	private static double ensureInRange01(final double value) {
		if (value < 0) {
			return 0;
		}
		else if (value > 1) {
			return 1;
		}
		else {
			return value;
		}
	}

}
