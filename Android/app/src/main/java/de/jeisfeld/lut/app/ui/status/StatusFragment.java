package de.jeisfeld.lut.app.ui.status;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import de.jeisfeld.lut.app.MainActivity;
import de.jeisfeld.lut.app.R;

/**
 * The fragment for displaying status.
 */
public class StatusFragment extends Fragment {
	/**
	 * The view model.
	 */
	private StatusViewModel mStatusViewModel;

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public final View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		mStatusViewModel = new ViewModelProvider(requireActivity()).get(StatusViewModel.class);
		mStatusViewModel.setActivity((MainActivity) requireActivity());
		View root = inflater.inflate(R.layout.fragment_status, container, false);

		final TextView textStatusMessage = root.findViewById(R.id.textControlStatusMessage);
		mStatusViewModel.getControlStatusMessage().observe(getViewLifecycleOwner(), textStatusMessage::setText);
		final TextView textProcessingStatus = root.findViewById(R.id.textProcessingStatus);
		mStatusViewModel.getProcessingStatus().observe(getViewLifecycleOwner(), textProcessingStatus::setText);

		final SwitchCompat switchBluetoothStatus = root.findViewById(R.id.switchBluetoothStatus);
		final LinearLayout layoutControlInfo = root.findViewById(R.id.layoutControlInfo);
		mStatusViewModel.getStatusBluetooth().observe(getViewLifecycleOwner(), checked -> {
			switchBluetoothStatus.setChecked(checked);
			layoutControlInfo.setVisibility(checked ? View.VISIBLE : View.GONE);
		});

		final SwitchCompat switchStandaloneStatus = root.findViewById(R.id.switchStandaloneStatus);
		mStatusViewModel.getStatusStandalone().observe(getViewLifecycleOwner(), switchStandaloneStatus::setChecked);
		switchStandaloneStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
			mStatusViewModel.updateStandaloneStatus(isChecked);
			textProcessingStatus.setVisibility(isChecked ? View.VISIBLE : View.GONE);
		});

		final SwitchCompat switchButton1 = root.findViewById(R.id.switchButton1);
		mStatusViewModel.getStatusButton1().observe(getViewLifecycleOwner(), switchButton1::setChecked);
		final SwitchCompat switchButton2 = root.findViewById(R.id.switchButton2);
		mStatusViewModel.getStatusButton2().observe(getViewLifecycleOwner(), switchButton2::setChecked);

		final SeekBar seekbarControl1 = root.findViewById(R.id.seekBarControl1);
		mStatusViewModel.getStatusControl1().observe(getViewLifecycleOwner(), seekbarControl1::setProgress);
		final SeekBar seekbarControl2 = root.findViewById(R.id.seekBarControl2);
		mStatusViewModel.getStatusControl2().observe(getViewLifecycleOwner(), seekbarControl2::setProgress);
		final SeekBar seekbarControl3 = root.findViewById(R.id.seekBarControl3);
		mStatusViewModel.getStatusControl3().observe(getViewLifecycleOwner(), seekbarControl3::setProgress);

		switchBluetoothStatus.setOnTouchListener((v, event) -> true);
		switchButton1.setOnTouchListener((v, event) -> true);
		switchButton2.setOnTouchListener((v, event) -> true);
		seekbarControl1.setOnTouchListener((v, event) -> true);
		seekbarControl2.setOnTouchListener((v, event) -> true);
		seekbarControl3.setOnTouchListener((v, event) -> true);

		return root;
	}
}
