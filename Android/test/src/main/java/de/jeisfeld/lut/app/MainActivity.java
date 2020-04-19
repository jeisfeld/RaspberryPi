package de.jeisfeld.lut.app;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import de.jeisfeld.lut.app.bluetooth.BluetoothMessageHandler;
import de.jeisfeld.lut.app.bluetooth.ConnectThread;

public class MainActivity extends AppCompatActivity {
	/**
	 * The logging tag.
	 */
	private static final String TAG = "JE.LuT.Main";

	/**
	 * The connect thread.
	 */
	private ConnectThread mConnectThread = null;

	/**
	 * Message counter.
	 */
	private int mCounter = 1;

	/**
	 * The handler for messages sent from bluetooth threads.
	 */
	private BluetoothMessageHandler mHandler;

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button buttonTest = findViewById(R.id.buttonTest);
		mHandler = new BluetoothMessageHandler(this);

		connect();

		buttonTest.setOnClickListener(v -> {
			testBluetooth();
		});
	}

	@Override
	protected final void onDestroy() {
		super.onDestroy();
		if (mConnectThread != null) {
			mConnectThread.cancel();
		}
	}

	/**
	 * Create bluetooth connection.
	 */
	public void connect() {
		if (mConnectThread != null) {
			mConnectThread.cancel();
		}
		Log.i(MainActivity.TAG, "Starting bluetooth connection...");
		mConnectThread = new ConnectThread(this, mHandler);
		mConnectThread.start();
	}

	private void testBluetooth() {
		if (mConnectThread != null) {
			mConnectThread.write("Hello world " + mCounter++ + "\n");
		}
		else {
			Log.e(MainActivity.TAG, "ConnectedThread not existing");
		}
	}

}
