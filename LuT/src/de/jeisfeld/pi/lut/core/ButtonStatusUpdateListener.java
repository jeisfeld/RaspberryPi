package de.jeisfeld.pi.lut.core;

/**
 * Listener for button status updates.
 */
public interface ButtonStatusUpdateListener {
	/**
	 * Callback on update of button status update.
	 *
	 * @param status The updated button status.
	 */
	void onButtonStatusUpdated(ButtonStatus status);
}
