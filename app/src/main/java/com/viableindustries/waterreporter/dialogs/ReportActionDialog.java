package com.viableindustries.waterreporter.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.viableindustries.waterreporter.PhotoMetaActivity;
import com.viableindustries.waterreporter.R;
import com.viableindustries.waterreporter.TimelineAdapterHelpers;
import com.viableindustries.waterreporter.UserProfileActivity;
import com.viableindustries.waterreporter.data.HtmlCompat;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportHolder;

/**
 * Created by brendanmcintyre on 11/11/16.
 */

public class ReportActionDialog extends DialogFragment implements View.OnClickListener {

    private int layoutType;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        super.onCreateDialog(savedInstanceState);

        View view;

        layoutType = getArguments().getInt("layout_type", 0);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        view = inflater.inflate(R.layout.post_action_dialog, null);

        switch (layoutType) {

            case 1:

                // Set click listeners on member views
                view.findViewById(R.id.actionEditPost).setOnClickListener(this);
                view.findViewById(R.id.actionDeletePost).setOnClickListener(this);

                break;

            default:

                view.findViewById(R.id.actionSaveImage).setOnClickListener(this);

                break;

        }

        // Set dialog view
        builder.setView(view);

        // Create the AlertDialog object and return it
        return builder.create();

    }

    @Override
    public void onClick(View view) {

        Context context = getContext();

        Report post = ReportHolder.getReport();

        Log.d("dialog-view-id", "" + view.getId());

        switch (view.getId()) {

            case R.id.actionEditPost:

                Intent intent = new Intent(context, PhotoMetaActivity.class);

                intent.putExtra("EDIT_MODE", true);

                context.startActivity(intent);

                dismiss();

                break;

            case R.id.actionDeletePost:

                // do something

                break;

            case R.id.actionSaveImage:

                TimelineAdapterHelpers.saveImage(context, post);

                dismiss();

                break;

            default:

                break;

        }

    }

}
