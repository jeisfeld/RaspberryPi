package de.jeisfeld.lut.app.ui.control;

/**
 * The possible pulse triggers.
 */
public enum PulseTrigger {
	/**
	 * Trigger on display of Random Image.
	 */
	RANDOM_IMAGE_DISPLAY(true, false),
	/**
	 * Pulse during hold of breath training.
	 */
	BREATH_TRAINING_HOLD(false, false),
	/**
	 * Pulse during device acceleration.
	 */
	ACCELERATION(false, true),
	/**
	 * Pulse during microphone input.
	 */
	MICROPHONE(false, true);

	/**
	 * Flag indicating if the trigger requires duration.
	 */
	private final boolean mIsWithDuration;
	/**
	 * Flag indicating if the trigger requires sensitivity.
	 */
	private final boolean mIsWithSensitivity;

	/**
	 * Constructor.
	 *
	 * @param isWithDuration Flag indicating if the trigger requires duration.
	 * @param isWithSensitivity Flag indicating if the trigger requires sensitivity value.
	 */
	PulseTrigger(final boolean isWithDuration, final boolean isWithSensitivity) {
		mIsWithDuration = isWithDuration;
		mIsWithSensitivity = isWithSensitivity;
	}

	/**
	 * Get pulse trigger from its ordinal value.
	 *
	 * @param ordinal The ordinal value.
	 * @return The corresponding pulse trigger.
	 */
	public static PulseTrigger fromOrdinal(final int ordinal) {
		for (PulseTrigger pulseTrigger : values()) {
			if (pulseTrigger.ordinal() == ordinal) {
				return pulseTrigger;
			}
		}
		return RANDOM_IMAGE_DISPLAY;
	}

	/**
	 * Get the flag indicating if the trigger requires duration.
	 *
	 * @return The flag indicating if the trigger requires duration.
	 */
	public boolean isWithDuration() {
		return mIsWithDuration;
	}

	/**
	 * Get the flag indicating if the trigger requires sensitivity.
	 *
	 * @return The flag indicating if the trigger requires sensitivity.
	 */
	public boolean isWithSensitivity() {
		return mIsWithSensitivity;
	}
}
