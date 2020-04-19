package de.jeisfeld.lut.app.bluetooth;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class ConnectThread extends Thread {
	/**
	 * The logging tag.
	 */
	private static final String TAG = "JE.LuT.ConnectThread";
	/**
	 * The UUID used for bluetooth.
	 */
	private static final UUID APP_UUID = UUID.fromString("e91868ef-2784-4d18-99b0-408878da815a");
	/**
	 * The MAC address of my Raspi 4.
	 */
	private static final String RASPI1_ADDRESS = "DC:A6:32:45:D8:04";
	/**
	 * The MAC address of my Raspi 3.
	 */
	private static final String RASPI2_ADDRESS = "B8:27:EB:CF:E5:E4";

	/**
	 * The bluetooth socket.
	 */
	private final BluetoothSocket mSocket;
	/**
	 * The context.
	 */
	private final Context mContext;
	/**
	 * The message handler.
	 */
	private final BluetoothMessageHandler mHandler;
	/**
	 * The connected thread.
	 */
	private ConnectedThread mConnectedThread = null;

	/**
	 * Constructor.
	 *
	 * @param context the context.
	 * @param handler a message handler.
	 */
	public ConnectThread(final Context context, final BluetoothMessageHandler handler) {
		this(context, handler, ConnectThread.RASPI2_ADDRESS);
	}

	/**
	 * Constructor.
	 *
	 * @param context the context.
	 * @param handler a message handler.
	 * @param mac     The MAC of the bluetooth device.
	 */
	public ConnectThread(final Context context, final BluetoothMessageHandler handler, final String mac) {
		mContext = context;
		mHandler = handler;
		BluetoothDevice device = getBluetoothDevice(mac);
		if (device == null) {
			Log.e(ConnectThread.TAG, "Failed to get Bluetooth device.");
			mSocket = null;
			return;
		}
		// Use a temporary object that is later assigned to mmSocket because mmSocket is final.
		BluetoothSocket tmpSocket = null;
		try {
			tmpSocket = device.createRfcommSocketToServiceRecord(ConnectThread.APP_UUID);
		}
		catch (IOException e) {
			Log.e(ConnectThread.TAG, "Socket's create() method failed", e);
		}
		mSocket = tmpSocket;
	}

	/**
	 * Get the bluetooth device from MAC.
	 *
	 * @param mac The MAC.
	 * @return The Bluetooth device. null if not found.
	 */
	private BluetoothDevice getBluetoothDevice(final String mac) {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			Log.e(ConnectThread.TAG, "Bluetooth not available");
			return null;
		}
		if (!bluetoothAdapter.isEnabled()) {
			Log.e(ConnectThread.TAG, "Bluetooth not enabled");
			return null;
		}

		Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				String deviceHardwareAddress = device.getAddress(); // MAC address
				if (mac.equals(deviceHardwareAddress)) {
					return device;
				}
			}
		}
		return null;
	}

	@Override
	public final void run() {
		if (mSocket == null) {
			return;
		}

		// Cancel discovery because it otherwise slows down the connection.
		// BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

		try {
			mSocket.connect();
		}
		catch (IOException connectException) {
			Log.e(ConnectThread.TAG, "Unable to connect: " + connectException.getMessage());
			cancel();
			mHandler.sendReconnect();
			return;
		}

		mConnectedThread = new ConnectedThread(mContext, mHandler, mSocket);
		mConnectedThread.start();
	}

	/**
	 * Write a message.
	 *
	 * @param message The message.
	 */
	public void write(final String message) {
		if (mConnectedThread == null) {
			Toast.makeText(mContext, "Failed to send message - no connection available", Toast.LENGTH_SHORT).show();
			Log.e(ConnectThread.TAG, "Failed to send message - no connection available");
		}
		else {
			mConnectedThread.write(message);
		}
	}

	/**
	 * Cause the client socket and close the thread.
	 */
	public void cancel() {
		try {
			mSocket.close();
		}
		catch (IOException e) {
			Log.e(ConnectThread.TAG, "Could not close the client socket");
		}
	}
}
