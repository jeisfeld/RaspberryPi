package de.jeisfeld.lut.app.ui.slideshow;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The view model for the fragment.
 */
public class SlideshowViewModel extends ViewModel {
	/**
	 * The display text.
	 */
	private final MutableLiveData<String> mText;

	/**
	 * Constructor.
	 */
	public SlideshowViewModel() {
		mText = new MutableLiveData<>();
		mText.setValue("This is slideshow fragment");
	}

	/**
	 * Get the display text.
	 *
	 * @return The display text,
	 */
	public final LiveData<String> getText() {
		return mText;
	}
}
