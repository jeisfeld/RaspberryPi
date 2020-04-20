package de.jeisfeld.pi.bluetooth;

/**
 * A handler for bluetooth messages.
 */
public interface BluetoothMessageHandler {
	/**
	 * Callback on message received.
	 *
	 * @param data The message data.
	 */
	void onMessageReceived(String data);
}
