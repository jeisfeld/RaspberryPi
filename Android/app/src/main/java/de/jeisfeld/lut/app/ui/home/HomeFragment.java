package de.jeisfeld.lut.app.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import de.jeisfeld.lut.app.MainActivity;
import de.jeisfeld.lut.app.R;
import de.jeisfeld.lut.app.bluetooth.ConnectThread;
import de.jeisfeld.lut.bluetooth.message.FreeTextMessage;

/**
 * The home fragment of the app.
 */
public class HomeFragment extends Fragment {
	/**
	 * The logging tag.
	 */
	private static final String TAG = "JE.LuT.HomeFragment";

	/**
	 * Message counter.
	 */
	private int mCounter = 1;

	@Override
	public final View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_home, container, false);
		Button buttonTest = root.findViewById(R.id.buttonTest);
		buttonTest.setOnClickListener(v -> {
			ConnectThread connectThread = ((MainActivity) requireActivity()).getConnectThread();
			if (connectThread != null) {
				connectThread.write(new FreeTextMessage("Hello world " + mCounter++));
			}
			else {
				Log.e(HomeFragment.TAG, "ConnectedThread not existing");
			}
		});
		return root;
	}
}
