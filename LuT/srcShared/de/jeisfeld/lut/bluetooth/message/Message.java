package de.jeisfeld.lut.bluetooth.message;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Messages transferred via Bluetooth between app and pi.
 */
public abstract class Message {
	/**
	 * The separator in serialization.
	 */
	protected static final String SEP = ",";

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
		case PROCESSING_STANDALONE:
			return new ProcessingStandaloneMessage(messageData);
		case PROCESSING_BLUETOOTH:
			return new ProcessingBluetoothMessage(messageData);
		case STANDALONE_STATUS:
			return new StandaloneStatusMessage(messageData);
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
	protected static String encode(final String s) {
		return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Decode a String from message.
	 *
	 * @param s The String from message.
	 * @return The decoded String.
	 */
	protected static String decode(final String s) {
		return new String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8);
	}

	/**
	 * Convert integer to String.
	 *
	 * @param i the integer
	 * @return the String
	 */
	protected static String intToString(final Integer i) {
		return i == null ? "" : i.toString();
	}

	/**
	 * Convert String back to integer.
	 *
	 * @param s The string.
	 * @return the integer.
	 */
	protected static Integer stringToInt(final String s) {
		return (s == null || s.isEmpty()) ? null : Integer.parseInt(s);
	}

	/**
	 * Convert long to String.
	 *
	 * @param i the long
	 * @return the String
	 */
	protected static String longToString(final Long i) {
		return i == null ? "" : i.toString();
	}

	/**
	 * Convert String back to long.
	 *
	 * @param s The string.
	 * @return the long.
	 */
	protected static Long stringToLong(final String s) {
		return (s == null || s.isEmpty()) ? null : Long.parseLong(s);
	}

	/**
	 * Convert double to String.
	 *
	 * @param d the double
	 * @return the String
	 */
	protected static String doubleToString(final Double d) {
		return d == null ? "" : d.toString();
	}

	/**
	 * Convert String back to double.
	 *
	 * @param s The string.
	 * @return the double.
	 */
	protected static Double stringToDouble(final String s) {
		return (s == null || s.isEmpty()) ? null : Double.parseDouble(s);
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
		 * The processing status of the Pi LuT standalone processing.
		 */
		PROCESSING_STANDALONE,
		/**
		 * A processing trigger of the Pi LuT bluetooth controlled processing.
		 */
		PROCESSING_BLUETOOTH,
		/**
		 * The status of standalone processing.
		 */
		STANDALONE_STATUS
	}

}
