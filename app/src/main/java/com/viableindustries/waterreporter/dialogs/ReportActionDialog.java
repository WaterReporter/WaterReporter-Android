package com.viableindustries.waterreporter.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.viableindustries.waterreporter.PhotoMetaActivity;
import com.viableindustries.waterreporter.R;
import com.viableindustries.waterreporter.TimelineAdapterHelpers;
import com.viableindustries.waterreporter.UserProfileActivity;
import com.viableindustries.waterreporter.data.Geometry;
import com.viableindustries.waterreporter.data.HtmlCompat;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportHolder;

import java.util.List;

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

        view.findViewById(R.id.actionSaveImage).setOnClickListener(this);
        view.findViewById(R.id.actionGetDirections).setOnClickListener(this);

        switch (layoutType) {

            case 1:

                // Set click listeners on member views
                view.findViewById(R.id.actionEditPost).setOnClickListener(this);
                view.findViewById(R.id.actionDeletePost).setOnClickListener(this);

                break;

            default:

                LinearLayout actionEditPost = (LinearLayout) view.findViewById(R.id.actionEditPost);
                actionEditPost.setVisibility(View.GONE);

                LinearLayout actionDeletePost = (LinearLayout) view.findViewById(R.id.actionDeletePost);
                actionDeletePost.setVisibility(View.GONE);

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

            case R.id.actionGetDirections:

                getDirections(context, post);

                dismiss();

                break;

            case R.id.actionSaveImage:

                TimelineAdapterHelpers.saveImage(context, post);

                dismiss();

                break;

            default:

                break;

        }

    }

    private void getDirections(Context context, Report post) {

        // Retrieve post location

        Geometry geometry = post.geometry.geometries.get(0);

        // Build the intent
        Uri location = Uri.parse(String.format("google.navigation:q=%s,%s", geometry.coordinates.get(1), geometry.coordinates.get(0)));

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, location);

        // Verify that the map intent resolves
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(mapIntent, 0);
        boolean isIntentSafe = activities.size() > 0;

        // If safe, dispatch map intent
        if (isIntentSafe) {
            context.startActivity(mapIntent);
        }

    }

}
