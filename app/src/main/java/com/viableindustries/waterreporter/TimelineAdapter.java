package com.viableindustries.waterreporter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportPhoto;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class TimelineAdapter extends ArrayAdapter {

    private final Context context;

    protected String creationDate;

    protected Integer featureId;

    protected String imagePath;

    protected String watershedName;

    protected List<String> groups;

    protected String groupList;

    protected String commentCount;

    public TimelineAdapter(Context context, List features) {
        super(context, 0, features);
        this.context = context;
    }

    protected static class ViewHolder {
        TextView reportDate;
        TextView reportOwner;
        TextView reportWatershed;
        TextView reportComments;
        TextView reportCaption;
        TextView reportGroups;
        ImageView ownerAvatar;
        ImageView reportThumb;
        RelativeLayout actionBadge;
        LinearLayout reportStub;
//        int position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.timeline_item, parent, false);

            viewHolder = new ViewHolder();

            viewHolder.reportDate = (TextView) convertView.findViewById(R.id.report_date);
            viewHolder.reportOwner = (TextView) convertView.findViewById(R.id.report_owner);
            viewHolder.reportWatershed = (TextView) convertView.findViewById(R.id.report_watershed);
            viewHolder.reportComments = (TextView) convertView.findViewById(R.id.comment_count);
            viewHolder.reportCaption = (TextView) convertView.findViewById(R.id.report_caption);
            viewHolder.reportGroups = (TextView) convertView.findViewById(R.id.report_groups);
            viewHolder.ownerAvatar = (ImageView) convertView.findViewById(R.id.owner_avatar);
            viewHolder.reportThumb = (ImageView) convertView.findViewById(R.id.report_thumb);
            viewHolder.actionBadge = (RelativeLayout) convertView.findViewById(R.id.action_badge);
            viewHolder.reportStub = (LinearLayout) convertView.findViewById(R.id.report_stub);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (ViewHolder) convertView.getTag();

        }

        // Get the data item for this position
        Report feature = (Report) getItem(position);

        Log.d("groups", feature.properties.groups.toString());

        ReportPhoto image = (ReportPhoto) feature.properties.images.get(0);

        imagePath = (String) image.properties.square_retina;

        creationDate = (String) feature.properties.created;

        featureId = (Integer) feature.id;

        // Extract watershed name, if any

        try {

            watershedName = String.format("%s Watershed", feature.properties.territory.properties.huc_6_name);

        } catch (NullPointerException ne) {

            watershedName = "Watershed not available";

        }

        // Extract group names, if any

        if (!feature.properties.groups.isEmpty()) {

            groupList = feature.properties.groups.get(0).properties.name;

        } else {

            groupList = "This report is not affiliated with any groups.";

        }

        try {
            //create SimpleDateFormat object with source string date format
            SimpleDateFormat sdfSource = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

            //parse the string into Date object
            Date date = sdfSource.parse(creationDate);

            //create SimpleDateFormat object with desired date format
            SimpleDateFormat sdfOutput = new SimpleDateFormat("MMM dd, yyyy");

            //parse the date into another format
            creationDate = sdfOutput.format(date);

        } catch (ParseException pe) {
            System.out.println("Parse Exception : " + pe);
        }

        // Check if an existing view is being reused, otherwise inflate the view
//        if (convertView == null) {
//            convertView = LayoutInflater.from(getContext()).inflate(R.layout.timeline_item, parent, false);
//        }
        // Lookup view for data population
//        TextView reportDate = (TextView) convertView.findViewById(R.id.report_date);
//        TextView reportOwner = (TextView) convertView.findViewById(R.id.report_owner);
//        TextView reportWatershed = (TextView) convertView.findViewById(R.id.report_watershed);
//        TextView reportComments = (TextView) convertView.findViewById(R.id.comment_count);
//        TextView reportCaption = (TextView) convertView.findViewById(R.id.report_caption);
//        TextView reportGroups = (TextView) convertView.findViewById(R.id.report_groups);
//        ImageView ownerAvatar = (ImageView) convertView.findViewById(R.id.owner_avatar);
//        ImageView reportThumb = (ImageView) convertView.findViewById(R.id.report_thumb);
//        RelativeLayout actionBadge = (RelativeLayout) convertView.findViewById(R.id.action_badge);
//        LinearLayout reportStub = (LinearLayout) convertView.findViewById(R.id.report_stub);

        // Populate the data into the template view using the data object
        viewHolder.reportDate.setText(creationDate);
        viewHolder.reportOwner.setText(String.format("%s %s", feature.properties.owner.properties.first_name, feature.properties.owner.properties.last_name));
        viewHolder.reportWatershed.setText(watershedName);
        viewHolder.reportCaption.setText(feature.properties.report_description.trim());
        viewHolder.reportGroups.setText(groupList);

        // Display badge if report is closed
        if (feature.properties.state.equals("closed")) {

            viewHolder.actionBadge.setVisibility(View.VISIBLE);

        } else {

            viewHolder.actionBadge.setVisibility(View.GONE);

        }

        // Set value of comment count string
        if (feature.properties.comments.size() != 1) {

            commentCount = String.format("%s comments", feature.properties.comments.size());

        } else {

            commentCount = "1 comment";

        }

        viewHolder.reportComments.setText(commentCount);

//        viewHolder.reportStub.setTag(featureId);

        Log.v("url", imagePath);

        Picasso.with(context).load(feature.properties.owner.properties.picture).transform(new CircleTransform()).into(viewHolder.ownerAvatar);

        Picasso.with(context).load(imagePath).fit().centerCrop().into(viewHolder.reportThumb);

        // Return the completed view to render on screen
        return convertView;

    }

}

