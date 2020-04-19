package de.jeisfeld.pi.bluetooth;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.microedition.io.StreamConnection;

public class ConnectedThread extends Thread {
	/**
	 * The connection.
	 */
	private final StreamConnection mConnection;

	/**
	 * Constructor.
	 *
	 * @param connection The connection.
	 */
	public ConnectedThread(final StreamConnection connection) {
		mConnection = connection;
	}

	@Override
	public final void run() {
		try {
			// prepare to receive data
			InputStream inputStream = mConnection.openInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

			System.out.println("waiting for input");

			while (true) {
				String line = reader.readLine();
				if (line != null) {
					System.out.println(line);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
