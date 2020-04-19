package de.jeisfeld.lut.app.ui.gallery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import de.jeisfeld.lut.app.R;

public class GalleryFragment extends Fragment {
	/**
	 * The view model.
	 */
	private GalleryViewModel mGalleryViewModel;

	@Override
	public final View onCreateView(@NonNull final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
		mGalleryViewModel = new ViewModelProvider(this).get(GalleryViewModel.class);
		View root = inflater.inflate(R.layout.fragment_gallery, container, false);
		final TextView textView = root.findViewById(R.id.text_gallery);
		mGalleryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
		return root;
	}
}
