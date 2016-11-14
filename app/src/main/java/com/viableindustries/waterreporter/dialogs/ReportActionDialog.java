package com.viableindustries.waterreporter.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.viableindustries.waterreporter.R;
import com.viableindustries.waterreporter.data.HtmlCompat;

/**
 * Created by brendanmcintyre on 11/11/16.
 */

public class ReportActionDialog extends DialogFragment {

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Resources res = getResources();

        String[] options = res.getStringArray(R.array.report_action_options);

        CharSequence[] renders = new CharSequence[2];

        for (int i = 0; i < options.length; i++) {

            renders[i] = HtmlCompat.fromHtml(options[i]);

        }

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setItems(renders, new DialogInterface.OnClickListener() {
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
