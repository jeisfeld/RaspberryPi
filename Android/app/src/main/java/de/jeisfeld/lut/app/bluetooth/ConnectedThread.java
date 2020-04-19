package de.jeisfeld.lut.app.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import de.jeisfeld.lut.app.bluetooth.BluetoothMessageHandler.MessageType;

public class ConnectedThread extends Thread {
	/**
	 * The logging tag.
	 */
	private static final String TAG = "JE.LuT.ConnectedThread";
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
	private final InputStream mInStream;
	/**
	 * The output stream.
	 */
	private final OutputStream mOutStream;

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
		InputStream tmpIn = null;
		OutputStream tmpOut = null;

		// Get the input and output streams; using temp objects because
		// member streams are final.
		try {
			tmpIn = socket.getInputStream();
		}
		catch (IOException e) {
			Log.e(ConnectedThread.TAG, "Error occurred when creating input stream", e);
		}
		try {
			tmpOut = socket.getOutputStream();
		}
		catch (IOException e) {
			Log.e(ConnectedThread.TAG, "Error occurred when creating output stream", e);
		}

		mInStream = tmpIn;
		mOutStream = tmpOut;
	}

	@Override
	public final void run() {
		byte[] buffer = new byte[1024]; // MAGIC_NUMBER
		int numBytes; // bytes returned from read()

		// Keep listening to the InputStream until an exception occurs.
		while (true) {
			try {
				// Read from the InputStream.
				numBytes = mInStream.read(buffer);
				// Send the obtained bytes to the UI activity.
				mHandler.sendMessage(MessageType.READ, numBytes, buffer);
			}
			catch (IOException e) {
				Log.w(ConnectedThread.TAG, "Input stream was disconnected");
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
	protected void write(final String data) {
		try {
			mOutStream.write(data.getBytes(StandardCharsets.UTF_8));
			mHandler.sendMessage(MessageType.WRITE, data);
		}
		catch (IOException e) {
			Log.e(ConnectedThread.TAG, "Error occurred when sending data", e);
		}
	}

}
