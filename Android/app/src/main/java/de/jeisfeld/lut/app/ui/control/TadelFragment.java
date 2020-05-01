package de.jeisfeld.lut.app.ui.control;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.TableRow;
import de.jeisfeld.lut.app.R;

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
		TableRow tableRowRunningProbability = parentView.findViewById(R.id.tableRowRunningProbability);
		TableRow tableRowAvgOffDuration = parentView.findViewById(R.id.tableRowAvgOffDuration);
		TableRow tableRowAvgOnDuration = parentView.findViewById(R.id.tableRowAvgOnDuration);

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
					tableRowRunningProbability.setVisibility(View.GONE);
					tableRowAvgOffDuration.setVisibility(View.GONE);
					tableRowAvgOnDuration.setVisibility(View.GONE);
					viewModel.updateActiveStatus(false);
					break;
				case FIXED:
					tableRowPower.setVisibility(View.VISIBLE);
					tableRowMinPower.setVisibility(View.GONE);
					tableRowPowerChangeDuration.setVisibility(View.VISIBLE);
					tableRowFrequency.setVisibility(View.VISIBLE);
					tableRowRunningProbability.setVisibility(View.GONE);
					tableRowAvgOffDuration.setVisibility(View.GONE);
					tableRowAvgOnDuration.setVisibility(View.GONE);
					viewModel.updateActiveStatus(true);
					break;
				case RANDOM_1:
					tableRowPower.setVisibility(View.VISIBLE);
					tableRowMinPower.setVisibility(View.VISIBLE);
					tableRowPowerChangeDuration.setVisibility(View.VISIBLE);
					tableRowFrequency.setVisibility(View.GONE);
					tableRowRunningProbability.setVisibility(View.VISIBLE);
					tableRowAvgOffDuration.setVisibility(View.GONE);
					tableRowAvgOnDuration.setVisibility(View.GONE);
					viewModel.updateActiveStatus(true);
					break;
				case RANDOM_2:
					tableRowPower.setVisibility(View.VISIBLE);
					tableRowMinPower.setVisibility(View.VISIBLE);
					tableRowPowerChangeDuration.setVisibility(View.VISIBLE);
					tableRowFrequency.setVisibility(View.GONE);
					tableRowRunningProbability.setVisibility(View.GONE);
					tableRowAvgOffDuration.setVisibility(View.VISIBLE);
					tableRowAvgOnDuration.setVisibility(View.VISIBLE);
					viewModel.updateActiveStatus(true);
					break;
				default:
					break;
				}
				viewModel.updateMode(mode);
			}

			@Override
			public void onNothingSelected(final AdapterView<?> parent) {
				// do nothing
			}
		};
	}

}
