package de.jeisfeld.lut.app.ui.control;

/**
 * The possible pulse triggers.
 */
public enum PulseTrigger {
	/**
	 * Trigger on display of Random Image.
	 */
	RANDOM_IMAGE_DISPLAY(true),
	/**
	 * Pulse during hold of breath training.
	 */
	BREATH_TRAINING_HOLD(false);

	/**
	 * Flag indicating if the trigger requires duration.
	 */
	private final boolean mIsWithDuration;

	/**
	 * Constructor.
	 *
	 * @param isWithDuration Flag indicating if the trigger requires duration.
	 */
	PulseTrigger(final boolean isWithDuration) {
		mIsWithDuration = isWithDuration;
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
}
