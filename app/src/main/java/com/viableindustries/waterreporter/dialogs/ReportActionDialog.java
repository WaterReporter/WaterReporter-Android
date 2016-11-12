package com.viableindustries.waterreporter.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import com.viableindustries.waterreporter.R;

/**
 * Created by brendanmcintyre on 11/11/16.
 */

public class ReportActionDialog extends android.support.v4.app.DialogFragment {

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setItems(R.array.report_action_options, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // The 'which' argument contains the index position
                // of the selected item
                CommentActionDialogListener activity = (CommentActionDialogListener) getActivity();

                activity.onSelectAction(which);

            }
        });

        // Create the AlertDialog object and return it
        return builder.create();

    }

}
