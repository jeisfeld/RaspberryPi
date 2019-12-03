package de.jeisfeld.pi.lut.core;

import java.io.IOException;

import de.jeisfeld.pi.lut.core.ButtonStatus.OnLongPressListener;

/**
 * A button listener that shuts down the device when long pressing the button.
 */
public class ShutdownListener extends OnLongPressListener {

	@Override
	public final void handleLongTrigger() {
		try {
			Runtime.getRuntime().exec("sudo shutdown -h now");
		}
		catch (IOException e) {
			// ignore
		}
	}
}
