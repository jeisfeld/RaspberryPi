package de.jeisfeld.lut.app.ui.control;

import java.util.Objects;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TableRow;
import de.jeisfeld.lut.app.R;
import de.jeisfeld.lut.bluetooth.message.Mode;

/**
 * The fragment for managing tadel messages.
 */
public abstract class TadelFragment extends ControlFragment {
	@Override
	protected final String[] getModeSpinnerValues() {
		return getResources().getStringArray(R.array.values_mode_tadel);
	}

	@Override
	protected final int modeToInt(final Mode mode) {
		return mode.getTadelValue();
	}

	@Override
	protected final OnItemSelectedListener getOnModeSelectedListener(final View parentView, final ControlViewModel viewModel) {
		TableRow tableRowPower = parentView.findViewById(R.id.tableRowPower);
		TableRow tableRowMinPower = parentView.findViewById(R.id.tableRowMinPower);
		TableRow tableRowPowerChangeDuration = parentView.findViewById(R.id.tableRowPowerChangeDuration);
		TableRow tableRowFrequency = parentView.findViewById(R.id.tableRowFrequency);
		TableRow tableRowWave = parentView.findViewById(R.id.tableRowWave);
		TableRow tableRowRunningProbability = parentView.findViewById(R.id.tableRowRunningProbability);
		TableRow tableRowAvgOffDuration = parentView.findViewById(R.id.tableRowAvgOffDuration);
		TableRow tableRowAvgOnDuration = parentView.findViewById(R.id.tableRowAvgOnDuration);
		TableRow tableRowPulseTrigger = parentView.findViewById(R.id.tableRowPulseTrigger);
		TableRow tableRowPulseDuration = parentView.findViewById(R.id.tableRowPulseDuration);
		TableRow tableRowSensorSensitivity = parentView.findViewById(R.id.tableRowSensorSensitivity);
		TableRow tableRowPulseInvert = parentView.findViewById(R.id.tableRowPulseInvert);

		return new OnItemSelectedListener() {
			@Override
			public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
				Mode mode = Mode.fromTadelValue(position);
				switch (mode) {
				case OFF:
					tableRowPower.setVisibility(View.GONE);
					tableRowMinPower.setVisibility(View.GONE);
					tableRowPowerChangeDuration.setVisibility(View.GONE);
					tableRowFrequency.setVisibility(View.GONE);
					tableRowWave.setVisibility(View.GONE);
					tableRowRunningProbability.setVisibility(View.GONE);
					tableRowAvgOffDuration.setVisibility(View.GONE);
					tableRowAvgOnDuration.setVisibility(View.GONE);
					tableRowPulseTrigger.setVisibility(View.GONE);
					tableRowPulseDuration.setVisibility(View.GONE);
					tableRowSensorSensitivity.setVisibility(View.GONE);
					tableRowPulseInvert.setVisibility(View.GONE);
					viewModel.updateActiveStatus(false);
					break;
				case FIXED:
					tableRowPower.setVisibility(View.VISIBLE);
					tableRowMinPower.setVisibility(View.GONE);
					tableRowPowerChangeDuration.setVisibility(View.VISIBLE);
					tableRowFrequency.setVisibility(View.VISIBLE);
					tableRowWave.setVisibility(View.VISIBLE);
					tableRowRunningProbability.setVisibility(View.GONE);
					tableRowAvgOffDuration.setVisibility(View.GONE);
					tableRowAvgOnDuration.setVisibility(View.GONE);
					tableRowPulseTrigger.setVisibility(View.GONE);
					tableRowPulseDuration.setVisibility(View.GONE);
					tableRowSensorSensitivity.setVisibility(View.GONE);
					tableRowPulseInvert.setVisibility(View.GONE);
					viewModel.updateActiveStatus(true);
					break;
				case RANDOM_1:
					tableRowPower.setVisibility(View.VISIBLE);
					tableRowMinPower.setVisibility(View.VISIBLE);
					tableRowPowerChangeDuration.setVisibility(View.VISIBLE);
					tableRowFrequency.setVisibility(View.VISIBLE);
					tableRowWave.setVisibility(View.VISIBLE);
					tableRowRunningProbability.setVisibility(View.VISIBLE);
					tableRowAvgOffDuration.setVisibility(View.GONE);
					tableRowAvgOnDuration.setVisibility(View.GONE);
					tableRowPulseTrigger.setVisibility(View.GONE);
					tableRowPulseDuration.setVisibility(View.GONE);
					tableRowSensorSensitivity.setVisibility(View.GONE);
					tableRowPulseInvert.setVisibility(View.GONE);
					viewModel.updateActiveStatus(true);
					break;
				case RANDOM_2:
					tableRowPower.setVisibility(View.VISIBLE);
					tableRowMinPower.setVisibility(View.VISIBLE);
					tableRowPowerChangeDuration.setVisibility(View.VISIBLE);
					tableRowFrequency.setVisibility(View.VISIBLE);
					tableRowWave.setVisibility(View.VISIBLE);
					tableRowRunningProbability.setVisibility(View.GONE);
					tableRowAvgOffDuration.setVisibility(View.VISIBLE);
					tableRowAvgOnDuration.setVisibility(View.VISIBLE);
					tableRowPulseTrigger.setVisibility(View.GONE);
					tableRowPulseDuration.setVisibility(View.GONE);
					tableRowSensorSensitivity.setVisibility(View.GONE);
					tableRowPulseInvert.setVisibility(View.GONE);
					viewModel.updateActiveStatus(true);
					break;
				case PULSE:
					tableRowPower.setVisibility(View.VISIBLE);
					tableRowMinPower.setVisibility(View.VISIBLE);
					tableRowPowerChangeDuration.setVisibility(View.VISIBLE);
					tableRowFrequency.setVisibility(View.VISIBLE);
					tableRowWave.setVisibility(View.VISIBLE);
					tableRowRunningProbability.setVisibility(View.GONE);
					tableRowAvgOffDuration.setVisibility(View.GONE);
					tableRowAvgOnDuration.setVisibility(View.GONE);
					tableRowPulseTrigger.setVisibility(View.VISIBLE);
					tableRowPulseDuration.setVisibility(
							Objects.requireNonNull(viewModel.getPulseTrigger().getValue()).isWithDuration() ? View.VISIBLE : View.GONE);
					tableRowSensorSensitivity.setVisibility(
							Objects.requireNonNull(viewModel.getPulseTrigger().getValue()).isWithSensitivity() ? View.VISIBLE : View.GONE);
					tableRowPulseInvert.setVisibility(
							Objects.requireNonNull(viewModel.getPulseTrigger().getValue()).isWithSensitivity() ? View.VISIBLE : View.GONE);
					viewModel.updateActiveStatus(true);
					break;
				default:
					break;
				}
				viewModel.updateMode(mode);

				getControlViewModel().stopSensorListeners();
				if (mode == Mode.PULSE) {
					PulseTrigger pulseTrigger = getControlViewModel().getPulseTrigger().getValue();
					if (pulseTrigger == PulseTrigger.ACCELERATION) {
						getControlViewModel().startAccelerationListener(getActivity());
					}
					else if (pulseTrigger == PulseTrigger.MICROPHONE || pulseTrigger == PulseTrigger.BREATH_TRAINING_MICROPHONE) {
						getControlViewModel().startMicrophoneListener(getActivity(), pulseTrigger);
					}
				}
			}

			@Override
			public void onNothingSelected(final AdapterView<?> parent) {
				// do nothing
			}
		};
	}

}
