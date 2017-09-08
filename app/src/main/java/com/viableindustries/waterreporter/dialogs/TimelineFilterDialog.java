package com.viableindustries.waterreporter.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.viableindustries.waterreporter.R;

/**
 * Created by brendanmcintyre on 9/8/17.
 */

public class TimelineFilterDialog extends DialogFragment implements View.OnClickListener {

    public interface TimelineFilterDialogCallback {

        void filterAll();

        void filterOnActions();

        void filterOnLikes();

        void filterOnComments();

        void filterOnStories();

    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        super.onCreateDialog(savedInstanceState);

        View view;

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        view = inflater.inflate(R.layout.timeline_filter_dialog, null);

        // Set click listeners on member views
        view.findViewById(R.id.filterAll).setOnClickListener(this);
        view.findViewById(R.id.filterActions).setOnClickListener(this);
        view.findViewById(R.id.filterLikes).setOnClickListener(this);
        view.findViewById(R.id.filterComments).setOnClickListener(this);
        view.findViewById(R.id.filterStories).setOnClickListener(this);

        // Set dialog view
        builder.setView(view);

        // Create the AlertDialog object and return it
        return builder.create();

    }

    @Override
    public void onClick(View view) {

        final Context context = getContext();

        switch (view.getId()) {

            case R.id.filterAll:

                ((TimelineFilterDialog.TimelineFilterDialogCallback) context).filterAll();

                dismiss();

                break;

            case R.id.filterActions:

                ((TimelineFilterDialog.TimelineFilterDialogCallback) context).filterOnActions();

                dismiss();

                break;

            case R.id.filterLikes:

                ((TimelineFilterDialog.TimelineFilterDialogCallback) context).filterOnLikes();

                dismiss();

                break;

            case R.id.filterComments:

                ((TimelineFilterDialog.TimelineFilterDialogCallback) context).filterOnComments();

                dismiss();

                break;

            case R.id.filterStories:

                ((TimelineFilterDialog.TimelineFilterDialogCallback) context).filterOnStories();

                dismiss();

                break;

            default:

                break;

        }

    }

}
