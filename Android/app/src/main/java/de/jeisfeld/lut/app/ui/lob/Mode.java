package de.jeisfeld.lut.app.ui.lob;

/**
 * The Lob modes.
 */
public enum Mode {
	/**
	 * Off.
	 */
	OFF,
	/**
	 * Wave up and down.
	 */
	WAVE,
	/**
	 * Random change between high/low level. Avg signal duration 2s. Levels and Probability controllable.
	 */
	RANDOM_1,
	/**
	 * Random change between on/off. On level and avg off/on duration controllable.
	 */
	RANDOM_2;

	/**
	 * Get mode from its ordinal value.
	 *
	 * @param ordinal The ordinal value.
	 * @return The corresponding mode.
	 */
	public static Mode fromOrdinal(final int ordinal) {
		for (Mode mode : values()) {
			if (mode.ordinal() == ordinal) {
				return mode;
			}
		}
		return OFF;
	}
}
