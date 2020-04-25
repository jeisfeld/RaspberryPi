package de.jeisfeld.lut.app.ui.lob;

import androidx.lifecycle.ViewModelProvider;

public class Lob1Fragment extends LobFragment {
	@Override
	protected final LobViewModel getLobViewModel() {
		return new ViewModelProvider(requireActivity()).get(Lob1ViewModel.class);
	}
}
