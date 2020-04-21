package de.jeisfeld.lut.bluetooth.message;

/**
 * A ping message.
 */
public class PingMessage extends Message {
	/**
	 * Constructor.
	 */
	public PingMessage() {
	}

	@Override
	public final MessageType getType() {
		return MessageType.PING;
	}

	@Override
	protected final String getDataString() {
		return "";
	}
}
