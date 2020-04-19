package de.jeisfeld.lut.app.ui.gallery;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * The view model for the fragment.
 */
public class GalleryViewModel extends ViewModel {
	/**
	 * The display text.
	 */
	private final MutableLiveData<String> mText;

	/**
	 * Constructor.
	 */
	public GalleryViewModel() {
		mText = new MutableLiveData<>();
		mText.setValue("This is gallery fragment");
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
