package de.jeisfeld.pi.lut.core.command;

import de.jeisfeld.pi.lut.core.ButtonStatus;

/**
 * Command for reading digital inputs.
 */
public class DigitalRead implements ReadCommand {

	@Override
	public final String getSerialString() {
		return "S";
	}

	@Override
	public final void processResponse(final String response, final ButtonStatus buttonStatus) {
		buttonStatus.setDigitalResult(response);
	}

}
