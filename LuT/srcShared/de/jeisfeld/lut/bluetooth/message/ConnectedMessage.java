package de.jeisfeld.lut.bluetooth.message;

/**
 * Message passed when connection is established.
 */
public class ConnectedMessage extends Message {
	/**
	 * Constructor.
	 */
	public ConnectedMessage() {
	}

	@Override
	public final MessageType getType() {
		return MessageType.CONNECTED;
	}

	@Override
	public final String getDataString() {
		return "";
	}
}
