package de.jeisfeld.pi.lut.core;

import de.jeisfeld.pi.lut.core.ButtonStatus.ButtonListener;
import de.jeisfeld.pi.lut.core.ButtonStatus.OnLongPressListener;
import de.jeisfeld.pi.lut.core.command.Lob;
import de.jeisfeld.pi.lut.core.command.Tadel;

/**
 * Sender for a specific LuT channel.
 */
public class ChannelSender {
	/**
	 * The Sender used by this ChannelSender.
	 */
	private final Sender mSender;
	/**
	 * The channel number.
	 */
	private final int mChannel;

	/**
	 * Constructor for a ChannelSender.
	 *
	 * @param sender The sender to be used.
	 * @param channel The channel to be used.
	 */
	public ChannelSender(final Sender sender, final int channel) {
		mSender = sender;
		mChannel = channel;
	}

	/**
	 * Send a fixed "Lob" message.
	 *
	 * @param power The power to be used.
	 * @param duration The duration of the message in ms.
	 * @throws InterruptedException Thread interrupted
	 */
	public void lob(final int power, final long duration) throws InterruptedException {
		long startTime = System.currentTimeMillis(); // SUPPRESS_CHECKSTYLE
		Lob lob = new Lob(mChannel, power, duration);
		if (duration >= Sender.SEND_DURATION) {
			lob.setNoOverride();
		}
		mSender.processCommands(lob);
		long remainingTime = duration - System.currentTimeMillis() + startTime;
		if (remainingTime > 0) {
			Thread.sleep(remainingTime);
		}
	}

	/**
	 * Send a fixed "Lob" message for standard duration, which is the real runtime.
	 *
	 * @param power The power to be used.
	 */
	public void lob(final int power) {
		try {
			lob(power, Sender.SEND_DURATION);
		}
		catch (InterruptedException e) {
			// ignore
		}
	}

	/**
	 * Send a varying "Lob" message.
	 *
	 * @param startPower The start power.
	 * @param endPower The end power.
	 * @param duration The duration.
	 * @throws InterruptedException Thread interrupted.
	 */
	public void lob(final int startPower, final int endPower, final long duration) throws InterruptedException {
		long stepCount = duration / Sender.SEND_DURATION;
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
	 * Send af fixed "Tadel" message.
	 *
	 * @param power The power
	 * @param frequency The frequency
	 * @param wave The waveform
	 * @param duration The duration
	 * @throws InterruptedException Thread interrupted.
	 */
	public void tadel(final int power, final int frequency, final int wave, final long duration) throws InterruptedException {
		long startTime = System.currentTimeMillis(); // SUPPRESS_CHECKSTYLE
		Tadel tadel = new Tadel(mChannel, power, frequency, wave, duration);
		if (duration >= Sender.SEND_DURATION) {
			tadel.setNoOverride();
		}
		mSender.processCommands(tadel);
		long remainingTime = duration - System.currentTimeMillis() + startTime;
		if (remainingTime > 0) {
			Thread.sleep(remainingTime);
		}
	}

	/**
	 * Send a fixed "Tadel" message for standard duration, which is the real runtime.
	 *
	 * @param power The power
	 * @param frequency The frequency
	 * @param wave The waveform
	 */
	public void tadel(final int power, final int frequency, final int wave) {
		try {
			tadel(power, frequency, wave, Sender.SEND_DURATION);
		}
		catch (InterruptedException e) {
			// ignore
		}
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
	 * @throws InterruptedException Thread interrupted.
	 */
	public void tadel(final int startPower, final int endPower, final int startFrequency, final int endFrequency, final int wave,
			final long duration) throws InterruptedException {
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
	 * Get the button status.
	 *
	 * @return The button status.
	 */
	public ButtonStatus getButtonStatus() {
		return mSender.getButtonStatus();
	}

	/**
	 * Set listener for button 1.
	 *
	 * @param listener The listener.
	 */
	public void setButton1Listener(final ButtonListener listener) {
		mSender.setButton1Listener(listener);
	}

	/**
	 * Set listener for button 2.
	 *
	 * @param listener The listener.
	 */
	public void setButton2Listener(final ButtonListener listener) {
		mSender.setButton2Listener(listener);
	}

	/**
	 * Set long press listener for button 1.
	 *
	 * @param listener The listener.
	 */
	public void setButton1LongPressListener(final OnLongPressListener listener) {
		mSender.setButton1LongPressListener(listener);
	}

	/**
	 * Set long press listener for button 2.
	 *
	 * @param listener The listener.
	 */
	public void setButton2LongPressListener(final OnLongPressListener listener) {
		mSender.setButton2LongPressListener(listener);
	}
}
