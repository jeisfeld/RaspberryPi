package de.jeisfeld.pi.lut.core.command;

/**
 * Command for sending Lob.
 */
public class Lob implements WriteCommand {
	/**
	 * The channel.
	 */
	private final int mChannel;
	/**
	 * The power.
	 */
	private final int mPower;
	/**
	 * The duration.
	 */
	private long mDuration = 0;
	/**
	 * The override flag.
	 */
	private boolean mIsOverride = true;

	/**
	 * Create a Lob.
	 *
	 * @param channel the channel.
	 * @param power The power
	 */
	public Lob(final int channel, final int power) {
		mChannel = channel;
		mPower = power;
	}

	@Override
	public final String getSerialString() {
		return "L" + mChannel + "P" + mPower;
	}

	@Override
	public final boolean overrides(final WriteCommand other) {
		return other instanceof Lob && ((Lob) other).mChannel == mChannel;
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
		return getSerialString() + " - " + mDuration;
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
