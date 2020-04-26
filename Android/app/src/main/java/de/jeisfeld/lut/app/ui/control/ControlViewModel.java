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
	 * The max value of the seekbars.
	 */
	protected static final int MIN_POWER_SEEKBAR_MAX_VALUE = 255;

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
	private final MutableLiveData<Integer> mPower = new MutableLiveData<>();
	/**
	 * The min power value.
	 */
	private final MutableLiveData<Integer> mMinPower = new MutableLiveData<>();
	/**
	 * The cycle length.
	 */
	private final MutableLiveData<Integer> mCycleLength = new MutableLiveData<>();
	/**
	 * The frequency.
	 */
	private final MutableLiveData<Integer> mFrequency = new MutableLiveData<>();
	/**
	 * The running probability.
	 */
	private final MutableLiveData<Double> mRunningProbability = new MutableLiveData<>();
	/**
	 * The average off duration.
	 */
	private final MutableLiveData<Long> mAvgOffDuration = new MutableLiveData<>();
	/**
	 * The average off duration.
	 */
	private final MutableLiveData<Long> mAvgOnDuration = new MutableLiveData<>();

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
		mIsActive.postValue(processingBluetoothMessage.isActive());
		if (processingBluetoothMessage.getMode() != null) {
			mMode.postValue(isTadel()
					? Mode.fromTadelValue(processingBluetoothMessage.getMode())
					: Mode.fromLobValue(processingBluetoothMessage.getMode()));
		}
		if (processingBluetoothMessage.getPower() != null) {
			mPower.postValue(processingBluetoothMessage.getPower());
		}
		if (processingBluetoothMessage.getMinPower() != null) {
			mMinPower.postValue(processingBluetoothMessage.getMinPower());
		}
		if (processingBluetoothMessage.getCycleLength() != null) {
			mCycleLength.postValue(processingBluetoothMessage.getCycleLength());
		}
		if (processingBluetoothMessage.getFrequency() != null) {
			mFrequency.postValue(processingBluetoothMessage.getFrequency());
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
				mIsActive.getValue() == null ? false : mIsActive.getValue(),
				mPower.getValue(), mFrequency.getValue(), null,
				mMode.getValue() == null ? 0 : (isTadel() ? mMode.getValue().getTadelValue() : mMode.getValue().getLobValue()),
				mMinPower.getValue(), mCycleLength.getValue(), mRunningProbability.getValue(),
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
	 * @param minPowerSeekbarValue The new value of minPower seekbar
	 */
	protected void updatePower(final int power, final int minPowerSeekbarValue) {
		mPower.setValue(power);
		mMinPower.setValue((int) Math.round((double) minPowerSeekbarValue * power / MIN_POWER_SEEKBAR_MAX_VALUE));
		writeBluetoothMessage();
	}

	/**
	 * Calculate min power seekbar value from min power.
	 *
	 * @param minPower The min power.
	 * @return The seekbar value.
	 */
	protected int getMinPowerSeekbarValue(final int minPower) {
		return (mPower.getValue() == null || mPower.getValue() == 0) ? 0
				: (int) Math.round(Math.min(MIN_POWER_SEEKBAR_MAX_VALUE, (double) minPower * MIN_POWER_SEEKBAR_MAX_VALUE / mPower.getValue()));
	}

	/**
	 * Get the min power value.
	 *
	 * @return The min power value.
	 */
	protected MutableLiveData<Integer> getMinPower() {
		return mMinPower;
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
	 * Update the frequency.
	 *
	 * @param frequency The new frequency.
	 */
	protected void updateFrequency(final int frequency) {
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
		return Math.round(1000 * Math.exp(0.016 * seekbarValue)); // MAGIC_NUMBER
	}

	/**
	 * Convert value in ms to seekbar value for avg duration.
	 *
	 * @param value the value in ms
	 * @return The seekbar value
	 */
	protected static int avgDurationValueToSeekbar(final long value) {
		return (int) Math.round(Math.log(value / 1000.0) / 0.016); // MAGIC_NUMBER
	}
}
