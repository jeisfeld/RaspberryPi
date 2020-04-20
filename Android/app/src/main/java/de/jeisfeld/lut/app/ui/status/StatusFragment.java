package de.jeisfeld.lut.app.ui.status;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import de.jeisfeld.lut.app.R;

public class StatusFragment extends Fragment {
	/**
	 * The view model.
	 */
	private StatusViewModel mStatusViewModel;

	@Override
	public final View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		mStatusViewModel = new ViewModelProvider(requireActivity()).get(StatusViewModel.class);
		View root = inflater.inflate(R.layout.fragment_status, container, false);

		final TextView textStatusMessage = root.findViewById(R.id.textStatusMessage);
		mStatusViewModel.getStatusMessage().observe(getViewLifecycleOwner(), textStatusMessage::setText);
		final TextView textProcessingMode = root.findViewById(R.id.textProcessingMode);
		mStatusViewModel.getProcessingMode().observe(getViewLifecycleOwner(), textProcessingMode::setText);

		final Switch switchBluetoothStatus = root.findViewById(R.id.switchBluetoothStatus);
		mStatusViewModel.getStatusBluetooth().observe(getViewLifecycleOwner(), switchBluetoothStatus::setChecked);
		final Switch switchButton1 = root.findViewById(R.id.switchButton1);
		mStatusViewModel.getStatusButton1().observe(getViewLifecycleOwner(), switchButton1::setChecked);
		final Switch switchButton2 = root.findViewById(R.id.switchButton2);
		mStatusViewModel.getStatusButton2().observe(getViewLifecycleOwner(), switchButton2::setChecked);

		final SeekBar seekbarControl1 = root.findViewById(R.id.seekBarControl1);
		mStatusViewModel.getStatusControl1().observe(getViewLifecycleOwner(), seekbarControl1::setProgress);
		final SeekBar seekbarControl2 = root.findViewById(R.id.seekBarControl2);
		mStatusViewModel.getStatusControl2().observe(getViewLifecycleOwner(), seekbarControl2::setProgress);
		final SeekBar seekbarControl3 = root.findViewById(R.id.seekBarControl3);
		mStatusViewModel.getStatusControl3().observe(getViewLifecycleOwner(), seekbarControl3::setProgress);

		return root;
	}
}