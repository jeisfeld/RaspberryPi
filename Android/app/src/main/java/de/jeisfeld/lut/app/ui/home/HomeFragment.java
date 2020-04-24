package de.jeisfeld.lut.app.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import de.jeisfeld.lut.app.MainActivity;
import de.jeisfeld.lut.app.R;
import de.jeisfeld.lut.bluetooth.message.FreeTextMessage;

/**
 * The home fragment of the app.
 */
public class HomeFragment extends Fragment {
	/**
	 * Message counter.
	 */
	private int mCounter = 1;

	@Override
	public final View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_home, container, false);
		Button buttonTest = root.findViewById(R.id.buttonTest);
		buttonTest.setOnClickListener(v -> {
			((MainActivity) requireActivity()).writeBluetoothMessage(new FreeTextMessage("Hello world " + mCounter++));
		});
		return root;
	}
}
