package de.jeisfeld.lut.app.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Receiver for broadcasts for other applications.
 */
public class ExternalTriggerReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		if ("de.jeisfeld.randomimage.DISPLAY_RANDOM_IMAGE".equals(action)) {
			String listName = intent.getStringExtra("de.jeisfeld.randomimage.listName");
			String fileName = intent.getStringExtra("de.jeisfeld.randomimage.fileName");
			int backgroundColor = intent.getIntExtra("de.jeisfeld.randomimage.backgroundColor", 0);
			boolean isStop = intent.getBooleanExtra("de.jeisfeld.randomimage.isStop", false);
			if (isStop) {
				Logger.log("Stop displaying random image" + listName);
			}
			else {
				Logger.log("Display random image: " + listName + " - " + fileName + " - " + backgroundColor);
			}
		}
		else if ("de.jeisfeld.randomimage.DISPLAY_NOTIFICATION".equals(action)) {
			String listName = intent.getStringExtra("de.jeisfeld.randomimage.listName");
			String fileName = intent.getStringExtra("de.jeisfeld.randomimage.fileName");
			int notificationStyle = intent.getIntExtra("de.jeisfeld.randomimage.notificationStyle", 0);
			boolean isVibrate = intent.getBooleanExtra("de.jeisfeld.randomimage.isVibrate", false);
			Logger.log("Notify random image: " + listName + " - " + fileName + " - " + notificationStyle + " - " + isVibrate);
		}
		else if ("de.jeisfeld.breathtraining.BREATH_EXERCISE".equals(action)) {
			String playStatus = intent.getStringExtra("de.jeisfeld.breathTraining.playStatus");
			String stepType = intent.getStringExtra("de.jeisfeld.breathTraining.stepType");
			long duration = intent.getLongExtra("de.jeisfeld.breathTraining.duration", 0);
			Logger.log("Breath training: " + playStatus + " - " + stepType + " - " + duration);
		}

		// TODO: trigger LuT actions based on received messages
	}
}
