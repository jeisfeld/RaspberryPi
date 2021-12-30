package de.jeisfeld.lut.app.ui.control;

import androidx.lifecycle.ViewModelProvider;

/**
 * The fragment for managing lob messages on channel 0.
 */
public class Lob0Fragment extends LobFragment {
	@Override
	protected final ControlViewModel getControlViewModel() {
		return new ViewModelProvider(requireActivity()).get(Lob0ViewModel.class);
	}
}
