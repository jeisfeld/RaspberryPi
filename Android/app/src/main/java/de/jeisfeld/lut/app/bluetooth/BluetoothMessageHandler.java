package de.jeisfeld.lut.app.bluetooth;

import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import de.jeisfeld.lut.app.MainActivity;

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
	private final WeakReference<AppCompatActivity> mActivity;

	/**
	 * Constructor.
	 *
	 * @param activity The triggering activity
	 */
	public BluetoothMessageHandler(final AppCompatActivity activity) {
		mActivity = new WeakReference<>(activity);
	}

	@Override
	public final void handleMessage(final Message msg) {
		super.handleMessage(msg);

		String data = msg.obj instanceof String ? (String) msg.obj : null;
		MessageType messageType = MessageType.fromOrdinal(msg.what);

		Log.i(BluetoothMessageHandler.TAG, "Received message: " + messageType.name() + (data == null ? "" : " - " + data));

		if (messageType == MessageType.ERROR) {
			AppCompatActivity activity = mActivity.get();
			if (activity instanceof MainActivity && !activity.isDestroyed() && !activity.isFinishing()) {
				((MainActivity) activity).connect();
			}
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
	 * Send a message.
	 *
	 * @param messageType The message type.
	 * @param size The size of message content.
	 * @param buffer The message content as byte buffer.
	 */
	protected void sendMessage(final MessageType messageType, final int size, final byte[] buffer) {
		if (size <= 0) {
			sendMessage(messageType, null);
		}
		else {
			sendMessage(messageType, new String(Arrays.copyOf(buffer, size), StandardCharsets.UTF_8));
		}
	}

	/**
	 * Send a reconnect message.
	 */
	protected void sendReconnect() {
		Message message = new Message();
		message.what = MessageType.ERROR.ordinal();
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
		ERROR;

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
