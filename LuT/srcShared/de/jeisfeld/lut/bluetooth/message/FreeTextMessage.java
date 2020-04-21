package de.jeisfeld.lut.bluetooth.message;

/**
 * A free text message.
 */
public class FreeTextMessage extends Message {
	/**
	 * The text.
	 */
	private final String mText;

	/**
	 * Constructor.
	 *
	 * @param text The text.
	 */
	public FreeTextMessage(final String text) {
		mText = text;
	}

	@Override
	public final MessageType getType() {
		return MessageType.FREE_TEXT;
	}

	@Override
	protected final String getDataString() {
		return Message.encode(mText);
	}

	/**
	 * Get the text.
	 *
	 * @return The text
	 */
	public String getText() {
		return mText;
	}

}
