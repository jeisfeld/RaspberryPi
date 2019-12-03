package de.jeisfeld.pi.lut.core.command;

import de.jeisfeld.pi.lut.core.ButtonStatus;

/**
 * Command for reading analog inputs.
 */
public class AnalogRead implements ReadCommand {

	@Override
	public final String getSerialString() {
		return "A";
	}

	@Override
	public final void processResponse(final String response, final ButtonStatus buttonStatus) {
		buttonStatus.setAnalogResult(response);
	}

	@Override
	public final String toString() {
		return getSerialString();
	}
}
