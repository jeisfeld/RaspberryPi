package de.jeisfeld.pi.bluetooth;

import de.jeisfeld.pi.util.Logger;

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
		Logger.setLogDetails(true);

		ConnectThread connectThread = new ConnectThread(new BluetoothMessageHandler() {
			@Override
			public void onMessageReceived(final String data) {
				Logger.log(data);
			}
		});
		connectThread.start();
	}
}
