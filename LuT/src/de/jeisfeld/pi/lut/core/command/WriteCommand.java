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

	/**
	 * Set the duration.
	 *
	 * @param duration The duration
	 */
	void setDuration(long duration);

	/**
	 * Get the duration.
	 *
	 * @return The duration.
	 */
	long getDuration();

	/**
	 * Set flag to do no override, but queue behind other commands.
	 */
	void setNoOverride();

	/**
	 * Get information if override of queued commands should be done.
	 *
	 * @return the override flag.
	 */
	boolean isOverride();

}
