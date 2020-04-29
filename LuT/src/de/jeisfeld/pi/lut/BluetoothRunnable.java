package de.jeisfeld.pi.lut;

import de.jeisfeld.lut.bluetooth.message.ProcessingBluetoothMessage;

/**
 * A thread that can be used for processing from bluetooth.
 */
public interface BluetoothRunnable extends Runnable {
	/**
	 * Update the data from triggering message.
	 *
	 * @param message The message.
	 */
	void updateValues(ProcessingBluetoothMessage message);

	/**
	 * Send the status via bluetooth.
	 */
	void sendStatus();

	/**
	 * Get information if thread is running.
	 *
	 * @return true if running.
	 */
	boolean isRunning();

	/**
	 * Stop this thread.
	 */
	void stop();
}
