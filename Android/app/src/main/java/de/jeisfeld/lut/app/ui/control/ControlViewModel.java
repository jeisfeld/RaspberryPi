package de.jeisfeld.lut.app.ui.control;

import java.lang.ref.WeakReference;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import de.jeisfeld.lut.app.MainActivity;
import de.jeisfeld.lut.bluetooth.message.Message;
import de.jeisfeld.lut.bluetooth.message.ProcessingBluetoothMessage;

/**
 * The view model for the fragment.
 */
public abstract class ControlViewModel extends ViewModel {
	/**
	 * The min duration for increasing tadel power by 1.
	 */
	protected static final int MIN_POWER_STEP_DURATION = 200;

	/**
	 * A reference to the starting activity.
	 */
	private WeakReference<MainActivity> mMainActivity;
	/**
	 * The status of bluetooth connection.
	 */
	private final MutableLiveData<Boolean> mStatusBluetooth = new MutableLiveData<>(false);
	/**
	 * The status of standalone processing.
	 */
	private final MutableLiveData<Boolean> mIsActive = new MutableLiveData<>(false);
	/**
	 * The mode.
	 */
	private final MutableLiveData<Mode> mMode = new MutableLiveData<>(Mode.OFF);
	/**
	 * The power.
	 */
	private final MutableLiveData<Integer> mPower = new MutableLiveData<>(0);
	/**
	 * The min power value.
	 */
	private final MutableLiveData<Double> mMinPower = new MutableLiveData<>(0.0);
	/**
	 * The min power value.
	 */
	private final MutableLiveData<Long> mPowerChangeDuration = new MutableLiveData<>(0L);
	/**
	 * The cycle length.
	 */
	private final MutableLiveData<Integer> mCycleLength = new MutableLiveData<>(0);
	/**
	 * The frequency.
	 */
	private final MutableLiveData<Integer> mFrequency = new MutableLiveData<>(32);
	/**
	 * The wave.
	 */
	private final MutableLiveData<Wave> mWave = new MutableLiveData<>(Wave.CONSTANT);
	/**
	 * The running probability.
	 */
	private final MutableLiveData<Double> mRunningProbability = new MutableLiveData<>(0.5);
	/**
	 * The average off duration.
	 */
	private final MutableLiveData<Long> mAvgOffDuration = new MutableLiveData<>(1000L);
	/**
	 * The average off duration.
	 */
	private final MutableLiveData<Long> mAvgOnDuration = new MutableLiveData<>(1000L);

	/**
	 * Constructor.
	 */
	public ControlViewModel() {
	}

	/**
	 * Get the channel.
	 *
	 * @return The channel.
	 */
	protected abstract int getChannel();

	/**
	 * Get the flag indicating if it is lob or tadel.
	 *
	 * @return Flag indicating if it is lob or tadel
	 */
	protected abstract boolean isTadel();

	/**
	 * Set the activity.
	 *
	 * @param activity The activity.
	 */
	protected void setActivity(final MainActivity activity) {
		mMainActivity = new WeakReference<>(activity);
	}

	/**
	 * Set the status from processing status message.
	 *
	 * @param processingBluetoothMessage The processing status message.
	 */
	public void setProcessingStatus(final ProcessingBluetoothMessage processingBluetoothMessage) {
		if (processingBluetoothMessage.isActive() != null) {
			mIsActive.postValue(processingBluetoothMessage.isActive());
		}
		if (processingBluetoothMessage.getMode() != null) {
			mMode.postValue(isTadel()
					? Mode.fromTadelValue(processingBluetoothMessage.getMode())
					: Mode.fromLobValue(processingBluetoothMessage.getMode()));
		}
		if (processingBluetoothMessage.getPower() != null && mMode.getValue() != Mode.OFF) {
			mPower.postValue(processingBluetoothMessage.getPower());
		}
		if (processingBluetoothMessage.getMinPower() != null) {
			mMinPower.postValue(processingBluetoothMessage.getMinPower());
		}
		if (processingBluetoothMessage.getPowerChangeDuration() != null) {
			mPowerChangeDuration.postValue(processingBluetoothMessage.getPowerChangeDuration());
		}
		if (processingBluetoothMessage.getCycleLength() != null) {
			mCycleLength.postValue(processingBluetoothMessage.getCycleLength());
		}
		if (processingBluetoothMessage.getFrequency() != null) {
			mFrequency.postValue(processingBluetoothMessage.getFrequency());
		}
		if (processingBluetoothMessage.getWave() != null) {
			mWave.postValue(Wave.fromTadelValue(processingBluetoothMessage.getWave()));
		}
		if (processingBluetoothMessage.getRunningProbability() != null) {
			mRunningProbability.postValue(processingBluetoothMessage.getRunningProbability());
		}
		if (processingBluetoothMessage.getAvgOffDuration() != null) {
			mAvgOffDuration.postValue(processingBluetoothMessage.getAvgOffDuration());
		}
		if (processingBluetoothMessage.getAvgOnDuration() != null) {
			mAvgOnDuration.postValue(processingBluetoothMessage.getAvgOnDuration());
		}
	}

	/**
	 * Write a bluetooth message.
	 *
	 * @param message The message to be written.
	 */
	private void writeBluetoothMessage(final Message message) {
		MainActivity activity = mMainActivity.get();
		if (activity != null) {
			activity.writeBluetoothMessage(message);
		}
	}

	/**
	 * Write the current data via bluetooth.
	 */
	private void writeBluetoothMessage() {
		ProcessingBluetoothMessage message = new ProcessingBluetoothMessage(getChannel(), isTadel(),
				mIsActive.getValue(), mPower.getValue(), mFrequency.getValue(),
				isTadel() && mWave.getValue() != null ? mWave.getValue().getTadelValue() : null,
				mMode.getValue() == null ? 0 : (isTadel() ? mMode.getValue().getTadelValue() : mMode.getValue().getLobValue()),
				mMinPower.getValue(), null, mPowerChangeDuration.getValue(),
				mCycleLength.getValue(), mRunningProbability.getValue(),
				mAvgOffDuration.getValue(), mAvgOnDuration.getValue());
		writeBluetoothMessage(message);
	}

	/**
	 * Set the bluetooth status.
	 *
	 * @param status The bluetooth status.
	 */
	public void setBluetoothStatus(final boolean status) {
		mStatusBluetooth.postValue(status);
	}

	/**
	 * Get the bluetooth status.
	 *
	 * @return The bluetooth status.
	 */
	public final LiveData<Boolean> getStatusBluetooth() {
		return mStatusBluetooth;
	}

	/**
	 * Update the standalone status of the device.
	 *
	 * @param isActive Flag indicating if standalone status should be set active or inactive.
	 */
	protected void updateActiveStatus(final boolean isActive) {
		mIsActive.setValue(isActive);
		writeBluetoothMessage();
	}

	/**
	 * Get the active status.
	 *
	 * @return The active status.
	 */
	public final LiveData<Boolean> getIsActive() {
		return mIsActive;
	}

	/**
	 * Get the mode.
	 *
	 * @return The mode.
	 */
	public final LiveData<Mode> getMode() {
		return mMode;
	}

	/**
	 * Update the mode.
	 *
	 * @param mode The new mode.
	 */
	protected void updateMode(final Mode mode) {
		mMode.setValue(mode);
		if (isTadel() && mode == Mode.OFF) {
			mPower.setValue(0);
		}
		writeBluetoothMessage();
	}

	/**
	 * Get the wave.
	 *
	 * @return The wave.
	 */
	public final LiveData<Wave> getWave() {
		return mWave;
	}

	/**
	 * Update the wave.
	 *
	 * @param wave The new wave.
	 */
	protected void updateWave(final Wave wave) {
		mWave.setValue(wave);
		writeBluetoothMessage();
	}

	/**
	 * Get the power.
	 *
	 * @return The power.
	 */
	protected MutableLiveData<Integer> getPower() {
		return mPower;
	}

	/**
	 * Update the power.
	 *
	 * @param power The new power.
	 */
	protected void updatePower(final int power) {
		int oldPower = mPower.getValue() == null ? 0 : mPower.getValue();
		mPower.setValue(isTadel() && power > oldPower ? oldPower : power);
		writeBluetoothMessage();
	}

	/**
	 * Get the min power value.
	 *
	 * @return The min power value.
	 */
	protected MutableLiveData<Double> getMinPower() {
		return mMinPower;
	}

	/**
	 * Update the min power.
	 *
	 * @param minPower The new min power.
	 */
	protected void updateMinPower(final double minPower) {
		mMinPower.setValue(minPower);
		writeBluetoothMessage();
	}

	/**
	 * Get the power change duration.
	 *
	 * @return The power change duration.
	 */
	protected MutableLiveData<Long> getPowerChangeDuration() {
		return mPowerChangeDuration;
	}

	/**
	 * Update the power change duration.
	 *
	 * @param powerChangeDurationSeekbarValue The power change duration seekbar value.
	 */
	protected void updatePowerChangeDuration(final int powerChangeDurationSeekbarValue) {
		long powerChangeDuration = (int) (599400 / Math.pow(1.04, powerChangeDurationSeekbarValue - 20)); // MAGIC_NUMBER
		mPowerChangeDuration.setValue(powerChangeDurationSeekbarValue < 20 ? 0 : powerChangeDuration); // MAGIC_NUMBER
		writeBluetoothMessage();
	}

	/**
	 * Convert value in ms to seekbar value for power change duration.
	 *
	 * @param powerChangeDuration the power change duration in ms
	 * @return The seekbar value
	 */
	protected static int getPowerChangeDurationSeekbarValue(final long powerChangeDuration) {
		if (powerChangeDuration == 0) {
			return 0; // MAGIC_NUMBER
		}
		return (int) Math.min(255, 20 + Math.log(599400f / powerChangeDuration) / Math.log(1.04)); // MAGIC_NUMBER
	}

	/**
	 * Get the cycle length.
	 *
	 * @return The cycle length.
	 */
	protected MutableLiveData<Integer> getCycleLength() {
		return mCycleLength;
	}

	/**
	 * Update the cycle length.
	 *
	 * @param cycleLength The new cycle length.
	 */
	protected void updateCycleLength(final int cycleLength) {
		mCycleLength.setValue(cycleLength);
		writeBluetoothMessage();
	}

	/**
	 * Get the frequency.
	 *
	 * @return The frequency.
	 */
	protected MutableLiveData<Integer> getFrequency() {
		return mFrequency;
	}

	/**
	 * Convert value to seekbar value for frequency.
	 *
	 * @param frequency the frequency
	 * @return The seekbar value
	 */
	protected static int getFrequencySeekbarValue(final int frequency) {
		if (frequency == 0) {
			return 1000; // MAGIC_NUMBER
		}
		return (int) (1000 * Math.pow(frequency / 256.0, 2.0 / 3.0)); // MAGIC_NUMBER
	}

	/**
	 * Update the frequency.
	 *
	 * @param frequencySeekbarValue The seekbar value of the new frequency.
	 */
	protected void updateFrequency(final int frequencySeekbarValue) {
		int frequency = 0;
		if (frequencySeekbarValue < 1000) { // MAGIC_NUMBER
			frequency = (int) (Math.pow(frequencySeekbarValue / 1000.0, 1.5) * 256); // MAGIC_NUMBER
			if (frequency < 2) {
				frequency = 2;
			}
		}
		mFrequency.setValue(frequency);
		writeBluetoothMessage();
	}

	/**
	 * Get the running probability.
	 *
	 * @return The running probability.
	 */
	protected MutableLiveData<Double> getRunningProbability() {
		return mRunningProbability;
	}

	/**
	 * Update the running probability.
	 *
	 * @param runningProbability The new running probability
	 */
	protected void updateRunningProbability(final double runningProbability) {
		mRunningProbability.setValue(runningProbability);
		writeBluetoothMessage();
	}

	/**
	 * Get the average off duration.
	 *
	 * @return The average off duration.
	 */
	protected MutableLiveData<Long> getAvgOffDuration() {
		return mAvgOffDuration;
	}

	/**
	 * Update the average off duration.
	 *
	 * @param avgOffDuration The new average off duration
	 */
	protected void updateAvgOffDuration(final long avgOffDuration) {
		mAvgOffDuration.setValue(avgOffDuration);
		writeBluetoothMessage();
	}

	/**
	 * Get the average on duration.
	 *
	 * @return The average on duration.
	 */
	protected MutableLiveData<Long> getAvgOnDuration() {
		return mAvgOnDuration;
	}

	/**
	 * Update the average on duration.
	 *
	 * @param avgOnDuration The new average on duration
	 */
	protected void updateAvgOnDuration(final long avgOnDuration) {
		mAvgOnDuration.setValue(avgOnDuration);
		writeBluetoothMessage();
	}

	/**
	 * Convert seekbar value to value in ms for avg duration.
	 *
	 * @param seekbarValue the seekbar value
	 * @return The value
	 */
	protected static long avgDurationSeekbarToValue(final int seekbarValue) {
		return Math.round(1000 * Math.exp(0.025 * seekbarValue)); // MAGIC_NUMBER
	}

	/**
	 * Convert value in ms to seekbar value for avg duration.
	 *
	 * @param value the value in ms
	 * @return The seekbar value
	 */
	protected static int avgDurationValueToSeekbar(final long value) {
		return (int) Math.round(Math.log(value / 1000.0) / 0.025); // MAGIC_NUMBER
	}
}
