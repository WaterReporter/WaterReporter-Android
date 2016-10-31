package com.viableindustries.waterreporter.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.viableindustries.waterreporter.R;

/**
 * Created by brendanmcintyre on 10/31/16.
 */

public class CommentActionDialog extends android.support.v4.app.DialogFragment {

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setTitle(R.string.pick_color)
        builder.setItems(R.array.comment_photo_options, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // The 'which' argument contains the index position
                // of the selected item
                CommentPhotoDialogListener activity = (CommentPhotoDialogListener) getActivity();

                activity.onReturnValue(which);

            }
        });

        // Create the AlertDialog object and return it
        return builder.create();
    }

}
