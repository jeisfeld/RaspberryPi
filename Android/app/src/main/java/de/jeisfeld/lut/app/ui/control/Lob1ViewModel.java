package de.jeisfeld.lut.app.ui.control;

/**
 * View model for Lob on channel 1.
 */
public class Lob1ViewModel extends ControlViewModel {
	@Override
	protected final int getChannel() {
		return 1;
	}

	@Override
	protected final boolean isTadel() {
		return false;
	}
}
