package de.jeisfeld.lut.app.util;

import android.app.Activity;
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
	private double mMinChange = 0.1; // MAGIC_NUMBER

	/**
	 * The activity.
	 */
	private final Activity mActivity;
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
	 * @param activity The activity.
	 * @param listener The listener.
	 */
	public AccelerationListener(final Activity activity, final AccelerationSensorListener listener) {
		mActivity = activity;
		mSensorManager = (SensorManager) mActivity.getSystemService(Context.SENSOR_SERVICE);
		mListener = listener;
	}

	/**
	 * Register the sensor event listener.
	 */
	public void register() {
		Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		// Logic to differentiate between TabS6 and S10e
		if ("qualcomm".equals(sensor.getVendor())) {
			mMinChange = 0.01; // MAGIC_NUMBER
		}
		mSensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
	}

	/**
	 * Unregister the sensor event listener.
	 */
	public void unregister() {
		mSensorManager.unregisterListener(this);
	}

	// OVERRIDABLE
	@Override
	public void onSensorChanged(final SensorEvent event) {
		float[] values = event.values;
		float value = (float) Math.sqrt(values[0] * values[0] + values[1] * values[1] + values[2] * values[2]);
		if (value > mMinChange && mListener != null) {
			mListener.onAccelerate(value - mMinChange);
		}
	}

	@Override
	public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
	}

	/**
	 * Callback of acceleration sensor.
	 */
	public interface AccelerationSensorListener {
		/**
		 * Callback message on acceleration.
		 *
		 * @param acceleration The acceleration.
		 */
		void onAccelerate(double acceleration);
	}
}
