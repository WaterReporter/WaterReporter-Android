package com.viableindustries.waterreporter.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.viableindustries.waterreporter.R;
import com.viableindustries.waterreporter.data.HtmlCompat;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportHolder;

/**
 * Created by brendanmcintyre on 10/31/16.
 */

public class CommentActionDialog extends DialogFragment {

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Report report = ReportHolder.getReport();

        Resources res = getResources();

        String[] options;

        CharSequence[] renders = new CharSequence[2];

        if ("open".equals(report.properties.state)) {

            options = res.getStringArray(R.array.comment_action_options_close);

        } else {

            options = res.getStringArray(R.array.comment_action_options_open);

        }

        for (int i = 0; i < options.length; i++) {

            renders[i] = HtmlCompat.fromHtml(options[i]);

        }

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
