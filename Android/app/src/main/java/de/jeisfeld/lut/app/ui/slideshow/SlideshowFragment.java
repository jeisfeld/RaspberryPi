package de.jeisfeld.lut.app.ui.slideshow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import de.jeisfeld.lut.app.R;

public class SlideshowFragment extends Fragment {
	/**
	 * The view model.
	 */
	private SlideshowViewModel mSlideshowViewModel;

	@Override
	public final View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		mSlideshowViewModel = new ViewModelProvider(this).get(SlideshowViewModel.class);
		View root = inflater.inflate(R.layout.fragment_slideshow, container, false);
		final TextView textView = root.findViewById(R.id.text_slideshow);
		mSlideshowViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
		return root;
	}
}
