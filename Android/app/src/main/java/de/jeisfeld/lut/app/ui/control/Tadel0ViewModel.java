package de.jeisfeld.lut.app.ui.control;

/**
 * View model for tadel on channel 0.
 */
public class Tadel0ViewModel extends ControlViewModel {
	@Override
	protected final int getChannel() {
		return 0;
	}

	@Override
	protected final boolean isTadel() {
		return true;
	}
}
