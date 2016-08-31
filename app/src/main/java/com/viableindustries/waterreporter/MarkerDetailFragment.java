package com.viableindustries.waterreporter;

import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.Geometry;

import java.util.List;

import butterknife.Bind;

/**
 * Created by brendanmcintyre on 8/8/16.
 */

public class MarkerDetailFragment extends Fragment {

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.marker_detail_fragment, parent, false);

    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        String reportDescription = getArguments().getString("reportDescription", "");
        String fullImage = getArguments().getString("fullImage", "");
        String creationDate = getArguments().getString("creationDate", "");
        String watershedName = getArguments().getString("watershedName", "");
        String groupList = getArguments().getString("groupList", "");
        String commentCount = getArguments().getString("commentCount", "");
        int reportId = getArguments().getInt("reportId", 0);
        String userName = getArguments().getString("userName", "");
        String userAvatar = getArguments().getString("userAvatar", null);
        String status = getArguments().getString("status", "OPEN");
        final double latitude = getArguments().getDouble("latitude", 0);
        final double longitude = getArguments().getDouble("longitude", 0);

        Log.d("reportDescription", reportDescription);
        Log.d("fullImage", fullImage);
        Log.d("creationDate", creationDate);
        Log.d("watershedName", watershedName);
        Log.d("groupList", groupList);
        Log.d("commentCount", commentCount);

        // Setup any handles to view objects here
        TextView reportDate = (TextView) view.findViewById(R.id.report_date);
        TextView reportOwner = (TextView) view.findViewById(R.id.report_owner);
        TextView reportWatershed = (TextView) view.findViewById(R.id.report_watershed);
        TextView reportComments = (TextView) view.findViewById(R.id.comment_count);
        TextView reportCaption = (TextView) view.findViewById(R.id.report_caption);
        TextView reportGroups = (TextView) view.findViewById(R.id.report_groups);
        ImageView ownerAvatar = (ImageView) view.findViewById(R.id.owner_avatar);
        ImageView reportThumb = (ImageView) view.findViewById(R.id.report_thumb);
        RelativeLayout actionBadge = (RelativeLayout) view.findViewById(R.id.action_badge);
        LinearLayout reportStub = (LinearLayout) view.findViewById(R.id.report_stub);
        final RelativeLayout locationIcon = (RelativeLayout) view.findViewById(R.id.location_icon);
        RelativeLayout directionsIcon = (RelativeLayout) view.findViewById(R.id.directions_icon);

        directionsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getDirections(v, latitude, longitude);

            }
        });

        reportComments.setText(commentCount);

        reportDate.setText(creationDate);
        reportOwner.setText(userName);
        reportWatershed.setText(watershedName);
        reportCaption.setText(reportDescription);
        reportGroups.setText(groupList);

        reportComments.setText(commentCount);

        Log.v("url", fullImage);

        try {

            Log.v("avatar", userAvatar);

        } catch (NullPointerException ne) {

            Log.v("avatar", "NULL");

        }

        Picasso.with(getActivity()).load(userAvatar).placeholder(R.drawable.user_avatar_placeholder).transform(new CircleTransform()).into(ownerAvatar);

        Picasso.with(getActivity()).load(fullImage).fit().centerCrop().into(reportThumb);

        // Display badge if report is closed
        if (status.equals("closed")) {

            actionBadge.setVisibility(View.VISIBLE);

        } else {

            actionBadge.setVisibility(View.GONE);

        }

    }

    private void getDirections(View view, double latitude, double longitude) {

        // Build the intent
        Uri location = Uri.parse(String.format("google.navigation:q=%s,%s", latitude, longitude));

        Intent mapIntent = new Intent(Intent.ACTION_VIEW, location);

        // Verify it resolves
        PackageManager packageManager = getActivity().getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(mapIntent, 0);
        boolean isIntentSafe = activities.size() > 0;

        // Start an activity if it's safe
        if (isIntentSafe) {
            startActivity(mapIntent);
        }

    }
}