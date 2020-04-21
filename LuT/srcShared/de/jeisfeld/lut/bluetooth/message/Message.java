package de.jeisfeld.lut.bluetooth.message;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

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
	protected abstract String getDataString();

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
			return new FreeTextMessage(Message.decode(messageData));
		case BUTTON_STATUS:
			return new ButtonStatusMessage(messageData);
		case PROCESSING_MODE:
			return new ProcessingModeMessage(messageData);
		default:
			return null;
		}
	}

	/**
	 * Encode a String for passing within message.
	 *
	 * @param s The String.
	 * @return The decoded String.
	 */
	protected static final String encode(final String s) {
		return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Decode a String from message.
	 *
	 * @param s The String from message.
	 * @return The decoded String.
	 */
	protected static final String decode(final String s) {
		return new String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8);
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
