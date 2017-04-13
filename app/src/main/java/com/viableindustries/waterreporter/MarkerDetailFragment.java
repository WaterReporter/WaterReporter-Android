//package com.viableindustries.waterreporter;
//
//import android.app.Activity;
//import android.app.Fragment;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.content.pm.ResolveInfo;
//import android.net.Uri;
//import android.os.Bundle;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import com.squareup.picasso.Picasso;
//import com.viableindustries.waterreporter.data.Geometry;
//import com.viableindustries.waterreporter.data.Organization;
//import com.viableindustries.waterreporter.data.OrganizationProfileListener;
//import com.viableindustries.waterreporter.data.Report;
//import com.viableindustries.waterreporter.data.ReportHolder;
//import com.viableindustries.waterreporter.data.ReportPhoto;
//import com.viableindustries.waterreporter.data.UserProfileListener;
//
//import java.util.List;
//
//import butterknife.Bind;
//
///**
// * Created by brendanmcintyre on 8/8/16.
// */
//
//public class MarkerDetailFragment extends Fragment {
//
//    // The onCreateView method is called when Fragment should create its View object hierarchy,
//    // either dynamically or via XML layout inflation.
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
//        // Defines the xml file for the fragment
//        return inflater.inflate(R.layout.marker_detail_fragment, parent, false);
//
//    }
//
//    // This event is triggered soon after onCreateView().
//    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
//    @Override
//    public void onViewCreated(View view, Bundle savedInstanceState) {
//
//        String creationDate;
//
//        Integer featureId;
//
//        String imagePath;
//
//        String watershedName;
//
//        List<String> groups;
//
//        String groupList;
//
//        String commentCount;
//
//        final Activity activity = getActivity();
//
//        final Report feature = ReportHolder.getReport();
//
//        // Set up handles to view objects
//
//        TextView reportDate = (TextView) view.findViewById(R.id.report_date);
//        TextView reportOwner = (TextView) view.findViewById(R.id.report_owner);
//        TextView reportWatershed = (TextView) view.findViewById(R.id.report_watershed);
//        TextView reportComments = (TextView) view.findViewById(R.id.comment_count);
//        TextView reportCaption = (TextView) view.findViewById(R.id.report_caption);
//        ImageView ownerAvatar = (ImageView) view.findViewById(R.id.owner_avatar);
//        LinearLayout reportGroups = (LinearLayout) view.findViewById(R.id.reportGroups);
//        ImageView reportThumb = (ImageView) view.findViewById(R.id.report_thumb);
//        RelativeLayout actionBadge = (RelativeLayout) view.findViewById(R.id.action_badge);
//        LinearLayout reportStub = (LinearLayout) view.findViewById(R.id.report_stub);
//        RelativeLayout locationIcon = (RelativeLayout) view.findViewById(R.id.location_icon);
//        RelativeLayout directionsIcon = (RelativeLayout) view.findViewById(R.id.directions_icon);
//        RelativeLayout commentIcon = (RelativeLayout) view.findViewById(R.id.comment_icon);
//
//        ReportPhoto image = (ReportPhoto) feature.properties.images.get(0);
//
//        imagePath = (String) image.properties.square_retina;
//
//        creationDate = (String) AttributeTransformUtility.relativeTime(feature.properties.created);
//
//        featureId = (Integer) feature.id;
//
//        // Extract watershed name, if any
//        watershedName = AttributeTransformUtility.parseWatershedName(feature.properties.territory);
//
//        // Extract group names, if any
//        groupList = AttributeTransformUtility.groupListSize(feature.properties.groups);
//
//        // Attach click listeners to active UI components
//
//        commentIcon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                ReportHolder.setReport(feature);
//
//                Intent intent = new Intent(activity, CommentActivity.class);
//
//                activity.startActivity(intent);
//
//            }
//        });
//
//        directionsIcon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Geometry geometry = feature.geometry.geometries.get(0);
//
//                Log.d("geometry", geometry.toString());
//
//                // Build the intent
//                Uri location = Uri.parse(String.format("google.navigation:q=%s,%s", geometry.coordinates.get(1), geometry.coordinates.get(0)));
//
//                Intent mapIntent = new Intent(Intent.ACTION_VIEW, location);
//
//                // Verify it resolves
//                PackageManager packageManager = activity.getPackageManager();
//                List<ResolveInfo> activities = packageManager.queryIntentActivities(mapIntent, 0);
//                boolean isIntentSafe = activities.size() > 0;
//
//                // Start an activity if it's safe
//                if (isIntentSafe) {
//                    activity.startActivity(mapIntent);
//                }
//
//            }
//        });
//
//        // Populate the data into the template view using the data object
//        reportDate.setText(creationDate);
//        reportOwner.setText(String.format("%s %s", feature.properties.owner.properties.first_name, feature.properties.owner.properties.last_name));
//        reportWatershed.setText(watershedName);
//
//        if (feature.properties.report_description != null && (feature.properties.report_description.length() > 0)) {
//
//            reportCaption.setVisibility(View.VISIBLE);
//
//            reportCaption.setText(feature.properties.report_description.trim());
//
//        } else {
//
//            reportCaption.setVisibility(View.GONE);
//
//        }
//
//        // Add clickable organization views, if any
//
//        reportGroups.removeAllViews();
//
//        if (feature.properties.groups.size() > 0) {
//
//            reportGroups.setVisibility(View.VISIBLE);
//
//            for (Organization organization : feature.properties.groups) {
//
//                TextView groupName = (TextView) LayoutInflater.from(activity).inflate(R.layout.related_group_item, reportGroups, false);
//
//                groupName.setText(organization.properties.name);
//
//                groupName.setTag(organization);
//
//                groupName.setOnClickListener(new OrganizationProfileListener(activity, organization));
//
//                reportGroups.addView(groupName);
//
//            }
//
//        } else {
//
//            reportGroups.setVisibility(View.GONE);
//
//        }
//
//        // Display badge if report is closed
//        if ("closed".equals(feature.properties.state)) {
//
//            actionBadge.setVisibility(View.VISIBLE);
//
//        } else {
//
//            actionBadge.setVisibility(View.GONE);
//
//        }
//
//        // Set value of comment count string
//        commentCount = AttributeTransformUtility.countComments(feature.properties.comments);
//
//        reportComments.setText(commentCount);
//
//        // Load report image and user avatar
//
//        Log.v("url", imagePath);
//
//        Picasso.with(activity).load(feature.properties.owner.properties.picture).placeholder(R.drawable.user_avatar_placeholder).transform(new CircleTransform()).into(ownerAvatar);
//
//        Picasso.with(activity).load(imagePath).fit().centerCrop().into(reportThumb);
//
//            ownerAvatar.setOnClickListener(new UserProfileListener(activity, feature.properties.owner));
//
//            reportOwner.setOnClickListener(new UserProfileListener(activity, feature.properties.owner));
//
////        directionsIcon.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////
////                getDirections(v, latitude, longitude);
////
////            }
////        });
////
////        reportComments.setText(commentCount);
////
////        reportDate.setText(creationDate);
////        reportOwner.setText(userName);
////        reportWatershed.setText(watershedName);
////        reportCaption.setText(reportDescription);
////        reportGroups.setText(groupList);
////
////        reportComments.setText(commentCount);
////
////        Log.v("url", fullImage);
////
////        try {
////
////            Log.v("avatar", userAvatar);
////
////        } catch (NullPointerException ne) {
////
////            Log.v("avatar", "NULL");
////
////        }
////
////        Picasso.with(getActivity()).load(userAvatar).placeholder(R.drawable.user_avatar_placeholder).transform(new CircleTransform()).into(ownerAvatar);
////
////        Picasso.with(getActivity()).load(fullImage).fit().centerCrop().into(reportThumb);
////
////        // Display badge if report is closed
////        if (status.equals("closed")) {
////
////            actionBadge.setVisibility(View.VISIBLE);
////
////        } else {
////
////            actionBadge.setVisibility(View.GONE);
////
////        }
//
//    }
//
//    private void getDirections(View view, double latitude, double longitude) {
//
//        // Build the intent
//        Uri location = Uri.parse(String.format("google.navigation:q=%s,%s", latitude, longitude));
//
//        Intent mapIntent = new Intent(Intent.ACTION_VIEW, location);
//
//        // Verify it resolves
//        PackageManager packageManager = getActivity().getPackageManager();
//        List<ResolveInfo> activities = packageManager.queryIntentActivities(mapIntent, 0);
//        boolean isIntentSafe = activities.size() > 0;
//
//        // Start an activity if it's safe
//        if (isIntentSafe) {
//            startActivity(mapIntent);
//        }
//
//    }
//}
