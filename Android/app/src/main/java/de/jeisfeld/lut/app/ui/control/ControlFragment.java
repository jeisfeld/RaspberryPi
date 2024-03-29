package de.jeisfeld.lut.app.ui.control;

import android.annotation.SuppressLint;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;
import de.jeisfeld.lut.app.MainActivity;
import de.jeisfeld.lut.app.R;
import de.jeisfeld.lut.bluetooth.message.Mode;

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
	protected abstract ControlViewModel getControlViewModel();

	/**
	 * Get the values in mode spinner dropdown.
	 *
	 * @return The values in mode spinner dropdown.
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
	 * @param viewModel The view model.
	 * @return The listener.
	 */
	protected abstract OnItemSelectedListener getOnModeSelectedListener(View parentView, ControlViewModel viewModel);

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public final View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		mControlViewModel = getControlViewModel();
		mControlViewModel.setActivity((MainActivity) requireActivity());
		View root = inflater.inflate(R.layout.fragment_control, container, false);

		final SwitchCompat switchBluetoothStatus = root.findViewById(R.id.switchBluetoothStatus);
		final LinearLayout layoutControlInfo = root.findViewById(R.id.layoutControlInfo);
		mControlViewModel.getStatusBluetooth().observe(getViewLifecycleOwner(), checked -> {
			switchBluetoothStatus.setChecked(checked);
			layoutControlInfo.setVisibility(checked ? View.VISIBLE : View.GONE);
		});

		final SwitchCompat switchActive = root.findViewById(R.id.switchActive);
		mControlViewModel.getIsActive().observe(getViewLifecycleOwner(), switchActive::setChecked);

		final Spinner spinnerMode = root.findViewById(R.id.spinnerMode);
		spinnerMode.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.spinner_item_mode, getModeSpinnerValues()));
		mControlViewModel.getMode().observe(getViewLifecycleOwner(), mode -> spinnerMode.setSelection(modeToInt(mode)));

		final Spinner spinnerWave = root.findViewById(R.id.spinnerWave);
		spinnerWave.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.spinner_item_standard,
				getResources().getStringArray(R.array.values_wave_tadel)));
		mControlViewModel.getWave().observe(getViewLifecycleOwner(), wave -> spinnerWave.setSelection(wave.ordinal()));

		final Spinner spinnerPulseTrigger = root.findViewById(R.id.spinnerPulseTrigger);
		spinnerPulseTrigger.setAdapter(new ArrayAdapter<>(requireContext(), R.layout.spinner_item_standard,
				getResources().getStringArray(R.array.values_pulse_trigger)));
		mControlViewModel.getPulseTrigger().observe(getViewLifecycleOwner(),
				pulseTrigger -> spinnerPulseTrigger.setSelection(pulseTrigger.ordinal()));

		switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> mControlViewModel.updateActiveStatus(isChecked));
		switchActive.setOnClickListener(v -> mControlViewModel.updateMode(Mode.OFF));

		spinnerMode.setOnItemSelectedListener(getOnModeSelectedListener(root, mControlViewModel));
		spinnerWave.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
				mControlViewModel.updateWave(Wave.fromOrdinal(position));
			}

			@Override
			public void onNothingSelected(final AdapterView<?> parent) {
				// do nothing
			}
		});
		spinnerPulseTrigger.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
				PulseTrigger pulseTrigger = PulseTrigger.fromOrdinal(position);
				mControlViewModel.updatePulseTrigger(pulseTrigger);
				if (mControlViewModel.getMode().getValue() == Mode.PULSE) {
					root.findViewById(R.id.tableRowPulseDuration).setVisibility(pulseTrigger.isWithDuration() ? View.VISIBLE : View.GONE);
					root.findViewById(R.id.tableRowSensorSensitivity).setVisibility(pulseTrigger.isWithSensitivity() ? View.VISIBLE : View.GONE);
					root.findViewById(R.id.tableRowPulseInvert).setVisibility(pulseTrigger.isWithSensitivity() ? View.VISIBLE : View.GONE);
				}
				mControlViewModel.stopSensorListeners();
				if (pulseTrigger == PulseTrigger.ACCELERATION) {
					mControlViewModel.startAccelerationListener(getActivity());
				}
				else if (pulseTrigger == PulseTrigger.MICROPHONE || pulseTrigger == PulseTrigger.BREATH_TRAINING_MICROPHONE) {
					mControlViewModel.startMicrophoneListener(getActivity(), pulseTrigger);
				}
			}

			@Override
			public void onNothingSelected(final AdapterView<?> parent) {
				// do nothing
			}
		});

		prepareSeekbarPower(root);
		prepareSeekbarMinPower(root);
		prepareSeekbarPowerChangeDuration(root);
		prepareSeekbarCycleLength(root);
		prepareSeekbarFrequency(root);
		prepareSeekbarRunningProbability(root);
		prepareSeekbarAvgOffDuration(root);
		prepareSeekbarAvgOnDuration(root);
		prepareSeekbarPulseDuration(root);
		prepareSeekbarSensorSensitivity(root);
		prepareCheckboxPulseInvert(root);
		prepareIconPowerStatus(root);
		prepareEndTimeButton(root);

		switchBluetoothStatus.setOnTouchListener((v, event) -> true);

		return root;
	}

	/**
	 * Prepare the power seekbar.
	 *
	 * @param root The parent view.
	 */
	private void prepareSeekbarPower(final View root) {
		final SeekBar seekbarPower = root.findViewById(R.id.seekBarPower);
		final TextView textViewPower = root.findViewById(R.id.textViewPower);

		textViewPower.setText(String.format(Locale.getDefault(), "%d", seekbarPower.getProgress()));

		mControlViewModel.getPower().observe(getViewLifecycleOwner(), power -> {
			seekbarPower.setProgress(power);
			textViewPower.setText(String.format(Locale.getDefault(), "%d", power));
		});
		seekbarPower.setOnSeekBarChangeListener(
				(OnSeekBarProgressChangedListener) progress -> mControlViewModel.updatePower(progress));
	}

	/**
	 * Prepare the min power seekbar.
	 *
	 * @param root The parent view.
	 */
	private void prepareSeekbarMinPower(final View root) {
		final SeekBar seekbarMinPower = root.findViewById(R.id.seekBarMinPower);
		final TextView textViewMinPower = root.findViewById(R.id.textViewMinPower);

		textViewMinPower.setText(String.format(Locale.getDefault(), "%d%%", seekbarMinPower.getProgress()));

		mControlViewModel.getMinPower().observe(getViewLifecycleOwner(), minPower -> {
			int percentage = (int) Math.round(minPower * 100); // MAGIC_NUMBER
			seekbarMinPower.setProgress(percentage);
			textViewMinPower.setText(String.format(Locale.getDefault(), "%d%%", percentage));
		});
		seekbarMinPower.setOnSeekBarChangeListener(
				(OnSeekBarProgressChangedListener) progress -> mControlViewModel.updateMinPower((double) progress / seekbarMinPower.getMax()));
	}

	/**
	 * Prepare the power change duration seekbar.
	 *
	 * @param root The parent view.
	 */
	private void prepareSeekbarPowerChangeDuration(final View root) {
		final SeekBar seekbarPowerChangeDuration = root.findViewById(R.id.seekBarPowerChangeDuration);
		final TextView textViewPowerChangeDuration = root.findViewById(R.id.textViewPowerChangeDuration);
		textViewPowerChangeDuration.setText(String.format(Locale.getDefault(), "%.1fs", 0.0));
		mControlViewModel.getPowerChangeDuration().observe(getViewLifecycleOwner(), powerChangeDuration -> {
			int seekbarValue = ControlViewModel.getPowerChangeDurationSeekbarValue(powerChangeDuration);
			seekbarPowerChangeDuration.setProgress(seekbarValue);
			if (powerChangeDuration >= 99900) { // MAGIC_NUMBER
				textViewPowerChangeDuration.setText(String.format(Locale.getDefault(), "%.2fm", powerChangeDuration / 60000.0)); // MAGIC_NUMBER
			}
			else {
				textViewPowerChangeDuration.setText(String.format(Locale.getDefault(), "%.1fs", powerChangeDuration / 1000.0)); // MAGIC_NUMBER
			}
		});
		seekbarPowerChangeDuration
				.setOnSeekBarChangeListener((OnSeekBarProgressChangedListener) progress -> mControlViewModel.updatePowerChangeDuration(progress));
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
		// Put values from 2-255 followed by 0, as 0 is highest frequency and 1 is unreliable when switching power quickly
		textViewFrequency.setText(String.format(Locale.getDefault(), "%d", 0));
		mControlViewModel.getFrequency().observe(getViewLifecycleOwner(), frequency -> {
			seekbarFrequency.setProgress(ControlViewModel.getFrequencySeekbarValue(frequency));
			textViewFrequency.setText(String.format(Locale.getDefault(), "%d", frequency));
		});
		seekbarFrequency.setOnSeekBarChangeListener((OnSeekBarProgressChangedListener) progress -> mControlViewModel.updateFrequency(progress));
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
				.setOnSeekBarChangeListener(
						(OnSeekBarProgressChangedListener) progress -> mControlViewModel.updateRunningProbability(progress / 100.0)); // MAGIC_NUMBER
	}

	/**
	 * Prepare the avg off duration seekbar.
	 *
	 * @param root The parent view.
	 */
	private void prepareSeekbarAvgOffDuration(final View root) {
		final SeekBar seekbarAvgOffDuration = root.findViewById(R.id.seekBarAvgOffDuration);
		final TextView textViewAvgOffDuration = root.findViewById(R.id.textViewAvgOffDuration);
		long avgOffDuration1 = ControlViewModel.avgDurationSeekbarToValue(seekbarAvgOffDuration.getProgress(), true);
		if (avgOffDuration1 >= 6000000) { // MAGIC_NUMBER
			textViewAvgOffDuration.setText(String.format(Locale.getDefault(), "%.2fh", avgOffDuration1 / 3600000.0)); // MAGIC_NUMBER
		}
		else if (avgOffDuration1 >= 600000) { // MAGIC_NUMBER
			textViewAvgOffDuration.setText(String.format(Locale.getDefault(), "%.1fm", avgOffDuration1 / 60000.0)); // MAGIC_NUMBER
		}
		else if (avgOffDuration1 >= 99900) { // MAGIC_NUMBER
			textViewAvgOffDuration.setText(String.format(Locale.getDefault(), "%.2fm", avgOffDuration1 / 60000.0)); // MAGIC_NUMBER
		}
		else {
			textViewAvgOffDuration.setText(String.format(Locale.getDefault(), "%.1fs", avgOffDuration1 / 1000.0)); // MAGIC_NUMBER
		}
		mControlViewModel.getAvgOffDuration().observe(getViewLifecycleOwner(), avgOffDuration -> {
			int seekbarValue = ControlViewModel.avgDurationValueToSeekbar(avgOffDuration, true);
			seekbarAvgOffDuration.setProgress(seekbarValue);
			if (avgOffDuration >= 6000000) { // MAGIC_NUMBER
				textViewAvgOffDuration.setText(String.format(Locale.getDefault(), "%.2fh", avgOffDuration / 3600000.0)); // MAGIC_NUMBER
			}
			else if (avgOffDuration >= 600000) { // MAGIC_NUMBER
				textViewAvgOffDuration.setText(String.format(Locale.getDefault(), "%.1fm", avgOffDuration / 60000.0)); // MAGIC_NUMBER
			}
			else if (avgOffDuration >= 99900) { // MAGIC_NUMBER
				textViewAvgOffDuration.setText(String.format(Locale.getDefault(), "%.2fm", avgOffDuration / 60000.0)); // MAGIC_NUMBER
			}
			else {
				textViewAvgOffDuration.setText(String.format(Locale.getDefault(), "%.1fs", avgOffDuration / 1000.0)); // MAGIC_NUMBER
			}
		});
		seekbarAvgOffDuration.setOnSeekBarChangeListener(
				(OnSeekBarProgressChangedListener) progress -> {
					long avgOffDuration = ControlViewModel.avgDurationSeekbarToValue(progress, true);
					Long oldPowerChangeDuration = mControlViewModel.getPowerChangeDuration().getValue();
					if (oldPowerChangeDuration != null && oldPowerChangeDuration > 0 && avgOffDuration > oldPowerChangeDuration) {
						mControlViewModel.getPowerChangeDuration().setValue(avgOffDuration);
					}
					mControlViewModel.updateAvgOffDuration(avgOffDuration);
				});
	}

	/**
	 * Prepare the avg on duration seekbar.
	 *
	 * @param root The parent view.
	 */
	private void prepareSeekbarAvgOnDuration(final View root) {
		final SeekBar seekbarAvgOnDuration = root.findViewById(R.id.seekBarAvgOnDuration);
		final TextView textViewAvgOnDuration = root.findViewById(R.id.textViewAvgOnDuration);
		long avgOnDuration1 = ControlViewModel.avgDurationSeekbarToValue(seekbarAvgOnDuration.getProgress(), false);
		if (avgOnDuration1 >= 99900) { // MAGIC_NUMBER
			textViewAvgOnDuration.setText(String.format(Locale.getDefault(), "%.2fm", avgOnDuration1 / 60000.0)); // MAGIC_NUMBER
		}
		else {
			textViewAvgOnDuration.setText(String.format(Locale.getDefault(), "%.1fs", avgOnDuration1 / 1000.0)); // MAGIC_NUMBER
		}
		mControlViewModel.getAvgOnDuration().observe(getViewLifecycleOwner(), avgOnDuration -> {
			int seekbarValue = ControlViewModel.avgDurationValueToSeekbar(avgOnDuration, false);
			seekbarAvgOnDuration.setProgress(seekbarValue);
			if (avgOnDuration >= 99900) { // MAGIC_NUMBER
				textViewAvgOnDuration.setText(String.format(Locale.getDefault(), "%.2fm", avgOnDuration / 60000.0)); // MAGIC_NUMBER
			}
			else {
				textViewAvgOnDuration.setText(String.format(Locale.getDefault(), "%.1fs", avgOnDuration / 1000.0)); // MAGIC_NUMBER
			}
		});
		seekbarAvgOnDuration
				.setOnSeekBarChangeListener((OnSeekBarProgressChangedListener) progress -> mControlViewModel
						.updateAvgOnDuration(ControlViewModel.avgDurationSeekbarToValue(progress, false)));
	}

	/**
	 * Prepare the pulse duration seekbar.
	 *
	 * @param root The parent view.
	 */
	private void prepareSeekbarPulseDuration(final View root) {
		final SeekBar seekbarPulseDuration = root.findViewById(R.id.seekBarPulseDuration);
		final TextView textViewPulseDuration = root.findViewById(R.id.textViewPulseDuration);
		long pulseDuration1 = ControlViewModel.avgDurationSeekbarToValue(seekbarPulseDuration.getProgress(), false);
		if (pulseDuration1 >= 99900) { // MAGIC_NUMBER
			textViewPulseDuration.setText(String.format(Locale.getDefault(), "%.2fm", pulseDuration1 / 60000.0)); // MAGIC_NUMBER
		}
		else {
			textViewPulseDuration.setText(String.format(Locale.getDefault(), "%.1fs", pulseDuration1 / 1000.0)); // MAGIC_NUMBER
		}
		mControlViewModel.getPulseDuration().observe(getViewLifecycleOwner(), pulseDuration -> {
			int seekbarValue = ControlViewModel.avgDurationValueToSeekbar(pulseDuration, false);
			seekbarPulseDuration.setProgress(seekbarValue);
			if (pulseDuration >= 99900) { // MAGIC_NUMBER
				textViewPulseDuration.setText(String.format(Locale.getDefault(), "%.2fm", pulseDuration / 60000.0)); // MAGIC_NUMBER
			}
			else {
				textViewPulseDuration.setText(String.format(Locale.getDefault(), "%.1fs", pulseDuration / 1000.0)); // MAGIC_NUMBER
			}
		});
		seekbarPulseDuration
				.setOnSeekBarChangeListener((OnSeekBarProgressChangedListener) progress -> mControlViewModel
						.updatePulseDuration(ControlViewModel.avgDurationSeekbarToValue(progress, false))); // MAGIC_NUMBER
	}

	/**
	 * Prepare the sensor sensitivity seekbar.
	 *
	 * @param root The parent view.
	 */
	private void prepareSeekbarSensorSensitivity(final View root) {
		final SeekBar seekbarSensorSensitivity = root.findViewById(R.id.seekBarSensorSensitivity);
		final TextView textViewSensorSensitivity = root.findViewById(R.id.textViewSensorSensitivity);
		double sensorSensitivity1 = seekbarSensorSensitivity.getProgress() * seekbarSensorSensitivity.getProgress() / 1000.0; // MAGIC_NUMBER
		textViewSensorSensitivity.setText(String.format(Locale.getDefault(), "%.2f", sensorSensitivity1));
		mControlViewModel.getSensorSensitivity().observe(getViewLifecycleOwner(), sensorSensitivity -> {
			int seekbarValue = (int) Math.round(Math.sqrt(sensorSensitivity * 1000)); // MAGIC_NUMBER
			seekbarSensorSensitivity.setProgress(seekbarValue);
			textViewSensorSensitivity.setText(String.format(Locale.getDefault(), "%.2f", sensorSensitivity));
		});
		seekbarSensorSensitivity.setOnSeekBarChangeListener(
				(OnSeekBarProgressChangedListener) progress -> mControlViewModel.updateSensorSensitivity(
						progress * progress / 1000.0)); // MAGIC_NUMBER
	}

	/**
	 * Prepare the sensor pulse invert checkbox.
	 *
	 * @param root The parent view.
	 */
	private void prepareCheckboxPulseInvert(final View root) {
		final CheckBox checkboxPulseInvert = root.findViewById(R.id.checkBoxPulseInvert);
		mControlViewModel.getPulseInvert().observe(getViewLifecycleOwner(), checkboxPulseInvert::setChecked);
		checkboxPulseInvert.setOnCheckedChangeListener((buttonView, isChecked) -> mControlViewModel.updatePulseInvert(isChecked));
	}

	/**
	 * Prepare the icon for power status.
	 *
	 * @param root The parent view.
	 */
	private void prepareIconPowerStatus(final View root) {
		final ImageView iconPowerStatus = root.findViewById(R.id.iconPowerStatus);
		mControlViewModel.getPowerStatus().observe(getViewLifecycleOwner(), isPowered ->
				iconPowerStatus.setImageResource(isPowered ? R.drawable.ic_icon_power_on : R.drawable.ic_icon_power_off));
	}

	/**
	 * Prepare the setup of the end time.
	 *
	 * @param root The parent view.
	 */
	private void prepareEndTimeButton(final View root) {
		final TextView textViewEndTime = root.findViewById(R.id.textViewEndTime);

		int hour = mControlViewModel.getEndTimeCalendar().get(Calendar.HOUR_OF_DAY);
		int minute = mControlViewModel.getEndTimeCalendar().get(Calendar.MINUTE);

		if (mControlViewModel.getEndTime().getValue() != null) {
			textViewEndTime.setText(String.format(Locale.getDefault(), "%02d:%02d", hour, minute));
		}

		mControlViewModel.getEndTime().observe(getViewLifecycleOwner(), endTime -> {
			if (mControlViewModel.getEndTime().getValue() == null) {
				textViewEndTime.setText(R.string.text_end_time);
			}
			else {
				textViewEndTime.setText(String.format(Locale.getDefault(), "%02d:%02d",
						ControlViewModel.getCalendarForDate(endTime).get(Calendar.HOUR_OF_DAY),
						ControlViewModel.getCalendarForDate(endTime).get(Calendar.MINUTE)));
			}
		});

		textViewEndTime.setOnClickListener(v -> {
			TimePickerDialog mTimePicker = new TimePickerDialog(getContext(),
					(timePicker, selectedHour, selectedMinute) -> {
						textViewEndTime.setText(String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute));
						mControlViewModel.updateEndTime(selectedHour, selectedMinute);
					},
					hour, minute, true);
			mTimePicker.show();
		});
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
