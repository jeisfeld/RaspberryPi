package de.jeisfeld.pi.lut.core;

import java.io.IOException;

import de.jeisfeld.pi.lut.core.ButtonStatus.ButtonListener;

/**
 * A button listener that shuts down the device when pressing the button.
 */
public class ShutdownListener implements ButtonListener {

	@Override
	public final void handleButtonDown() {
		try {
			Runtime.getRuntime().exec("sudo shutdown -h now");
		}
		catch (IOException e) {
			// ignore
		}
	}

	@Override
	public void handleButtonUp() {
		// do nothing.
	}

}
