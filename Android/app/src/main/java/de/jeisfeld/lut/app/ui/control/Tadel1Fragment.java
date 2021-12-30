package de.jeisfeld.lut.app.ui.control;

import androidx.lifecycle.ViewModelProvider;

/**
 * The fragment for managing tadel messages on channel 1.
 */
public class Tadel1Fragment extends TadelFragment {
	@Override
	protected final ControlViewModel getControlViewModel() {
		return new ViewModelProvider(requireActivity()).get(Tadel1ViewModel.class);
	}
}
