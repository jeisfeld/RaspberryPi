package de.jeisfeld.lut.app.bluetooth;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import de.jeisfeld.lut.app.bluetooth.BluetoothMessageHandler.MessageType;
import de.jeisfeld.lut.bluetooth.message.ConnectedMessage;

/**
 * Thread for sending and receiving data via bluetooth connection.
 */
public class ConnectedThread extends Thread {
	/**
	 * The logging tag.
	 */
	private static final String TAG = "LuT.JE.ConnectedThread";
	/**
	 * The context.
	 */
	private final Context mContext;
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
	 * Create a message thread.
	 *
	 * @param context The context.
	 * @param handler The bluetooth message handler.
	 * @param socket The socket.
	 */
	protected ConnectedThread(final Context context, final BluetoothMessageHandler handler, final BluetoothSocket socket) {
		mContext = context;
		mHandler = handler;
		BufferedReader tmpIn = null;
		BufferedWriter tmpOut = null;

		// Get the input and output streams; using temp objects because
		// member streams are final.
		try {
			tmpIn = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
		}
		catch (IOException e) {
			Log.e(TAG, "Error occurred when creating input stream", e);
		}
		try {
			tmpOut = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
		}
		catch (IOException e) {
			Log.e(TAG, "Error occurred when creating output stream", e);
		}

		mReader = tmpIn;
		mWriter = tmpOut;
		// update local GUI with connection
		mHandler.sendMessage(MessageType.CONNECTED, null);
		write(new ConnectedMessage().toString());
	}

	@Override
	public final void run() {
		// Keep listening to the InputStream until an exception occurs.
		while (true) {
			try {
				// Read from the InputStream.
				String data = mReader.readLine();
				// Send the obtained bytes to the UI activity.
				mHandler.sendMessage(MessageType.READ, data);
			}
			catch (IOException e) {
				Log.w(TAG, "Input stream was disconnected");
				mHandler.sendReconnect();
				break;
			}
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
			mHandler.sendMessage(MessageType.WRITE, data);
		}
		catch (IOException e) {
			Log.e(TAG, "Error occurred when sending data", e);
		}
	}

}
