package de.jeisfeld.lut.app.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * The acceleration sensor event listener.
 */
public class AccelerationListener implements SensorEventListener {
	/**
	 * The min accelerometer value considered as change.
	 */
	private static final double MIN_CHANGE = 0.1;

	/**
	 * The context.
	 */
	private final Context mContext;
	/**
	 * The sensor manager.
	 */
	private final SensorManager mSensorManager;
	/**
	 * The listener.
	 */
	private final AccelerationSensorListener mListener;

	/**
	 * Constructor.
	 *
	 * @param context The context.
	 * @param listener The listener.
	 */
	public AccelerationListener(final Context context, final AccelerationSensorListener listener) {
		mContext = context;
		mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
		mListener = listener;
	}

	/**
	 * Register the sensor event listener.
	 */
	public void register() {
		Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
	}

	/**
	 * Unregister the sensor event listener.
	 */
	public void unregister() {
		mSensorManager.unregisterListener(this);
	}


	@Override
	public void onSensorChanged(SensorEvent event) {
		float[] values = event.values;
		float value = (float) Math.sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2]);
		if (value > MIN_CHANGE && mListener != null) {
			mListener.onAccelerate(value - MIN_CHANGE);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	/**
	 * Callback of acceleration sensor.
	 */
	public interface AccelerationSensorListener {
		/**
		 * Callback message on acceleration.
		 * @param acceleration The acceleration.
		 */
		void onAccelerate(double acceleration);
	}
}
