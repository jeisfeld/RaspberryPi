package de.jeisfeld.lut.bluetooth.message;

/**
 * A shutdown message.
 */
public class ShutdownMessage extends Message {
	/**
	 * Constructor.
	 */
	public ShutdownMessage() {
	}

	@Override
	public final MessageType getType() {
		return MessageType.SHUTDOWN;
	}

	@Override
	protected final String getDataString() {
		return "";
	}
}
