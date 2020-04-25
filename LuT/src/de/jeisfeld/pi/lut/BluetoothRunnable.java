package de.jeisfeld.pi.lut;

import de.jeisfeld.lut.bluetooth.message.ProcessingBluetoothMessage;
import de.jeisfeld.pi.bluetooth.ConnectThread;

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
	 *
	 * @param connectThread The bluetooth connection.
	 */
	void sendStatus(ConnectThread connectThread);

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
