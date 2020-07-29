package de.jeisfeld.lut.app.util;

import java.util.Locale;

import javax.annotation.Nonnull;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import de.jeisfeld.lut.app.Application;
import de.jeisfeld.lut.app.R;
import de.jeisfeld.lut.app.util.DialogUtil.ConfirmDialogFragment.ConfirmDialogListener;

/**
 * Helper class to show standard dialogs.
 */
public final class DialogUtil {
	/**
	 * Parameter to pass the title resource to the DialogFragment.
	 */
	private static final String PARAM_TITLE_RESOURCE = "titleResource";
	/**
	 * Parameter to pass the message to the DialogFragment.
	 */
	private static final String PARAM_MESSAGE = "message";
	/**
	 * Parameter to pass the text resource for the confirmation button to the ConfirmDialogFragment.
	 */
	private static final String PARAM_CONFIRM_BUTTON_RESOURCE = "confirmButtonResource";
	/**
	 * Parameter to pass the text resource for the cancellation button to the ConfirmDialogFragment.
	 */
	private static final String PARAM_CANCEL_BUTTON_RESOURCE = "cancelButtonResource";
	/**
	 * Instance state flag indicating if a dialog should not be recreated after orientation change.
	 */
	private static final String PREVENT_RECREATION = "preventRecreation";

	/**
	 * Hide default constructor.
	 */
	private DialogUtil() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Display a toast.
	 *
	 * @param context the current activity or context
	 * @param resource the message resource
	 * @param args arguments for the error message
	 */
	public static void displayToast(final Context context, final int resource, final Object... args) {
		try {
			final String message = capitalizeFirst(context.getString(resource, args));
			if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
				Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
			}
			else {
				new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
			}
		}
		catch (Exception e) {
			Log.e(Application.TAG, "Error displaying toast.", e);
		}
	}

	/**
	 * Display a confirmation message asking for cancel or ok.
	 *
	 * @param activity the current activity
	 * @param listener The listener waiting for the response
	 * @param titleResource the title of the confirmation dialog
	 * @param cancelButtonResource the display on the negative button
	 * @param confirmButtonResource the display on the positive button
	 * @param messageResource the confirmation message
	 * @param args arguments for the confirmation message
	 */
	public static void displayConfirmationMessage(final FragmentActivity activity, final ConfirmDialogListener listener,
			final Integer titleResource, final Integer cancelButtonResource, final int confirmButtonResource,
			final int messageResource, final Object... args) {
		String message = capitalizeFirst(activity.getString(messageResource, args));
		Bundle bundle = new Bundle();
		bundle.putCharSequence(PARAM_MESSAGE, message);
		if (cancelButtonResource != null) {
			bundle.putInt(PARAM_CANCEL_BUTTON_RESOURCE, cancelButtonResource);
		}
		bundle.putInt(PARAM_CONFIRM_BUTTON_RESOURCE, confirmButtonResource);
		bundle.putInt(PARAM_TITLE_RESOURCE, titleResource == null ? R.string.title_dialog_confirmation : titleResource);
		ConfirmDialogFragment fragment = new ConfirmDialogFragment();
		fragment.setListener(listener);
		fragment.setArguments(bundle);
		fragment.show(activity.getSupportFragmentManager(), fragment.getClass().toString());
	}

	/**
	 * Display a confirmation message asking for cancel or ok.
	 *
	 * @param activity the current activity
	 * @param titleResource the title of the confirmation dialog
	 * @param messageResource the confirmation message
	 * @param args arguments for the confirmation message
	 */
	public static void displayConfirmationMessage(final FragmentActivity activity, final Integer titleResource,
			final int messageResource, final Object... args) {
		displayConfirmationMessage(activity, null, titleResource, null, R.string.button_ok, messageResource, args);
	}

	/**
	 * Capitalize the first letter of a String.
	 *
	 * @param input The input String
	 * @return The same string with the first letter capitalized.
	 */
	public static String capitalizeFirst(final String input) {
		if (input == null || input.length() == 0) {
			return input;
		}
		else {
			return input.substring(0, 1).toUpperCase(Locale.getDefault()) + input.substring(1);
		}
	}

	/**
	 * Fragment to display a confirmation message.
	 */
	public static class ConfirmDialogFragment extends DialogFragment {
		/**
		 * The listener called when the dialog is ended.
		 */
		private ConfirmDialogListener mListener = null;

		/**
		 * Set the listener.
		 *
		 * @param listener the listener.
		 */
		public final void setListener(final ConfirmDialogListener listener) {
			mListener = listener;
		}

		@Override
		@Nonnull
		public final Dialog onCreateDialog(final Bundle savedInstanceState) {
			assert getArguments() != null;
			final CharSequence message = getArguments().getCharSequence(PARAM_MESSAGE);
			final int cancelButtonResource = getArguments().getInt(PARAM_CANCEL_BUTTON_RESOURCE);
			final int confirmButtonResource = getArguments().getInt(PARAM_CONFIRM_BUTTON_RESOURCE);
			final int titleResource = getArguments().getInt(PARAM_TITLE_RESOURCE);

			View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_confirmation, null);
			((TextView) view.findViewById(R.id.textViewConfirmationMessage)).setText(message);

			// Listeners cannot retain functionality when automatically recreated.
			// Therefore, dialogs with listeners must be re-created by the activity on orientation change.
			boolean preventRecreation = false;
			if (savedInstanceState != null) {
				preventRecreation = savedInstanceState.getBoolean(PREVENT_RECREATION);
			}
			if (preventRecreation) {
				mListener = null;
				dismiss();
			}

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(titleResource) //
					.setIcon(R.drawable.ic_warning) //
					.setView(view) //
					.setPositiveButton(confirmButtonResource, (dialog, id) -> {
						// Send the negative button event back to the host activity
						if (mListener != null) {
							mListener.onDialogPositiveClick(ConfirmDialogFragment.this);
						}
					});

			if (cancelButtonResource > 0) {
				builder.setNegativeButton(cancelButtonResource, (dialog, id) -> {
					// Send the positive button event back to the host activity
					if (mListener != null) {
						mListener.onDialogNegativeClick(ConfirmDialogFragment.this);
					}
				});
			}

			return builder.create();
		}

		@Override
		public final void onCancel(@Nonnull final DialogInterface dialogInterface) {
			if (mListener != null) {
				mListener.onDialogNegativeClick(ConfirmDialogFragment.this);
			}
			super.onCancel(dialogInterface);
		}

		@Override
		public final void onSaveInstanceState(@Nonnull final Bundle outState) {
			if (mListener != null) {
				// Typically cannot serialize the listener due to its reference to the activity.
				mListener = null;
				outState.putBoolean(PREVENT_RECREATION, true);
			}
			super.onSaveInstanceState(outState);
		}

		/**
		 * A callback handler for the dialog.
		 */
		public interface ConfirmDialogListener {
			/**
			 * Callback method for positive click from the confirmation dialog.
			 *
			 * @param dialog the confirmation dialog fragment.
			 */
			void onDialogPositiveClick(DialogFragment dialog);

			/**
			 * Callback method for negative click from the confirmation dialog.
			 *
			 * @param dialog the confirmation dialog fragment.
			 */
			default void onDialogNegativeClick(final DialogFragment dialog) {
				// do nothing
			}
		}
	}
}
