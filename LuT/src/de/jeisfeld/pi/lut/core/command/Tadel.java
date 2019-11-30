package de.jeisfeld.pi.lut.core.command;

/**
 * Command for sending Tacel.
 */
public class Tadel implements WriteCommand {
	/**
	 * The channel.
	 */
	private final int mChannel;
	/**
	 * The power.
	 */
	private final int mPower;
	/**
	 * The frequency.
	 */
	private final int mFrequency;
	/**
	 * The wave.
	 */
	private final int mWave;

	/**
	 * Create a Tadel.
	 *
	 * @param channel the channel.
	 * @param power The power
	 * @param frequency The frequency
	 * @param wave The waveform
	 */
	public Tadel(final int channel, final int power, final int frequency, final int wave) {
		mChannel = channel;
		mPower = power;
		mFrequency = frequency;
		mWave = wave;
	}

	@Override
	public final String getSerialString() {
		return "T" + mChannel + "P" + mPower + "F" + mFrequency + "W" + mWave;
	}

	@Override
	public final boolean overrides(final WriteCommand other) {
		return other instanceof Tadel && ((Tadel) other).mChannel == mChannel;
	}

	@Override
	public final String toString() {
		return getSerialString();
	}
}
