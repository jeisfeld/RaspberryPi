package de.jeisfeld.pi.bluetooth;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import javax.microedition.io.StreamConnection;

import de.jeisfeld.pi.util.Logger;

/**
 * Thread for sending and receiving data via bluetooth connection.
 */
public class ConnectedThread extends Thread {
	/**
	 * The message handler.
	 */
	private final BluetoothMessageHandler mHandler;
	/**
	 * The input stream.
	 */
	private final BufferedReader mReader;
	/**
	 * The output stream.
	 */
	private final BufferedWriter mWriter;

	/**
	 * Constructor.
	 *
	 * @param handler The message handler.
	 * @param connection The connection.
	 */
	public ConnectedThread(final BluetoothMessageHandler handler, final StreamConnection connection) {
		mHandler = handler;
		BufferedReader tmpIn = null;
		BufferedWriter tmpOut = null;
		try {
			tmpIn = new BufferedReader(new InputStreamReader(connection.openInputStream(), StandardCharsets.UTF_8));
		}
		catch (IOException e) {
			Logger.error(e);
		}
		try {
			tmpOut = new BufferedWriter(new OutputStreamWriter(connection.openOutputStream(), StandardCharsets.UTF_8));
		}
		catch (IOException e) {
			Logger.error(e);
		}

		mReader = tmpIn;
		mWriter = tmpOut;
	}

	@Override
	public final void run() {
		try {
			// prepare to receive data
			Logger.info("waiting for input");

			while (true) {
				String line = mReader.readLine();
				if (line != null) {
					mHandler.onMessageReceived(line);
				}
			}
		}
		catch (InterruptedIOException e) {
			Logger.info("Connected thread interrupted");
		}
		catch (IOException e) {
			Logger.error(e);
		}
	}

	/**
	 * Write a message via bluetooth.
	 *
	 * @param data The data to be written.
	 */
	protected synchronized void write(final String data) {
		try {
			mWriter.write(data);
			mWriter.newLine();
			mWriter.flush();
		}
		catch (IOException e) {
			Logger.error(e);
		}
	}

}
