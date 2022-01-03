package de.jeisfeld.lut.app.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import androidx.core.app.ActivityCompat;
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
	private double mMinChange = 35;

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
		this.mActivity = activity;
		mListener = listener;
	}

	/**
	 * Start listening.
	 */
	public void start() {
		if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
			DialogUtil.displayToast(mActivity, R.string.toast_permissions_missing);
			return;
		}
		int BufferElements2Rec = 1024;
		int BytesPerElement = 2; // 2 bytes in 16bit format
		mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC,
				RECORDER_SAMPLERATE, RECORDER_CHANNELS,
				RECORDER_AUDIO_ENCODING, BufferElements2Rec * BytesPerElement);
		String productName = mRecorder.getRoutedDevice().getProductName().toString();
		// Logic to differentiate between TabS6 and S10e
		if ("SM-T865".equals(productName)) {
			mMinChange = 130;
		}
		else if ("SM-G970F".equals(productName)) {
			mMinChange = 35;
		}

		mRecorder.startRecording();
		mRecordingThread = new Thread(() -> {
			short[] sData = new short[BufferElements2Rec];
			while (!mIsStopped) {
				mRecorder.read(sData, 0, BufferElements2Rec);
				double value = 0;
				for (short sDatum : sData) {
					if (Math.abs(sDatum) >=  value) {
						 value = Math.abs(sDatum);
					}
				}

				if (value > mMinChange) {
					value = Math.sqrt(value - mMinChange) / 20.0;
					if (mListener != null) {
						mListener.onMicrophoneInput(value);
					}
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
		 * @param input The microphone input.
		 */
		void onMicrophoneInput
		(double input);
	}
}
