package de.jeisfeld.lut.app.ui.control;

/**
 * View model for tadel on channel 1.
 */
public class Tadel1ViewModel extends ControlViewModel {
	@Override
	protected final int getChannel() {
		return 1;
	}

	@Override
	protected final boolean isTadel() {
		return true;
	}
}
