package de.jeisfeld.lut.app;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import de.jeisfeld.lut.app.bluetooth.BluetoothMessageHandler;
import de.jeisfeld.lut.app.bluetooth.ConnectThread;

/**
 * Main activity of the app.
 */
public class MainActivity extends AppCompatActivity {
	/**
	 * The logging tag.
	 */
	private static final String TAG = "JE.LuT.MainActivity";
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

	@Override
	protected final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		FloatingActionButton fab = findViewById(R.id.fab);
		fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
				.setAction("Action", null).show());
		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		NavigationView navigationView = findViewById(R.id.nav_view);
		// Passing each menu ID as a set of Ids because each
		// menu should be considered as top level destinations.
		mAppBarConfiguration = new AppBarConfiguration.Builder(
				R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
						.setDrawerLayout(drawer)
						.build();
		NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
		NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
		NavigationUI.setupWithNavController(navigationView, navController);

		mHandler = new BluetoothMessageHandler(this);
		connect();
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

	/**
	 * Get the connect thread.
	 *
	 * @return The connect thread.
	 */
	public ConnectThread getConnectThread() {
		return mConnectThread;
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
}
