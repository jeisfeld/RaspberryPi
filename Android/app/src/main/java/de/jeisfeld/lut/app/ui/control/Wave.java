package de.jeisfeld.lut.app.ui.control;

/**
 * The possible waves.
 */
public enum Wave {
	/**
	 * Constant power.
	 */
	CONSTANT(0),
	/**
	 * Rampup with low pulse.
	 */
	RAMPUP_LOWPULSE(1),
	/**
	 * Rampup with high pulse.
	 */
	RAMPUP_HIGHPULSE(2),
	/**
	 * Ramp up and down with low pulse.
	 */
	RAMPUPDOWN_LOWPULSE(3),
	/**
	 * Ramp up and down with high pulse.
	 */
	RAMPUPDOWN_HIGHPULSE(4),
	/**
	 * Freqency goes up and down.
	 */
	FREQUENCY_UP_DOWN(7),
	/**
	 * Freqency goes up and down.
	 */
	RAMPUPDOWN_WOBBLE(10),
	/**
	 * Freqency goes up and down.
	 */
	FREQUENCY_UP_WOBBLE(16),
	/**
	 * Freqency goes up and down.
	 */
	RAMPUPDOWN_IMPULSE(20);

	/**
	 * The integer value for Tadel.
	 */
	private int mTadelValue;

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
	 * @param tadelValue The integer value for Tadel.
	 */
	Wave(final int tadelValue) {
		mTadelValue = tadelValue;
	}

	/**
	 * Get wave from its Tadel value.
	 *
	 * @param tadelValue The Tadel value.
	 * @return The corresponding wave.
	 */
	public static Wave fromTadelValue(final int tadelValue) {
		if (tadelValue < 0) {
			return CONSTANT;
		}
		for (Wave wave : values()) {
			if (wave.mTadelValue == tadelValue) {
				return wave;
			}
		}
		return CONSTANT;
	}

	/**
	 * Get wave from its ordinal value.
	 *
	 * @param ordinal The ordinal value.
	 * @return The corresponding wave.
	 */
	public static Wave fromOrdinal(final int ordinal) {
		for (Wave wave : values()) {
			if (wave.ordinal() == ordinal) {
				return wave;
			}
		}
		return CONSTANT;
	}
}
