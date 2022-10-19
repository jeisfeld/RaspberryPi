package de.jeisfeld.lut.app.util;

import java.lang.ref.WeakReference;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.ViewModelProvider;
import de.jeisfeld.lut.app.Application;
import de.jeisfeld.lut.app.MainActivity;
import de.jeisfeld.lut.app.ui.control.Lob0ViewModel;
import de.jeisfeld.lut.app.ui.control.Lob1ViewModel;
import de.jeisfeld.lut.app.ui.control.PulseTrigger;
import de.jeisfeld.lut.app.ui.control.Tadel0ViewModel;
import de.jeisfeld.lut.app.ui.control.Tadel1ViewModel;

/**
 * Receiver for broadcasts for other applications.
 */
public class ExternalTriggerReceiver extends BroadcastReceiver {
	/**
	 * The triggering activity.
	 */
	private final WeakReference<MainActivity> mActivity;

	/**
	 * Constructor.
	 *
	 * @param activity The triggering activity
	 */
	public ExternalTriggerReceiver(final MainActivity activity) {
		mActivity = new WeakReference<>(activity);
	}

	@Override
	public final void onReceive(final Context context, final Intent intent) {
		String action = intent.getAction();

		if ("de.jeisfeld.randomimage.DISPLAY_RANDOM_IMAGE".equals(action)) {
			String listName = intent.getStringExtra("de.jeisfeld.randomimage.listName");
			String fileName = intent.getStringExtra("de.jeisfeld.randomimage.fileName");
			int backgroundColor = intent.getIntExtra("de.jeisfeld.randomimage.backgroundColor", 0);
			boolean isStop = intent.getBooleanExtra("de.jeisfeld.randomimage.isStop", false);
			if (isStop) {
				Log.d(Application.TAG, "Stop displaying random image" + listName);
			}
			else {
				Log.d(Application.TAG, "Display random image: " + listName + " - " + fileName + " - " + backgroundColor);
				triggerBluetoothMessageOnExternalTrigger(PulseTrigger.RANDOM_IMAGE_DISPLAY, true);
			}
		}
		else if ("de.jeisfeld.randomimage.DISPLAY_NOTIFICATION".equals(action)) {
			String listName = intent.getStringExtra("de.jeisfeld.randomimage.listName");
			String fileName = intent.getStringExtra("de.jeisfeld.randomimage.fileName");
			int notificationStyle = intent.getIntExtra("de.jeisfeld.randomimage.notificationStyle", 0);
			boolean isVibrate = intent.getBooleanExtra("de.jeisfeld.randomimage.isVibrate", false);
			Log.d(Application.TAG, "Notify random image: " + listName + " - " + fileName + " - " + notificationStyle + " - " + isVibrate);
		}
		else if ("de.jeisfeld.breathtraining.BREATH_EXERCISE".equals(action)) {
			String playStatus = intent.getStringExtra("de.jeisfeld.breathTraining.playStatus");
			String stepType = intent.getStringExtra("de.jeisfeld.breathTraining.stepType");
			long duration = intent.getLongExtra("de.jeisfeld.breathTraining.duration", 0);
			Log.d(Application.TAG, "Breath training: " + playStatus + " - " + stepType + " - " + duration);
			triggerBluetoothMessageOnExternalTrigger(PulseTrigger.BREATH_TRAINING_HOLD, "HOLD".equals(stepType) && "PLAYING".equals(playStatus));
		}
		else if ("de.jeisfeld.dsmessenger.TRIGGER_LUT".equals(action)) {
			String messageType = intent.getStringExtra("de.jeisfeld.dsmessenger.lut.messageType");
			double powerFactor = intent.getDoubleExtra("de.jeisfeld.dsmessenger.lut.powerFactor", 1);
			long duration = intent.getLongExtra("de.jeisfeld.dsmessenger.lut.duration", -1);
			Log.d(Application.TAG, "DS Messenger: " + messageType + " - " + powerFactor + " - " + duration);
			if ("PULSE".equals(messageType)) {
				triggerBluetoothMessageOnExternalTrigger(PulseTrigger.DSMESSENGER, true, duration, powerFactor);
			}
			else {
				triggerBluetoothMessageOnExternalTrigger(PulseTrigger.DSMESSENGER, "ON".equals(messageType), Long.MAX_VALUE, 1);
			}
		}
	}

	/**
	 * Write bluetooth message to trigger pulse based on external trigger, if applicable.
	 *
	 * @param pulseTrigger The pulse trigger.
	 * @param isHighPower  true for setting pulse, false for stopping pulse.
	 */
	private void triggerBluetoothMessageOnExternalTrigger(final PulseTrigger pulseTrigger, final boolean isHighPower) {
		MainActivity activity = mActivity.get();
		if (activity != null) {
			new ViewModelProvider(activity).get(Lob0ViewModel.class).writeBluetoothMessageOnExternalTrigger(pulseTrigger, isHighPower);
			new ViewModelProvider(activity).get(Lob1ViewModel.class).writeBluetoothMessageOnExternalTrigger(pulseTrigger, isHighPower);
			new ViewModelProvider(activity).get(Tadel0ViewModel.class).writeBluetoothMessageOnExternalTrigger(pulseTrigger, isHighPower);
			new ViewModelProvider(activity).get(Tadel1ViewModel.class).writeBluetoothMessageOnExternalTrigger(pulseTrigger, isHighPower);
		}
	}

	/**
	 * Write bluetooth message to trigger pulse based on external trigger, if applicable.
	 *
	 * @param pulseTrigger The pulse trigger.
	 * @param isHighPower  true for setting pulse, false for stopping pulse.
	 * @param duration     The duration of the pulse.
	 * @param powerFactor A factor that is multipled with the power.
	 */
	private void triggerBluetoothMessageOnExternalTrigger(final PulseTrigger pulseTrigger, final boolean isHighPower,
														  final long duration, final double powerFactor) {
		MainActivity activity = mActivity.get();
		if (activity != null) {
			new ViewModelProvider(activity).get(Lob0ViewModel.class).writeBluetoothMessageOnExternalTrigger(
					pulseTrigger, isHighPower, duration, powerFactor);
			new ViewModelProvider(activity).get(Lob1ViewModel.class).writeBluetoothMessageOnExternalTrigger(
					pulseTrigger, isHighPower, duration, powerFactor);
			new ViewModelProvider(activity).get(Tadel0ViewModel.class).writeBluetoothMessageOnExternalTrigger(
					pulseTrigger, isHighPower, duration, powerFactor);
			new ViewModelProvider(activity).get(Tadel1ViewModel.class).writeBluetoothMessageOnExternalTrigger(
					pulseTrigger, isHighPower, duration, powerFactor);
		}
	}

}
