package de.jeisfeld.pi.bluetooth;

import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import de.jeisfeld.lut.bluetooth.message.Message;
import de.jeisfeld.pi.util.Logger;

/**
 * Thread for creating a bluetooth connection.
 */
public class ConnectThread extends Thread {
	/**
	 * The connected thread.
	 */
	private ConnectedThread mConnectedThread;
	/**
	 * The message handler.
	 */
	private final BluetoothMessageHandler mHandler;

	/**
	 * Constructor.
	 *
	 * @param handler the bluetooth message handler.
	 */
	public ConnectThread(final BluetoothMessageHandler handler) {
		mHandler = handler;
	}

	@Override
	public final void run() {
		StreamConnectionNotifier notifier = null;
		StreamConnection connection = null;

		// setup the server to listen for connection
		while (notifier == null) {
			try {
				UUID uuid = new UUID("e91868ef27844d1899b0408878da815a", false);
				String url = "btspp://localhost:" + uuid.toString() + ";name=RemoteBluetooth";
				notifier = (StreamConnectionNotifier) Connector.open(url);
			}
			catch (Exception e) {
				Logger.error(e);
			}

			if (notifier == null) {
				try {
					Thread.sleep(1000); // MAGIC_NUMBER
				}
				catch (InterruptedException e) {
					return;
				}
			}
		}

		// waiting for connection
		while (true) {
			// Retry, so that after closing a connection, the next one will immediately reopen.
			try {
				Logger.info("waiting for connection...");
				connection = notifier.acceptAndOpen();
				Logger.info("got connection.");

				ConnectedThread newConnectedThread = new ConnectedThread(mHandler, connection);
				if (mConnectedThread != null) {
					mConnectedThread.interrupt();
				}
				mConnectedThread = newConnectedThread;
				mConnectedThread.start();
			}
			catch (Exception e) {
				Logger.error(e);
				return;
			}
		}
	}

	/**
	 * Write a message.
	 *
	 * @param message The message.
	 */
	private void write(final String message) {
		if (mConnectedThread == null) {
			Logger.error(new RuntimeException("Failed to send message - no connection available"));
		}
		else {
			mConnectedThread.write(message);
		}
	}

	/**
	 * Write a message.
	 *
	 * @param message The message.
	 */
	public void write(final Message message) {
		write(message.toString());
	}

}
