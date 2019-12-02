package de.jeisfeld.pi.lut.core.command;

/**
 * Command for waiting in a controlled way.
 */
public class Wait implements WriteCommand {
	/**
	 * The duration.
	 */
	private long mDuration = 0;
	/**
	 * The override flag.
	 */
	private boolean mIsOverride = false;

	/**
	 * Create a Wait.
	 *
	 * @param duration The duration.
	 */
	public Wait(final long duration) {
		mDuration = duration;
	}

	@Override
	public final String getSerialString() {
		return null;
	}

	@Override
	public final boolean overrides(final WriteCommand other) {
		return false;
	}

	@Override
	public final void setDuration(final long duration) {
		mDuration = duration;
	}

	@Override
	public final long getDuration() {
		return mDuration;
	}

	@Override
	public final String toString() {
		return "WAIT - " + mDuration;
	}

	@Override
	public final void setNoOverride() {
		mIsOverride = false;
	}

	@Override
	public final boolean isOverride() {
		return mIsOverride;
	}
}
