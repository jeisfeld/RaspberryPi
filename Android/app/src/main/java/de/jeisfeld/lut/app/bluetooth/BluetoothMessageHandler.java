package de.jeisfeld.lut.app.bluetooth;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import de.jeisfeld.lut.app.MainActivity;

/**
 * Message handler for the bluetooth connection.
 */
public class BluetoothMessageHandler extends Handler {
	/**
	 * The logging tag.
	 */
	private static final String TAG = "JE.LuT.BTMessageHandler";
	/**
	 * The reconnect delay in ms.
	 */
	private static final int RECONNECT_DELAY = 1000;
	/**
	 * The context.
	 */
	private final WeakReference<MainActivity> mActivity;

	/**
	 * Constructor.
	 *
	 * @param activity The triggering activity
	 */
	public BluetoothMessageHandler(final MainActivity activity) {
		mActivity = new WeakReference<>(activity);
	}

	@Override
	public final void handleMessage(final Message msg) {
		super.handleMessage(msg);

		String data = msg.obj instanceof String ? (String) msg.obj : null;
		MessageType messageType = MessageType.fromOrdinal(msg.what);
		MainActivity activity = mActivity.get();

		switch (messageType) {
		case RECONNECT:
			if (activity != null && !activity.isDestroyed() && !activity.isFinishing()) {
				activity.updateConnectedStatus(false);
				activity.connect();
			}
			break;
		case READ:
			de.jeisfeld.lut.bluetooth.message.Message message = null;
			try {
				message = de.jeisfeld.lut.bluetooth.message.Message.fromString(data);
			}
			catch (Exception e) {
				Log.e(TAG, "Exception while decoding message: " + data, e);
			}
			if (message != null) {
				if (activity != null) {
					activity.updateOnMessageReceived(message);
				}
			}
			else {
				Log.i(TAG, "Received unknown read message: " + data);
			}
			break;
		case CONNECTED:
			if (activity != null && !activity.isDestroyed() && !activity.isFinishing()) {
				activity.updateConnectedStatus(true);
			}
			break;
		default:
			Log.i(TAG, "Received message: " + messageType.name() + (data == null ? "" : " - " + data));
		}
	}

	/**
	 * Send a message.
	 *
	 * @param messageType The message type.
	 * @param message The message content.
	 */
	protected void sendMessage(final MessageType messageType, final Object message) {
		obtainMessage(messageType.ordinal(), message).sendToTarget();
	}

	/**
	 * Send a reconnect message.
	 */
	protected void sendReconnect() {
		Message message = new Message();
		message.what = MessageType.RECONNECT.ordinal();
		sendMessageDelayed(message, BluetoothMessageHandler.RECONNECT_DELAY);
	}

	/**
	 * The message types.
	 */
	public enum MessageType {
		/**
		 * Unknown message.
		 */
		UNKNOWN,
		/**
		 * Read data.
		 */
		READ,
		/**
		 * Write data.
		 */
		WRITE,
		/**
		 * Connection error.
		 */
		RECONNECT,
		/**
		 * Connected message.
		 */
		CONNECTED;

		/**
		 * Get the message type from its ordinal value.
		 *
		 * @param ordinal The ordinal value.
		 * @return The message type.
		 */
		public static MessageType fromOrdinal(final int ordinal) {
			for (MessageType messageType : MessageType.values()) {
				if (messageType.ordinal() == ordinal) {
					return messageType;
				}
			}
			return UNKNOWN;
		}
	}
}
