package de.jeisfeld.lut.bluetooth.message;

/**
 * The Lob modes.
 */
public enum Mode {
	/**
	 * Off.
	 */
	OFF(0, 0),
	/**
	 * Fixed.
	 */
	FIXED(-1, 1),
	/**
	 * Wave up and down.
	 */
	WAVE(1, -1),
	/**
	 * Random change between high/low level. Avg signal duration 2s. Levels and Probability controllable.
	 */
	RANDOM_1(2, 2),
	/**
	 * Random change between on/off. On level and avg off/on duration controllable.
	 */
	RANDOM_2(3, 3),
	/**
	 * Individual pulses controlled by client.
	 */
	PULSE(4, 4);

	/**
	 * The integer value / dropdown position for Lob.
	 */
	private int mLobValue;

	/**
	 * The integer value / dropdown position for Tadel.
	 */
	private int mTadelValue;

	/**
	 * Get the int value for lob.
	 *
	 * @return The int value for lob.
	 */
	public int getLobValue() {
		return mLobValue;
	}

	/**
	 * Get the int value for tadel.
	 *
	 * @return The int value for tadel.
	 */
	public int getTadelValue() {
		return mTadelValue;
	}

	/**
	 * Constructor.
	 *
	 * @param lobValue The integer value / dropdown position for Lob.
	 * @param tadelValue The integer value / dropdown position for Tadel.
	 */
	Mode(final int lobValue, final int tadelValue) {
		mLobValue = lobValue;
		mTadelValue = tadelValue;
	}

	/**
	 * Get mode from its lob value.
	 *
	 * @param lobValue The lob value.
	 * @return The corresponding mode.
	 */
	public static Mode fromLobValue(final int lobValue) {
		if (lobValue < 0) {
			return OFF;
		}
		for (Mode mode : values()) {
			if (mode.mLobValue == lobValue) {
				return mode;
			}
		}
		return OFF;
	}

	/**
	 * Get mode from its Tadel value.
	 *
	 * @param tadelValue The Tadel value.
	 * @return The corresponding mode.
	 */
	public static Mode fromTadelValue(final int tadelValue) {
		if (tadelValue < 0) {
			return OFF;
		}
		for (Mode mode : values()) {
			if (mode.mTadelValue == tadelValue) {
				return mode;
			}
		}
		return OFF;
	}
}
