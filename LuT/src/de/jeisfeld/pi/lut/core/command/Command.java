package de.jeisfeld.pi.lut.core.command;

/**
 * Interface for commands to LuT.
 */
public interface Command {
	/**
	 * Get the String to send for this command via serial output.
	 *
	 * @return The String to send for this command via serial output.
	 */
	String getSerialString();
}
