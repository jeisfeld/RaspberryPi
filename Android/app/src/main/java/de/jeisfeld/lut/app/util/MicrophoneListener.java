package de.jeisfeld.lut.app.util;

import java.util.ArrayList;
import java.util.List;

import android.Manifest.permission;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;

import androidx.core.content.ContextCompat;
import de.jeisfeld.lut.app.R;

/**
 * The microphone input event listener.
 */
public class MicrophoneListener {
	/**
	 * The sample rate.
	 */
	private static final int RECORDER_SAMPLERATE = 44100;
	/**
	 * The recorder channel.
	 */
	private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
	/**
	 * The audio encoding.
	 */
	private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
	/**
	 * The min microphone value considered as change.
	 */
	private double mMinChange = 20; // MAGIC_NUMBER

	/**
	 * The activity.
	 */
	private final Activity mActivity;
	/**
	 * The listener.
	 */
	private final MicrophoneInputListener mListener;
	/**
	 * The audio recorder.
	 */
	private AudioRecord mRecorder = null;
	/**
	 * The recording thread.
	 */
	private Thread mRecordingThread = null;
	/**
	 * Flag checking if the listener ist stopped.
	 */
	private boolean mIsStopped = false;

	/**
	 * Constructor.
	 *
	 * @param activity The activity.
	 * @param listener The listener.
	 */
	public MicrophoneListener(final Activity activity, final MicrophoneInputListener listener) {
		mActivity = activity;
		mListener = listener;
	}

	/**
	 * Start listening.
	 */
	public void start() {
		if (ContextCompat.checkSelfPermission(mActivity, permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
			DialogUtil.displayToast(mActivity, R.string.toast_permissions_missing);
			return;
		}
		final int bufferElements2Rec = 1024; // MAGIC_NUMBER
		final int bufferQuantile = 896; // MAGIC_NUMBER
		final int bytesPerElement = 2; // 2 bytes in 16bit format
		mRecorder = new AudioRecord(AudioSource.MIC,
				RECORDER_SAMPLERATE, RECORDER_CHANNELS,
				RECORDER_AUDIO_ENCODING, bufferElements2Rec * bytesPerElement);
		String productName = mRecorder.getRoutedDevice().getProductName().toString();

		// Logic to differentiate between TabS6 and S10e
		switch (productName) {
		case "SM-T865":
			mMinChange = 60; // MAGIC_NUMBER
			break;
		case "SM-G970F":
			mMinChange = 20; // MAGIC_NUMBER
			break;
		case "h2w":
			mMinChange = 40; // MAGIC_NUMBER
			break;
		default:
			mMinChange = 20; // MAGIC_NUMBER
		}

		mRecorder.startRecording();
		mRecordingThread = new Thread(() -> {
			short[] sData = new short[bufferElements2Rec];
			while (!mIsStopped) {
				mRecorder.read(sData, 0, bufferElements2Rec);
				List<Integer> values = new ArrayList<>();
				for (short sDatum : sData) {
					values.add(Math.abs(sDatum));
				}
				values.sort(Integer::compareTo);
				double value = values.get(bufferQuantile);

				if (mListener != null) {
					value = value > mMinChange ? Math.sqrt(value - mMinChange) / 15.0 : 0; // MAGIC_NUMBER
					mListener.onMicrophoneInput(value);
				}
			}
		}, "AudioRecorder Thread");
		mRecordingThread.start();
	}

	/**
	 * Stop listening.
	 */
	public void stop() {
		mIsStopped = true;
		if (mRecorder != null) {
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;
		}
		mRecordingThread = null;
	}

	/**
	 * Callback of microphone input sensor.
	 */
	public interface MicrophoneInputListener {
		/**
		 * Callback message on microphone input.
		 *
		 * @param input The microphone input.
		 */
		void onMicrophoneInput(double input);
	}
}
