package de.jeisfeld.pi.bluetooth;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

/**
 * Thread for creating a bluetooth connection.
 */
public class ConnectThread extends Thread {

	/**
	 * Constructor.
	 */
	public ConnectThread() {
	}

	@Override
	public final void run() {
		LocalDevice local = null;
		StreamConnectionNotifier notifier;
		StreamConnection connection = null;

		// setup the server to listen for connection
		try {
			local = LocalDevice.getLocalDevice();
			local.setDiscoverable(DiscoveryAgent.GIAC);

			UUID uuid = new UUID("e91868ef27844d1899b0408878da815a", false);
			String url = "btspp://localhost:" + uuid.toString() + ";name=RemoteBluetooth";
			notifier = (StreamConnectionNotifier) Connector.open(url);
		}
		catch (Exception e) {
			e.printStackTrace();
			return;
		}
		// waiting for connection
		while (true) {
			try {
				System.out.println("waiting for connection...");
				connection = notifier.acceptAndOpen();
				System.out.println("got connection.");

				Thread processThread = new ConnectedThread(connection);
				processThread.start();
			}
			catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}

}
