package de.jeisfeld.lut.app.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import de.jeisfeld.lut.app.MainActivity;
import de.jeisfeld.lut.app.R;
import de.jeisfeld.lut.app.util.DialogUtil;
import de.jeisfeld.lut.bluetooth.message.ShutdownMessage;

/**
 * Fragment for settings.
 */
public class SettingsFragment extends PreferenceFragmentCompat {
	@Override
	public final void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
		setPreferencesFromResource(R.xml.preferences, rootKey);
		configureBatteryOptimizationButton();
		configureShutdownButton();
	}

	/**
	 * Configure the button for battery optimization.
	 */
	private void configureBatteryOptimizationButton() {
		Preference batteryOptimizationPreference = findPreference(getString(R.string.key_pref_dummy_setting_battery_optimizations));
		assert batteryOptimizationPreference != null;
		batteryOptimizationPreference.setOnPreferenceClickListener(preference -> {
			PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
			Intent intent = new Intent();
			if (pm.isIgnoringBatteryOptimizations(getContext().getPackageName())) {
				intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
			}
			else {
				intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
				intent.setData(Uri.parse("package:" + getContext().getPackageName()));
			}
			startActivity(intent);
			return true;
		});
	}

	/**
	 * Configure the shutdown button.
	 */
	private void configureShutdownButton() {
		Preference shutdownPreference = findPreference(getString(R.string.key_pref_dummy_shutdown_raspi));
		assert shutdownPreference != null;
		shutdownPreference.setOnPreferenceClickListener(preference -> {
			MainActivity mainActivity = (MainActivity) getActivity();
			assert mainActivity != null;
			DialogUtil.displayConfirmationMessage(mainActivity, dialog -> {
				mainActivity.writeBluetoothMessage(new ShutdownMessage());
				mainActivity.finish();
			}, null, R.string.button_cancel, R.string.button_shutdown, R.string.message_dialog_confirmation_shutdown);
			return true;
		});
	}
}
