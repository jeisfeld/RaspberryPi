package de.jeisfeld.pi.gpio;

import com.pi4j.io.gpio.Pin;

/**
 * Management of M74HC595 Shift register.
 */
public class M74hc595 {
	/**
	 * The data pin sending data bytes.
	 */
	private final DigitalOutput mDataOutput;
	/**
	 * The latch pin triggering the sending of bytes from the output of M74HC595.
	 */
	private final DigitalOutput mLatchOutput;
	/**
	 * The clock pin triggering the sending of bits to the input of M74HC595.
	 */
	private final DigitalOutput mClockOutput;
	/**
	 * Flag indicating the sequence in which bits are sent.
	 */
	private final boolean mHighBitFirst;

	/**
	 * Initialize the M74hc595 inputs.
	 *
	 * @param dataPin The data pin sending data bytes.
	 * @param latchPin The latch pin triggering the sending of bytes from the output of M74HC595
	 * @param clockPin The clock pin triggering the sending of bits to the input of M74HC595
	 * @param highBitFirst Flag indicating the sequence in which bits are sent
	 */
	public M74hc595(final Pin dataPin, final Pin latchPin, final Pin clockPin, final boolean highBitFirst) {
		mDataOutput = new DigitalOutput(dataPin, false);
		mLatchOutput = new DigitalOutput(latchPin, false);
		mClockOutput = new DigitalOutput(clockPin, false);
		mHighBitFirst = highBitFirst;
	}

	/**
	 * Setting a data byte via M74HC595.
	 *
	 * @param dataByte The data byte.
	 */
	public void setByte(final int dataByte) {
		mLatchOutput.setValue(false);
		for (int i = 0; i < 8; i++) { // MAGIC_NUMBER
			int byteIndex = mHighBitFirst ? 7 - i : i; // MAGIC_NUMBER
			mClockOutput.setValue(false);
			mDataOutput.setValue(((dataByte >> byteIndex) & 1) == 1);
			mClockOutput.setValue(true);
		}
		mLatchOutput.setValue(true);
	}

}
