package de.jeisfeld.pi.lut;

import java.io.IOException;

import de.jeisfeld.lut.bluetooth.message.ButtonStatusMessage;
import de.jeisfeld.lut.bluetooth.message.FreeTextMessage;
import de.jeisfeld.lut.bluetooth.message.Message;
import de.jeisfeld.lut.bluetooth.message.ProcessingStatusMessage;
import de.jeisfeld.lut.bluetooth.message.StandaloneStatusMessage;
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
	 * The RandomizedLob instances.
	 */
	private static RandomizedLob[] mLobs;
	/**
	 * The RandomizedTadel instances.
	 */
	private static RandomizedTadel[] mTadels;
	/**
	 * Flag indicating if the standalone processing is active.
	 */
	private static boolean mIsStandaloneActive;

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
				Message message = null;
				try {
					message = Message.fromString(data);
				}
				catch (Exception e) {
					Logger.error(e);
				}
				if (message == null) {
					Logger.error(new RuntimeException("Received unspecified bluetooth message: " + data));
				}
				else {
					switch (message.getType()) {
					case CONNECTED:
						connectThread.write(new ProcessingStatusMessage(mChannel, mIsTadel, false, null, null, null, mMode, "", ""));
						connectThread.write(new StandaloneStatusMessage(mIsStandaloneActive));
						break;
					case PING:
						Logger.info("Ping");
						break;
					case FREE_TEXT:
						Logger.log("Received free text message: " + ((FreeTextMessage) message).getText());
						break;
					case STANDALONE_STATUS:
						if (((StandaloneStatusMessage) message).isActive()) {
							if (!mIsStandaloneActive) {
								mChannel = 0;
								mIsTadel = false;
								mMode = 0;
								new Thread(mLobs[mChannel]).start();
								mIsStandaloneActive = true;
							}
						}
						else {
							for (RandomizedLob lob : mLobs) {
								lob.stop();
							}
							for (RandomizedTadel tadel : mTadels) {
								tadel.stop();
							}
							mIsStandaloneActive = false;
						}
						break;
					case PROCESSING_STATUS:
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
			public void onModeDetails(final boolean isActive, final Integer power, final Integer frequency, final Integer wave,
					final Integer mode, final String modeName, final String details) {
				connectThread.write(new ProcessingStatusMessage(mChannel, mIsTadel, isActive, power, frequency, wave, mode, modeName, details));
			}
		};

		mLobs = new RandomizedLob[] {new RandomizedLob(0, listener), new RandomizedLob(1, listener)};
		mTadels = new RandomizedTadel[] {new RandomizedTadel(0, listener), new RandomizedTadel(1, listener)};

		Sender sender = Sender.getInstance();
		sender.setButton2LongPressListener(new ShutdownListener(4000)); // MAGIC_NUMBER

		sender.setButton2Listener(new ButtonListener() {
			@Override
			public void handleButtonDown() {
				mChannel = (mChannel + 1) % 2;

				if (mIsTadel) {
					for (RandomizedTadel tadel : mTadels) {
						tadel.stop();
					}
					mLobs[mChannel].signal(2, true);
					new Thread(mTadels[mChannel]).start();
				}
				else {
					for (RandomizedLob lob : mLobs) {
						lob.stop();
					}
					mLobs[mChannel].signal(1, true);
					new Thread(mLobs[mChannel]).start();
				}
				listener.onModeDetails(false, null, null, null, 0, "", "");
			}
		});

		sender.setButton1LongPressListener(new OnLongPressListener() {
			@Override
			public void handleLongTrigger() {
				if (mIsTadel) {
					for (RandomizedTadel tadel : mTadels) {
						tadel.stop();
					}
					mLobs[mChannel].signal(1, true);
					new Thread(mLobs[mChannel]).start();
					mIsTadel = false;
				}
				else {
					for (RandomizedLob lob : mLobs) {
						lob.stop();
					}
					mLobs[mChannel].signal(2, true);
					new Thread(mTadels[mChannel]).start();
					mIsTadel = true;
				}
				listener.onModeDetails(false, null, null, null, 0, "", "");
			}
		});

		new Thread(mLobs[mChannel]).start();
		mIsStandaloneActive = true;

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
		 * Callback called to report mode details.
		 *
		 * @param isActive Flag if power is active.
		 * @param power The power.
		 * @param frequency The frequency.
		 * @param wave The wave.
		 * @param mode The mode.
		 * @param modeName The mode name.
		 * @param details mode details.
		 */
		void onModeDetails(boolean isActive, Integer power, Integer frequency, Integer wave, Integer mode, String modeName, String details);
	}

}
