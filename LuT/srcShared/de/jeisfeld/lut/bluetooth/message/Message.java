package de.jeisfeld.lut.bluetooth.message;

/**
 * Messages transferred via Bluetooth between app and pi.
 */
public abstract class Message {
	/**
	 * Get the type of message.
	 *
	 * @return The message type.
	 */
	public abstract MessageType getType();

	/**
	 * Get the data of the message.
	 *
	 * @return The message data.
	 */
	public abstract String getDataString();

	@Override
	public final String toString() {
		return getType().name() + ":" + getDataString();
	}

	/**
	 * Retrieve message from its String representation.
	 *
	 * @param data The message String representation.
	 * @return The message.
	 */
	public static Message fromString(final String data) {
		if (data == null || data.isEmpty()) {
			return null;
		}
		String[] splitData = data.split(":", 2);
		MessageType type = MessageType.valueOf(splitData[0]);
		String messageData = splitData.length > 1 ? splitData[1] : "";

		switch (type) {
		case CONNECTED:
			return new ConnectedMessage();
		case PING:
			return new PingMessage();
		case FREE_TEXT:
			return new FreeTextMessage(messageData);
		case BUTTON_STATUS:
			return new ButtonStatusMessage(messageData);
		case PROCESSING_MODE:
			return new ProcessingModeMessage(messageData);
		default:
			return null;
		}
	}

	/**
	 * The types of bluetooth messages.
	 */
	public enum MessageType {
		/**
		 * Message passed when connected.
		 */
		CONNECTED,
		/**
		 * Ping message.
		 */
		PING,
		/**
		 * Free text message.
		 */
		FREE_TEXT,
		/**
		 * Button status message sent from Pi to Android.
		 */
		BUTTON_STATUS,
		/**
		 * The processing mode of the Pi LuT application.
		 */
		PROCESSING_MODE
	}

}
