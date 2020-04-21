package de.jeisfeld.pi.lut;

import java.io.IOException;

import de.jeisfeld.lut.bluetooth.message.ButtonStatusMessage;
import de.jeisfeld.lut.bluetooth.message.Message;
import de.jeisfeld.lut.bluetooth.message.ProcessingModeMessage;
import de.jeisfeld.pi.bluetooth.BluetoothMessageHandler;
import de.jeisfeld.pi.bluetooth.ConnectThread;
import de.jeisfeld.pi.lut.core.ButtonStatus;
import de.jeisfeld.pi.lut.core.ButtonStatus.ButtonListener;
import de.jeisfeld.pi.lut.core.ButtonStatus.OnLongPressListener;
import de.jeisfeld.pi.lut.core.ButtonStatusUpdateListener;
import de.jeisfeld.pi.lut.core.Sender;
import de.jeisfeld.pi.lut.core.ShutdownListener;
import de.jeisfeld.pi.util.Logger;

/**
 * Test class for LuT framework.
 */
public class Startup { // SUPPRESS_CHECKSTYLE
	/**
	 * Flag indicating if we run RandomizedLob or RandomizedTadel.
	 */
	private static boolean mIsTadel = false;
	/**
	 * The used channel.
	 */
	private static int mChannel = 0;
	/**
	 * The used mode.
	 */
	private static int mMode = 0;

	/**
	 * Main method.
	 *
	 * @param args The command line arguments (not required).
	 * @throws IOException connection issues
	 * @throws InterruptedException if interrupted
	 */
	public static void main(final String[] args) throws IOException, InterruptedException { // SUPPRESS_CHECKSTYLE
		ConnectThread connectThread = new ConnectThread();
		connectThread.setMessageHandler(new BluetoothMessageHandler() {
			@Override
			public void onMessageReceived(final String data) {
				Message message = Message.fromString(data);
				if (message == null) {
					Logger.error(new RuntimeException("Received unspecified bluetooth message: " + data));
				}
				else {
					switch (message.getType()) {
					case CONNECTED:
						connectThread.write(new ProcessingModeMessage(Startup.mChannel, Startup.mIsTadel, Startup.mMode));
						break;
					case PING:
						Logger.info("Ping");
						break;
					case FREE_TEXT:
						Logger.log("Received free text message: " + message.getDataString());
						break;
					case PROCESSING_MODE:
					case BUTTON_STATUS:
					default:
						Logger.error(new RuntimeException("Received unexpected message: " + data));
					}
				}

			}
		});

		connectThread.start();

		OnModeChangeListener listener = new OnModeChangeListener() {
			@Override
			public void onModeChange(final int mode) {
				Startup.mMode = mode;
				connectThread.write(new ProcessingModeMessage(Startup.mChannel, Startup.mIsTadel, mode));
			}
		};

		RandomizedLob[] lobs = {new RandomizedLob(0, listener), new RandomizedLob(1, listener)};
		RandomizedTadel[] tadels = {new RandomizedTadel(0, listener), new RandomizedTadel(1, listener)};

		Sender sender = Sender.getInstance();
		sender.setButton2LongPressListener(new ShutdownListener(4000)); // MAGIC_NUMBER

		sender.setButton2Listener(new ButtonListener() {
			@Override
			public void handleButtonDown() {
				Startup.mChannel = (Startup.mChannel + 1) % 2;

				if (Startup.mIsTadel) {
					for (RandomizedTadel tadel : tadels) {
						tadel.stop();
					}
					lobs[Startup.mChannel].signal(2, true);
					new Thread(tadels[Startup.mChannel]).start();
				}
				else {
					for (RandomizedLob lob : lobs) {
						lob.stop();
					}
					lobs[Startup.mChannel].signal(1, true);
					new Thread(lobs[Startup.mChannel]).start();
				}
				listener.onModeChange(0);
			}
		});

		sender.setButton1LongPressListener(new OnLongPressListener() {
			@Override
			public void handleLongTrigger() {
				if (Startup.mIsTadel) {
					for (RandomizedTadel tadel : tadels) {
						tadel.stop();
					}
					lobs[Startup.mChannel].signal(1, true);
					new Thread(lobs[Startup.mChannel]).start();
					Startup.mIsTadel = false;
				}
				else {
					for (RandomizedLob lob : lobs) {
						lob.stop();
					}
					lobs[Startup.mChannel].signal(2, true);
					new Thread(tadels[Startup.mChannel]).start();
					Startup.mIsTadel = true;
				}
				listener.onModeChange(0);
			}
		});

		new Thread(lobs[Startup.mChannel]).start();

		sender.setButtonStatusUpdateListener(new ButtonStatusUpdateListener() {
			@Override
			public void onButtonStatusUpdated(final ButtonStatus status) {
				connectThread.write(new ButtonStatusMessage(status.toString()));
			}
		});

	}

	/**
	 * Listener for mode change.
	 */
	public interface OnModeChangeListener {
		/**
		 * Callback called in case of mode change.
		 *
		 * @param mode The new mode.
		 */
		void onModeChange(int mode);
	}

}
