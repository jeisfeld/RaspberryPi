package de.jeisfeld.pi.lut;

import java.io.IOException;

/**
 * Sender for a specific LuT channel.
 */
public class ChannelSender {
	/**
	 * The Sender used by this ChannelSender.
	 */
	private Sender mSender;
	/**
	 * The channel number.
	 */
	private int mChannel;

	/**
	 * Duration of fixed chung in varying signals.
	 */
	private static final int CHUNK_DURATION = 200;

	/**
	 * Constructor for a ChannelSender.
	 *
	 * @param sender The sender to be used.
	 * @param channel The channel to be used.
	 * @throws IOException issues with connection.
	 */
	public ChannelSender(final Sender sender, final int channel) throws IOException {
		this.mSender = sender;
		this.mChannel = channel;
	}

	/**
	 * Send a fixed "Lob" message.
	 *
	 * @param power The power to be used.
	 * @param duration The duration of the message in ms.
	 * @throws IOException issues with connection
	 * @throws InterruptedException Thread interrupted
	 */
	public void lob(final byte power, final long duration) throws IOException, InterruptedException {
		mSender.write("L" + mChannel + "P" + power, duration);
	}

	/**
	 * Send a varying "Lob" message.
	 *
	 * @param startPower The start power.
	 * @param endPower The end power.
	 * @param duration The duration.
	 * @throws IOException issues with connection
	 * @throws InterruptedException Thread interrupted.
	 */
	public void lob(final byte startPower, final byte endPower, final long duration) throws IOException, InterruptedException {
		long stepCount = duration / 200; // MAGIC_NUMBER
		if (stepCount <= 1) {
			lob(endPower, duration);
		}
		else {
			long stepDuration = duration / stepCount;
			for (int i = 0; i < stepCount; i++) {
				byte power = (byte) (startPower + (i * (endPower - startPower)) / (stepCount - 1));
				lob(power, stepDuration);
			}
		}
	}

	/**
	 * Switch the "Lob" off.
	 *
	 * @throws IOException issues with connection
	 * @throws InterruptedException Thread interrupted.
	 */
	public void lobOff() throws IOException, InterruptedException {
		mSender.write("L" + mChannel, CHUNK_DURATION);
	}

	/**
	 * Send af fixed "Tadel" message.
	 *
	 * @param power The power
	 * @param frequency The frequency
	 * @param wave The waveform
	 * @param duration The duration
	 * @throws IOException issues with connection
	 * @throws InterruptedException Thread interrupted.
	 */
	public void tadel(final byte power, final byte frequency, final byte wave, final long duration)
			throws IOException, InterruptedException {
		mSender.write("T" + mChannel + "P" + power + "F" + frequency + "W" + wave, duration);
	}

	/**
	 * Send a varying "Tadel" message.
	 *
	 * @param startPower The start power
	 * @param endPower The end power
	 * @param startFrequency The start frequency
	 * @param endFrequency The end frequency
	 * @param wave the waveform
	 * @param duration The duration
	 * @throws IOException issues with connection
	 * @throws InterruptedException Thread interrupted.
	 */
	public void tadel(final byte startPower, final byte endPower, final byte startFrequency, final byte endFrequency, final byte wave,
			final long duration) throws IOException, InterruptedException {
		long stepCount = duration / 200; // MAGIC_NUMBER
		if (stepCount <= 1) {
			tadel(endPower, endFrequency, wave, duration);
		}
		else {
			long stepDuration = duration / stepCount;
			for (int i = 0; i < stepCount; i++) {
				byte power = (byte) (startPower + (i * (endPower - startPower)) / (stepCount - 1));
				byte frequency = (byte) (startFrequency + (i * (endFrequency - startFrequency)) / (stepCount - 1));
				tadel(power, frequency, wave, stepDuration);
			}
		}
	}

	/**
	 * Switch the "Tadel" off.
	 *
	 * @throws IOException issues with connection
	 * @throws InterruptedException Thread interrupted.
	 */
	public void tadelOff() throws IOException, InterruptedException {
		mSender.write("T" + mChannel, CHUNK_DURATION);
	}

	/**
	 * Close the channel sender.
	 *
	 * @throws IOException issues with connection
	 * @throws InterruptedException Thread interrupted.
	 */
	public void close() throws IOException, InterruptedException {
		lobOff();
		tadelOff();
		mSender.close();
	}

}
