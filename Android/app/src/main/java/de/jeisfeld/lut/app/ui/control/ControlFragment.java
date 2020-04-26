package de.jeisfeld.lut.app.ui.control;

import java.util.Locale;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import de.jeisfeld.lut.app.MainActivity;
import de.jeisfeld.lut.app.R;

/**
 * The fragment for managing Lob or Tadel messages.
 */
public abstract class ControlFragment extends Fragment {
	/**
	 * The view model.
	 */
	private ControlViewModel mControlViewModel;

	/**
	 * Get the view model.
	 *
	 * @return The view model.
	 */
	protected abstract ControlViewModel getLobViewModel();

	/**
	 * Get the values in spinner dropdown.
	 *
	 * @return The values in spinner dropdown.
	 */
	protected abstract String[] getModeSpinnerValues();

	/**
	 * Get the integer value from a mode.
	 *
	 * @param mode The mode.
	 * @return The integer value of the mode.
	 */
	protected abstract int modeToInt(Mode mode);

	/**
	 * Get the listener on mode change.
	 *
	 * @param parentView The parent view.
	 * @param viewModel  The view model.
	 * @return The listener.
	 */
	protected abstract OnItemSelectedListener getOnModeSelectedListener(View parentView, ControlViewModel viewModel);

	@Override
	public final View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		mControlViewModel = getLobViewModel();
		mControlViewModel.setActivity((MainActivity) requireActivity());
		View root = inflater.inflate(R.layout.fragment_control, container, false);

		final Switch switchBluetoothStatus = root.findViewById(R.id.switchBluetoothStatus);
		final LinearLayout layoutControlInfo = root.findViewById(R.id.layoutControlInfo);
		mControlViewModel.getStatusBluetooth().observe(getViewLifecycleOwner(), checked -> {
			switchBluetoothStatus.setChecked(checked);
			layoutControlInfo.setVisibility(checked ? View.VISIBLE : View.GONE);
		});

		final Switch switchActive = root.findViewById(R.id.switchActive);
		mControlViewModel.getIsActive().observe(getViewLifecycleOwner(), switchActive::setChecked);

		final Spinner spinnerMode = root.findViewById(R.id.spinnerMode);
		spinnerMode.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.spinner_item, getModeSpinnerValues()));
		mControlViewModel.getMode().observe(getViewLifecycleOwner(), mode -> spinnerMode.setSelection(modeToInt(mode)));

		switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> mControlViewModel.updateActiveStatus(isChecked));
		switchActive.setOnClickListener(v -> mControlViewModel.updateMode(Mode.OFF));

		spinnerMode.setOnItemSelectedListener(getOnModeSelectedListener(root, mControlViewModel));

		prepareSeekbarsPower(root);
		prepareSeekbarCycleLength(root);
		prepareSeekbarFrequency(root);
		prepareSeekbarRunningProbability(root);
		prepareSeekbarAvgOffDuration(root);
		prepareSeekbarAvgOnDuration(root);

		return root;
	}

	/**
	 * Prepare the power seekbar.
	 *
	 * @param root The parent view.
	 */
	private void prepareSeekbarsPower(final View root) {
		final SeekBar seekbarPower = root.findViewById(R.id.seekBarPower);
		final TextView textViewPower = root.findViewById(R.id.textViewPower);

		final SeekBar seekbarMinPower = root.findViewById(R.id.seekBarMinPower);
		final TextView textViewMinPower = root.findViewById(R.id.textViewMinPower);

		textViewPower.setText(String.format(Locale.getDefault(), "%d", seekbarPower.getProgress()));
		textViewMinPower.setText(String.format(Locale.getDefault(), "%d", seekbarMinPower.getProgress()));

		mControlViewModel.getPower().observe(getViewLifecycleOwner(), power -> {
			seekbarPower.setProgress(power);
			textViewPower.setText(String.format(Locale.getDefault(), "%d", power));
		});
		mControlViewModel.getMinPower().observe(getViewLifecycleOwner(), minPower -> {
			seekbarMinPower.setProgress(mControlViewModel.getMinPowerSeekbarValue(minPower));
			textViewMinPower.setText(String.format(Locale.getDefault(), "%d", minPower));
		});

		seekbarPower.setOnSeekBarChangeListener(
				(OnSeekBarProgressChangedListener) progress -> mControlViewModel.updatePower(progress, seekbarMinPower.getProgress()));
		seekbarMinPower.setOnSeekBarChangeListener(
				(OnSeekBarProgressChangedListener) progress -> mControlViewModel.updatePower(seekbarPower.getProgress(), progress));
	}

	/**
	 * Prepare the cycle length seekbar.
	 *
	 * @param root The parent view.
	 */
	private void prepareSeekbarCycleLength(final View root) {
		final SeekBar seekbarCycleLength = root.findViewById(R.id.seekBarCycleLength);
		final TextView textViewCycleLength = root.findViewById(R.id.textViewCycleLength);
		textViewCycleLength.setText(String.format(Locale.getDefault(), "%d", seekbarCycleLength.getProgress()));
		mControlViewModel.getCycleLength().observe(getViewLifecycleOwner(), cycleLength -> {
			seekbarCycleLength.setProgress(cycleLength);
			textViewCycleLength.setText(String.format(Locale.getDefault(), "%d", cycleLength));
		});
		seekbarCycleLength.setOnSeekBarChangeListener((OnSeekBarProgressChangedListener) progress -> mControlViewModel.updateCycleLength(progress));
	}

	/**
	 * Prepare the frequency seekbar.
	 *
	 * @param root The parent view.
	 */
	private void prepareSeekbarFrequency(final View root) {
		final SeekBar seekbarFrequency = root.findViewById(R.id.seekBarFrequency);
		final TextView textViewFrequency = root.findViewById(R.id.textViewFrequency);
		textViewFrequency.setText(String.format(Locale.getDefault(), "%d", seekbarFrequency.getProgress() + 1));
		mControlViewModel.getFrequency().observe(getViewLifecycleOwner(), frequency -> {
			seekbarFrequency.setProgress((frequency + 255) % 256); // MAGIC_NUMBER
			textViewFrequency.setText(String.format(Locale.getDefault(), "%d", frequency));
		});
		seekbarFrequency.setOnSeekBarChangeListener((OnSeekBarProgressChangedListener) progress ->
				mControlViewModel.updateFrequency((progress + 1) % 256)); // MAGIC_NUMBER
	}

	/**
	 * Prepare the cycle length seekbar.
	 *
	 * @param root The parent view.
	 */
	private void prepareSeekbarRunningProbability(final View root) {
		final SeekBar seekbarRunningProbability = root.findViewById(R.id.seekBarRunningProbability);
		final TextView textViewRunningProbability = root.findViewById(R.id.textViewRunningProbability);
		textViewRunningProbability.setText(String.format(Locale.getDefault(), "%d%%", seekbarRunningProbability.getProgress()));
		mControlViewModel.getRunningProbability().observe(getViewLifecycleOwner(), runningProbability -> {
			int seekbarValue = (int) Math.round(runningProbability * 100); // MAGIC_NUMBER
			seekbarRunningProbability.setProgress(seekbarValue);
			textViewRunningProbability.setText(String.format(Locale.getDefault(), "%d%%", seekbarValue));
		});
		seekbarRunningProbability
				.setOnSeekBarChangeListener((OnSeekBarProgressChangedListener) progress -> //
						mControlViewModel.updateRunningProbability(progress / 100.0)); // MAGIC_NUMBER
	}

	/**
	 * Prepare the avg off duration seekbar.
	 *
	 * @param root The parent view.
	 */
	private void prepareSeekbarAvgOffDuration(final View root) {
		final SeekBar seekbarAvgOffDuration = root.findViewById(R.id.seekBarAvgOffDuration);
		final TextView textViewAvgOffDuration = root.findViewById(R.id.textViewAvgOffDuration);
		textViewAvgOffDuration.setText(String.format(Locale.getDefault(), "%.1fs",
				ControlViewModel.avgDurationSeekbarToValue(seekbarAvgOffDuration.getProgress()) / 1000.0)); // MAGIC_NUMBER
		mControlViewModel.getAvgOffDuration().observe(getViewLifecycleOwner(), avgOffDuration -> {
			int seekbarValue = ControlViewModel.avgDurationValueToSeekbar(avgOffDuration);
			seekbarAvgOffDuration.setProgress(seekbarValue);
			textViewAvgOffDuration.setText(String.format(Locale.getDefault(), "%.1fs", avgOffDuration / 1000.0)); // MAGIC_NUMBER
		});
		seekbarAvgOffDuration
				.setOnSeekBarChangeListener((OnSeekBarProgressChangedListener) progress -> //
						mControlViewModel.updateAvgOffDuration(ControlViewModel.avgDurationSeekbarToValue(progress))); // MAGIC_NUMBER
	}

	/**
	 * Prepare the avg on duration seekbar.
	 *
	 * @param root The parent view.
	 */
	private void prepareSeekbarAvgOnDuration(final View root) {
		final SeekBar seekbarAvgOnDuration = root.findViewById(R.id.seekBarAvgOnDuration);
		final TextView textViewAvgOnDuration = root.findViewById(R.id.textViewAvgOnDuration);
		textViewAvgOnDuration.setText(String.format(Locale.getDefault(), "%.1fs",
				ControlViewModel.avgDurationSeekbarToValue(seekbarAvgOnDuration.getProgress()) / 1000.0)); // MAGIC_NUMBER
		mControlViewModel.getAvgOnDuration().observe(getViewLifecycleOwner(), avgOnDuration -> {
			int seekbarValue = ControlViewModel.avgDurationValueToSeekbar(avgOnDuration);
			seekbarAvgOnDuration.setProgress(seekbarValue);
			textViewAvgOnDuration.setText(String.format(Locale.getDefault(), "%.1fs", avgOnDuration / 1000.0)); // MAGIC_NUMBER
		});
		seekbarAvgOnDuration
				.setOnSeekBarChangeListener((OnSeekBarProgressChangedListener) progress -> //
						mControlViewModel.updateAvgOnDuration(ControlViewModel.avgDurationSeekbarToValue(progress))); // MAGIC_NUMBER
	}

	/**
	 * Variant of OnSeekBarChangeListener that reacts only on progress change from user.
	 */
	private interface OnSeekBarProgressChangedListener extends OnSeekBarChangeListener {
		/**
		 * Callback on progress change from user.
		 *
		 * @param progress The progress.
		 */
		void onProgessChanged(int progress);

		@Override
		default void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
			if (fromUser) {
				onProgessChanged(progress);
			}
		}

		@Override
		default void onStartTrackingTouch(final SeekBar seekBar) {
			// do nothing
		}

		@Override
		default void onStopTrackingTouch(final SeekBar seekBar) {
			// do nothing
		}
	}

}
