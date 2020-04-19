package de.jeisfeld.pi.bluetooth;

/**
 * Standalone bluetooth server.
 */
public class RemoteBluetoothServer {
	/**
	 * Main method.
	 *
	 * @param args Command line arguments.
	 */
	public static void main(final String[] args) {
		new ConnectThread().start();
	}
}
