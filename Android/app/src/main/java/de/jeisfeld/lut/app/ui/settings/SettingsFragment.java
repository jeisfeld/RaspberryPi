package de.jeisfeld.lut.app.ui.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import de.jeisfeld.lut.app.R;

/**
 * Fragment for settings.
 */
public class SettingsFragment extends PreferenceFragmentCompat {
	@Override
	public final void onCreatePreferences(final Bundle savedInstanceState, final String rootKey) {
		setPreferencesFromResource(R.xml.preferences, rootKey);
	}
}
