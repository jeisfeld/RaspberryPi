package de.jeisfeld.lut.app.ui.status;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import de.jeisfeld.lut.bluetooth.message.ButtonStatusMessage;
import de.jeisfeld.lut.bluetooth.message.ProcessingModeMessage;

/**
 * The view model for the fragment.
 */
public class StatusViewModel extends ViewModel {
	/**
	 * The status of bluetooth connection.
	 */
	private final MutableLiveData<Boolean> mStatusBluetooth = new MutableLiveData<>();
	/**
	 * The status message.
	 */
	private final MutableLiveData<String> mStatusMessage = new MutableLiveData<>();
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
	 * The status message.
	 */
	private final MutableLiveData<String> mProcessingMode = new MutableLiveData<>();

	/**
	 * Constructor.
	 */
	public StatusViewModel() {
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
		mStatusMessage.postValue("Control Status: "
				+ statusMessage.getControl1Value() + "," + statusMessage.getControl2Value() + "," + statusMessage.getControl3Value());
	}

	/**
	 * Set the status from processing mode message.
	 *
	 * @param processingModeMessage The processing mode message.
	 */
	public void setProcessingMode(final ProcessingModeMessage processingModeMessage) {
		String processingMode = "Channel: " + processingModeMessage.getChannel() + "\n"
				+ "Type: " + (processingModeMessage.isTadel() ? "Tadel" : "Lob") + "\n"
				+ "Mode: " + processingModeMessage.getMode();
		mProcessingMode.postValue(processingMode);
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
	 * Get the status message.
	 *
	 * @return The status message.
	 */
	public final LiveData<String> getStatusMessage() {
		return mStatusMessage;
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
		return mProcessingMode;
	}
}
