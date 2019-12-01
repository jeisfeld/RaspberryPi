package de.jeisfeld.pi.lut.core.command;

/**
 * A command without response.
 */
public interface WriteCommand extends Command {
	/**
	 * Check if the current command overrides an other command.
	 *
	 * @param other The other command.
	 * @return If the current command overrides an other command.
	 */
	boolean overrides(WriteCommand other);
}
