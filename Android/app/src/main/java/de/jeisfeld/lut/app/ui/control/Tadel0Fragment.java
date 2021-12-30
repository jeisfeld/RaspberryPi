package de.jeisfeld.lut.app.ui.control;

import androidx.lifecycle.ViewModelProvider;

/**
 * The fragment for managing tadel messages on channel 0.
 */
public class Tadel0Fragment extends TadelFragment {
	@Override
	protected final ControlViewModel getControlViewModel() {
		return new ViewModelProvider(requireActivity()).get(Tadel0ViewModel.class);
	}
}
