package de.jeisfeld.lut.app.ui.lob;

import java.util.Locale;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import de.jeisfeld.lut.app.MainActivity;
import de.jeisfeld.lut.app.R;

/**
 * The fragment for managing Lob messages.
 */
public abstract class LobFragment extends Fragment {
	/**
	 * The view model.
	 */
	private LobViewModel mLobViewModel;

	/**
	 * Get the view model.
	 *
	 * @return The view model.
	 */
	protected abstract LobViewModel getLobViewModel();

	@Override
	public final View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		mLobViewModel = getLobViewModel();
		mLobViewModel.setActivity((MainActivity) requireActivity());
		View root = inflater.inflate(R.layout.fragment_lob, container, false);

		final Switch switchBluetoothStatus = root.findViewById(R.id.switchBluetoothStatus);
		final LinearLayout layoutControlInfo = root.findViewById(R.id.layoutControlInfo);
		mLobViewModel.getStatusBluetooth().observe(getViewLifecycleOwner(), checked -> {
			switchBluetoothStatus.setChecked(checked);
			layoutControlInfo.setVisibility(checked ? View.VISIBLE : View.GONE);
		});

		final Switch switchActive = root.findViewById(R.id.switchActive);
		mLobViewModel.getIsActive().observe(getViewLifecycleOwner(), switchActive::setChecked);

		final Spinner spinnerMode = root.findViewById(R.id.spinnerMode);
		ArrayAdapter<String> spinnerModeArrayAdapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item,
				getResources().getStringArray(R.array.values_mode_lob));
		spinnerMode.setAdapter(spinnerModeArrayAdapter);
		mLobViewModel.getMode().observe(getViewLifecycleOwner(), mode -> spinnerMode.setSelection(mode.ordinal()));

		switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> mLobViewModel.updateActiveStatus(isChecked));
		switchActive.setOnClickListener(v -> mLobViewModel.updateMode(Mode.OFF));

		TableRow tableRowPower = root.findViewById(R.id.tableRowPower);
		TableRow tableRowMinPower = root.findViewById(R.id.tableRowMinPower);
		TableRow tableRowCycleLength = root.findViewById(R.id.tableRowCycleLength);
		TableRow tableRowRunningProbability = root.findViewById(R.id.tableRowRunningProbability);
		TableRow tableRowAvgOffDuration = root.findViewById(R.id.tableRowAvgOffDuration);
		TableRow tableRowAvgOnDuration = root.findViewById(R.id.tableRowAvgOnDuration);

		spinnerMode.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(final AdapterView<?> parent, final View view, final int position, final long id) {
				Mode mode = Mode.fromOrdinal(position);
				switch (mode) {
				case OFF:
					tableRowPower.setVisibility(View.GONE);
					tableRowMinPower.setVisibility(View.GONE);
					tableRowCycleLength.setVisibility(View.GONE);
					tableRowRunningProbability.setVisibility(View.GONE);
					tableRowAvgOffDuration.setVisibility(View.GONE);
					tableRowAvgOnDuration.setVisibility(View.GONE);
					mLobViewModel.updateActiveStatus(false);
					break;
				case WAVE:
					tableRowPower.setVisibility(View.VISIBLE);
					tableRowMinPower.setVisibility(View.VISIBLE);
					tableRowCycleLength.setVisibility(View.VISIBLE);
					tableRowRunningProbability.setVisibility(View.GONE);
					tableRowAvgOffDuration.setVisibility(View.GONE);
					tableRowAvgOnDuration.setVisibility(View.GONE);
					mLobViewModel.updateActiveStatus(true);
					break;
				case RANDOM_1:
					tableRowPower.setVisibility(View.VISIBLE);
					tableRowMinPower.setVisibility(View.VISIBLE);
					tableRowCycleLength.setVisibility(View.GONE);
					tableRowRunningProbability.setVisibility(View.VISIBLE);
					tableRowAvgOffDuration.setVisibility(View.GONE);
					tableRowAvgOnDuration.setVisibility(View.GONE);
					mLobViewModel.updateActiveStatus(true);
					break;
				case RANDOM_2:
					tableRowPower.setVisibility(View.VISIBLE);
					tableRowMinPower.setVisibility(View.VISIBLE);
					tableRowCycleLength.setVisibility(View.GONE);
					tableRowRunningProbability.setVisibility(View.GONE);
					tableRowAvgOffDuration.setVisibility(View.VISIBLE);
					tableRowAvgOnDuration.setVisibility(View.VISIBLE);
					mLobViewModel.updateActiveStatus(true);
					break;
				default:
					break;
				}
				mLobViewModel.updateMode(mode);
			}

			@Override
			public void onNothingSelected(final AdapterView<?> parent) {
				// do nothing
			}
		});

		prepareSeekbarsPower(root);
		prepareSeekbarCycleLength(root);
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

		mLobViewModel.getPower().observe(getViewLifecycleOwner(), power -> {
			seekbarPower.setProgress(power);
			textViewPower.setText(String.format(Locale.getDefault(), "%d", power));
		});
		mLobViewModel.getMinPower().observe(getViewLifecycleOwner(), minPower -> {
			seekbarMinPower.setProgress(mLobViewModel.getMinPowerSeekbarValue(minPower));
			textViewMinPower.setText(String.format(Locale.getDefault(), "%d", minPower));
		});

		seekbarPower.setOnSeekBarChangeListener(
				(OnSeekBarProgressChangedListener) progress -> mLobViewModel.updatePower(progress, seekbarMinPower.getProgress()));
		seekbarMinPower.setOnSeekBarChangeListener(
				(OnSeekBarProgressChangedListener) progress -> mLobViewModel.updatePower(seekbarPower.getProgress(), progress));
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
		mLobViewModel.getCycleLength().observe(getViewLifecycleOwner(), cycleLength -> {
			seekbarCycleLength.setProgress(cycleLength);
			textViewCycleLength.setText(String.format(Locale.getDefault(), "%d", cycleLength));
		});
		seekbarCycleLength.setOnSeekBarChangeListener((OnSeekBarProgressChangedListener) progress -> mLobViewModel.updateCycleLength(progress));
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
		mLobViewModel.getRunningProbability().observe(getViewLifecycleOwner(), runningProbability -> {
			int seekbarValue = (int) Math.round(runningProbability * 100); // MAGIC_NUMBER
			seekbarRunningProbability.setProgress(seekbarValue);
			textViewRunningProbability.setText(String.format(Locale.getDefault(), "%d%%", seekbarValue));
		});
		seekbarRunningProbability
				.setOnSeekBarChangeListener((OnSeekBarProgressChangedListener) progress -> //
				mLobViewModel.updateRunningProbability(progress / 100.0)); // MAGIC_NUMBER
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
				LobViewModel.avgDurationSeekbarToValue(seekbarAvgOffDuration.getProgress()) / 1000.0)); // MAGIC_NUMBER
		mLobViewModel.getAvgOffDuration().observe(getViewLifecycleOwner(), avgOffDuration -> {
			int seekbarValue = LobViewModel.avgDurationValueToSeekbar(avgOffDuration);
			seekbarAvgOffDuration.setProgress(seekbarValue);
			textViewAvgOffDuration.setText(String.format(Locale.getDefault(), "%.1fs", avgOffDuration / 1000.0)); // MAGIC_NUMBER
		});
		seekbarAvgOffDuration
				.setOnSeekBarChangeListener((OnSeekBarProgressChangedListener) progress -> //
						mLobViewModel.updateAvgOffDuration(LobViewModel.avgDurationSeekbarToValue(progress))); // MAGIC_NUMBER
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
				LobViewModel.avgDurationSeekbarToValue(seekbarAvgOnDuration.getProgress()) / 1000.0)); // MAGIC_NUMBER
		mLobViewModel.getAvgOnDuration().observe(getViewLifecycleOwner(), avgOnDuration -> {
			int seekbarValue = LobViewModel.avgDurationValueToSeekbar(avgOnDuration);
			seekbarAvgOnDuration.setProgress(seekbarValue);
			textViewAvgOnDuration.setText(String.format(Locale.getDefault(), "%.1fs", avgOnDuration / 1000.0)); // MAGIC_NUMBER
		});
		seekbarAvgOnDuration
				.setOnSeekBarChangeListener((OnSeekBarProgressChangedListener) progress -> //
						mLobViewModel.updateAvgOnDuration(LobViewModel.avgDurationSeekbarToValue(progress))); // MAGIC_NUMBER
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
