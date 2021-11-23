package de.jeisfeld.lut.app;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.WindowManager;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import de.jeisfeld.lut.app.bluetooth.BluetoothMessageHandler;
import de.jeisfeld.lut.app.bluetooth.ConnectThread;
import de.jeisfeld.lut.app.ui.control.Lob0ViewModel;
import de.jeisfeld.lut.app.ui.control.Lob1ViewModel;
import de.jeisfeld.lut.app.ui.control.Tadel0ViewModel;
import de.jeisfeld.lut.app.ui.control.Tadel1ViewModel;
import de.jeisfeld.lut.app.ui.status.StatusViewModel;
import de.jeisfeld.lut.app.util.DialogUtil;
import de.jeisfeld.lut.app.util.ExternalTriggerReceiver;
import de.jeisfeld.lut.app.util.PreferenceUtil;
import de.jeisfeld.lut.bluetooth.message.ButtonStatusMessage;
import de.jeisfeld.lut.bluetooth.message.Message;
import de.jeisfeld.lut.bluetooth.message.ProcessingBluetoothMessage;
import de.jeisfeld.lut.bluetooth.message.ProcessingStandaloneMessage;
import de.jeisfeld.lut.bluetooth.message.StandaloneStatusMessage;

/**
 * Main activity of the app.
 */
public class MainActivity extends AppCompatActivity {
	/**
	 * Request code for enabling bluetooth.
	 */
	private static final int REQUEST_ENABLE_BLUETOOTH = 1;

	/**
	 * The logging tag.
	 */
	private static final String TAG = "LuT.JE.MainActivity";
	/**
	 * The navigation bar configuration.
	 */
	private AppBarConfiguration mAppBarConfiguration;

	/**
	 * The connect thread.
	 */
	private ConnectThread mConnectThread = null;

	/**
	 * The handler for messages sent from bluetooth threads.
	 */
	private BluetoothMessageHandler mHandler;

	/**
	 * The receiver for external messages.
	 */
	private ExternalTriggerReceiver mTriggerReceiver;

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		AppBarConfiguration.Builder builder = new AppBarConfiguration.Builder(
				R.id.nav_status, R.id.nav_lob_0, R.id.nav_tadel_0, R.id.nav_lob_1, R.id.nav_tadel_1, R.id.nav_settings);
		if (findViewById(R.id.tabletIntermediateLayout) == null) {
			builder.setOpenableLayout(drawer);
		}
		mAppBarConfiguration = builder.build();
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
		NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
		NavigationView navigationView = findViewById(R.id.nav_view);
		NavigationUI.setupWithNavController(navigationView, navController);

		mHandler = new BluetoothMessageHandler(this);

		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (bluetoothAdapter == null) {
			DialogUtil.displayToast(this, R.string.toast_bluetooth_required);
			finish();
			return;
		}
		else if (!bluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
			return;
		}

		mTriggerReceiver = new ExternalTriggerReceiver(this);
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("de.jeisfeld.randomimage.DISPLAY_RANDOM_IMAGE");
		intentFilter.addAction("de.jeisfeld.randomimage.DISPLAY_NOTIFICATION");
		intentFilter.addAction("de.jeisfeld.breathtraining.BREATH_EXERCISE");
		registerReceiver(mTriggerReceiver, intentFilter);

		connect();
	}

	@Override
	protected final void onDestroy() {
		super.onDestroy();
		if (mConnectThread != null) {
			mConnectThread.cancel();
		}
		unregisterReceiver(mTriggerReceiver);
	}

	/**
	 * Create bluetooth connection.
	 */
	public void connect() {
		if (PreferenceUtil.getSharedPreferenceBoolean(R.string.key_pref_prevent_screen_timeout, true)) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		if (mConnectThread != null) {
			mConnectThread.cancel();
		}
		Log.i(TAG, "Starting bluetooth connection...");
		mConnectThread = new ConnectThread(this, mHandler);
		mConnectThread.start();
	}

	/**
	 * Write a message via bluetooth to Pi.
	 *
	 * @param message The message.
	 */
	public void writeBluetoothMessage(final Message message) {
		ConnectThread connectThread = mConnectThread;
		if (connectThread != null) {
			connectThread.write(message);
		}
		else {
			DialogUtil.displayToast(this, R.string.toast_failed_send);
			Log.e(TAG, "ConnectedThread not existing");
		}
	}

	@Override
	public final boolean onCreateOptionsMenu(final Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public final boolean onSupportNavigateUp() {
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
		return NavigationUI.navigateUp(navController, mAppBarConfiguration)
				|| super.onSupportNavigateUp();
	}

	/**
	 * Update the button status based on message received from Pi.
	 *
	 * @param message The button status message.
	 */
	public void updateOnMessageReceived(final Message message) {
		StatusViewModel statusViewModel = new ViewModelProvider(this).get(StatusViewModel.class);
		switch (message.getType()) {
		case BUTTON_STATUS:
			statusViewModel.setStatus((ButtonStatusMessage) message);
			break;
		case PROCESSING_STANDALONE:
			statusViewModel.setProcessingStatus((ProcessingStandaloneMessage) message);
			break;
		case PROCESSING_BLUETOOTH:
			ProcessingBluetoothMessage bluetoothMessage = (ProcessingBluetoothMessage) message;
			if (bluetoothMessage.isTadel()) {
				switch (bluetoothMessage.getChannel()) {
				case 0:
					new ViewModelProvider(this).get(Tadel0ViewModel.class).setProcessingStatus(bluetoothMessage);
					break;
				case 1:
				default:
					new ViewModelProvider(this).get(Tadel1ViewModel.class).setProcessingStatus(bluetoothMessage);
					break;
				}
			}
			else {
				switch (bluetoothMessage.getChannel()) {
				case 0:
					new ViewModelProvider(this).get(Lob0ViewModel.class).setProcessingStatus(bluetoothMessage);
					break;
				case 1:
				default:
					new ViewModelProvider(this).get(Lob1ViewModel.class).setProcessingStatus(bluetoothMessage);
					break;
				}
			}
			break;
		case STANDALONE_STATUS:
			statusViewModel.setStandaloneStatus(((StandaloneStatusMessage) message).isActive());
			break;
		default:
			Log.i(TAG, "Received unexpected message: " + message);
		}
	}

	/**
	 * Update the status information if bluetooth is connected.
	 *
	 * @param connected true if connected.
	 */
	public void updateConnectedStatus(final boolean connected) {
		Log.i(TAG, "Bluetooth connection status: " + connected);
		ViewModelProvider viewModelProvider = new ViewModelProvider(this);
		viewModelProvider.get(StatusViewModel.class).setBluetoothStatus(connected);
		viewModelProvider.get(Lob0ViewModel.class).setBluetoothStatus(connected);
		viewModelProvider.get(Tadel0ViewModel.class).setBluetoothStatus(connected);
		viewModelProvider.get(Lob1ViewModel.class).setBluetoothStatus(connected);
		viewModelProvider.get(Tadel1ViewModel.class).setBluetoothStatus(connected);
	}

	@Override
	protected final void onActivityResult(final int requestCode, final int resultCode, @Nullable final Intent data) {
		if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
			if (resultCode == RESULT_OK) {
				connect();
			}
			else {
				DialogUtil.displayToast(this, R.string.toast_bluetooth_required);
				finish();
			}
		}
		else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}
}
