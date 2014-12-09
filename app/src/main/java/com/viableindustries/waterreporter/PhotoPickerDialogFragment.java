package com.viableindustries.waterreporter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by Ryan Hamley on 10/28/14.
 * Dialog that allows user to choose whether to take a photo or choose an existing one.
 */
public class PhotoPickerDialogFragment extends DialogFragment {

    //This interface must be implemented by any Activity which launches this dialog.
    //The two methods allow the fragment to pass data directly on to the activity.
    public interface PhotoPickerDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    //Create a listener that attaches to the calling activity
    PhotoPickerDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        //Create new dialog builder to set options for the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Photo Options")
                .setIcon(R.drawable.ic_action_camera)
                .setPositiveButton("Take a photo", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //add click listeners for the two buttons to the dialog listener
                        //the listener will pass these click events on to the calling activity
                        listener.onDialogPositiveClick(PhotoPickerDialogFragment.this);
                    }
                })
                .setNegativeButton("Choose photo", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogNegativeClick(PhotoPickerDialogFragment.this);
                    }
                });

        return builder.create();
    }

    @Override
    //onAttach attaches the listener to the calling activity or
    //throws an error if something goes wrong
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            listener = (PhotoPickerDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement PhotoPickerDialogListener");
        }
    }
}
