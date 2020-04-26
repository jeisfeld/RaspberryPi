package de.jeisfeld.lut.app.ui.control;

import androidx.lifecycle.ViewModelProvider;

/**
 * The fragment for managing lob messages on channel 1.
 */
public class Lob1Fragment extends LobFragment {
	@Override
	protected final ControlViewModel getLobViewModel() {
		return new ViewModelProvider(requireActivity()).get(Lob1ViewModel.class);
	}
}
