package de.jeisfeld.pi.examples;

import java.io.IOException;

import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.system.SystemInfo;

import de.jeisfeld.pi.gpio.DigitalInput;
import de.jeisfeld.pi.gpio.DigitalInput.OnLongPressListener;
import de.jeisfeld.pi.gpio.DigitalInput.OnStableStateChangeListener;
import de.jeisfeld.pi.gpio.DigitalOutput;

/**
 * A controller for the fan.
 */
public class FanControl {
	/**
	 * Temperature when fan switches on.
	 */
	private static final float FAN_ON_TEMPERATURE = 60;
	/**
	 * Temperature when fan switches off.
	 */
	private static final float FAN_OFF_TEMPERATURE = 50;
	/**
	 * The current fan status.
	 */
	private boolean mStatus = false;

	/**
	 * Main method.
	 *
	 * @param args The command line arguments (not required).
	 */
	public static void main(final String[] args) {
		new FanControl().run();
	}

	/**
	 * Run the blinking LED.
	 */
	private void run() {
		final DigitalInput buttonPin = new DigitalInput(RaspiPin.GPIO_28, true);

		buttonPin.addListener(new OnLongPressListener() {
			@Override
			public void handleLongTrigger() {
				try {
					Runtime.getRuntime().exec("sudo shutdown -h now");
				}
				catch (IOException e) {
					// ignore
				}
			}
		});

		buttonPin.addListener(new OnStableStateChangeListener() {
			@Override
			public void handleTriggerOn() {
				mStatus = !mStatus;
			}

			@Override
			public void handleTriggerOff() {
				// do nothing
			}
		});

		final DigitalOutput outputPin = new DigitalOutput(RaspiPin.GPIO_29, true);
		float temperature = 0;

		while (true) {
			try {
				Thread.sleep(1000); // MAGIC_NUMBER
				temperature = SystemInfo.getCpuTemperature();
			}
			catch (InterruptedException | IOException e) {
				// ignore
			}

			if (temperature >= FanControl.FAN_ON_TEMPERATURE) {
				mStatus = true;
			}
			else if (temperature < FanControl.FAN_OFF_TEMPERATURE) {
				mStatus = false;
			}

			outputPin.setValue(mStatus);
		}
	}

}
