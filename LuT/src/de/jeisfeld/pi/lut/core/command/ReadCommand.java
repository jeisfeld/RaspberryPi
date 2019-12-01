package de.jeisfeld.pi.lut.core.command;

import de.jeisfeld.pi.lut.core.ButtonStatus;

/**
 * A command with response.
 */
public interface ReadCommand extends Command {
	/**
	 * Process the serial response.
	 *
	 * @param response The message to process.
	 * @param buttonStatus The ButtonStatus object holding response data.
	 */
	void processResponse(String response, ButtonStatus buttonStatus);
}
