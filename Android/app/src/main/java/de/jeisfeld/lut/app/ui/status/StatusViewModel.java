package de.jeisfeld.lut.app.ui.status;

import java.lang.ref.WeakReference;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import de.jeisfeld.lut.app.MainActivity;
import de.jeisfeld.lut.bluetooth.message.ButtonStatusMessage;
import de.jeisfeld.lut.bluetooth.message.Message;
import de.jeisfeld.lut.bluetooth.message.ProcessingStatusMessage;
import de.jeisfeld.lut.bluetooth.message.StandaloneStatusMessage;

/**
 * The view model for the fragment.
 */
public class StatusViewModel extends ViewModel {
	/**
	 * A reference to the starting activity.
	 */
	private WeakReference<MainActivity> mMainActivity;
	/**
	 * The status of bluetooth connection.
	 */
	private final MutableLiveData<Boolean> mStatusBluetooth = new MutableLiveData<>();
	/**
	 * The status of standalone processing.
	 */
	private final MutableLiveData<Boolean> mStatusStandalone = new MutableLiveData<>();
	/**
	 * The control status message.
	 */
	private final MutableLiveData<String> mControlStatusMessage = new MutableLiveData<>();
	/**
	 * The status of button 1.
	 */
	private final MutableLiveData<Boolean> mStatusButton1 = new MutableLiveData<>();
	/**
	 * The status of button 1.
	 */
	private final MutableLiveData<Boolean> mStatusButton2 = new MutableLiveData<>();
	/**
	 * The status of control 1.
	 */
	private final MutableLiveData<Integer> mStatusControl1 = new MutableLiveData<>();
	/**
	 * The status of control 2.
	 */
	private final MutableLiveData<Integer> mStatusControl2 = new MutableLiveData<>();
	/**
	 * The status of control 3.
	 */
	private final MutableLiveData<Integer> mStatusControl3 = new MutableLiveData<>();
	/**
	 * The processing status message.
	 */
	private final MutableLiveData<String> mProcessingStatus = new MutableLiveData<>();

	/**
	 * Constructor.
	 */
	public StatusViewModel() {
	}

	/**
	 * Set the activity.
	 *
	 * @param activity The activity.
	 */
	protected void setActivity(final MainActivity activity) {
		mMainActivity = new WeakReference<>(activity);
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
	 * Set the status from status message.
	 *
	 * @param statusMessage The status message.
	 */
	public void setStatus(final ButtonStatusMessage statusMessage) {
		mStatusButton1.postValue(statusMessage.isButton1Pressed());
		mStatusButton2.postValue(statusMessage.isButton2Pressed());
		mStatusControl1.postValue(statusMessage.getControl1Value());
		mStatusControl2.postValue(statusMessage.getControl2Value());
		mStatusControl3.postValue(statusMessage.getControl3Value());
		mControlStatusMessage.postValue("Control Status: "
				+ statusMessage.getControl1Value() + "," + statusMessage.getControl2Value() + "," + statusMessage.getControl3Value());
	}

	/**
	 * Set the status from processing mode message.
	 *
	 * @param processingStatusMessage The processing mode message.
	 */
	public void setProcessingStatus(final ProcessingStatusMessage processingStatusMessage) {
		String processingStatus = "Channel: " + processingStatusMessage.getChannel() + "\n"
				+ "Type: " + (processingStatusMessage.isTadel() ? "Tadel" : "Lob") + "\n"
				+ (processingStatusMessage.getPower() == null ? ""
						: ("Active: " + processingStatusMessage.isActive() + "\n"
								+ "Power: " + processingStatusMessage.getPower() + "\n"))
				+ (processingStatusMessage.getFrequency() == null ? "" : "Frequency: " + processingStatusMessage.getFrequency() + "\n")
				+ (processingStatusMessage.getWave() == null ? "" : "Wave: " + processingStatusMessage.getWave() + "\n")
				+ (processingStatusMessage.getMode() == null ? ""
						: "Mode: " + processingStatusMessage.getMode() + " (" + processingStatusMessage.getModeName() + ")\n")
				+ processingStatusMessage.getDetails();
		mProcessingStatus.postValue(processingStatus);
	}

	/**
	 * Update the standalone status of the device.
	 *
	 * @param isStandaloneActive Flag indicating if standalone status should be set active or inactive.
	 */
	protected void updateStandaloneStatus(final boolean isStandaloneActive) {
		writeBluetoothMessage(new StandaloneStatusMessage(isStandaloneActive));
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
	 * Set the standalone status.
	 *
	 * @param status The standalone status.
	 */
	public void setStandaloneStatus(final boolean status) {
		mStatusStandalone.postValue(status);
	}

	/**
	 * Get the standalone processing status.
	 *
	 * @return The standalone processing status.
	 */
	public final LiveData<Boolean> getStatusStandalone() {
		return mStatusStandalone;
	}

	/**
	 * Get the control status message.
	 *
	 * @return The control status message.
	 */
	public final LiveData<String> getControlStatusMessage() {
		return mControlStatusMessage;
	}

	/**
	 * Get the status of button 1.
	 *
	 * @return The status of button 1.
	 */
	public final LiveData<Boolean> getStatusButton1() {
		return mStatusButton1;
	}

	/**
	 * Get the status of button 2.
	 *
	 * @return The status of button 2.
	 */
	public final LiveData<Boolean> getStatusButton2() {
		return mStatusButton2;
	}

	/**
	 * Get the status of control 1.
	 *
	 * @return The status of control 1.
	 */
	public final LiveData<Integer> getStatusControl1() {
		return mStatusControl1;
	}

	/**
	 * Get the status of control 2.
	 *
	 * @return The status of control 2.
	 */
	public final LiveData<Integer> getStatusControl2() {
		return mStatusControl2;
	}

	/**
	 * Get the status of control 3.
	 *
	 * @return The status of control 3.
	 */
	public final LiveData<Integer> getStatusControl3() {
		return mStatusControl3;
	}

	/**
	 * Get the processing mode.
	 *
	 * @return The processing mode.
	 */
	public final LiveData<String> getProcessingMode() {
		return mProcessingStatus;
	}
}
