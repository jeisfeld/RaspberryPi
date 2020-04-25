package de.jeisfeld.lut.app.ui.lob;

import androidx.lifecycle.ViewModelProvider;

public class Lob0Fragment extends LobFragment {
	@Override
	protected final LobViewModel getLobViewModel() {
		return new ViewModelProvider(requireActivity()).get(Lob0ViewModel.class);
	}
}
