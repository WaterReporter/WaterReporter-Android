package com.viableindustries.waterreporter;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.viableindustries.waterreporter.data.Comment;
import com.viableindustries.waterreporter.data.Geometry;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationProfileListener;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportHolder;
import com.viableindustries.waterreporter.data.ReportPhoto;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.UserProfileListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends ArrayAdapter {

    private final Context context;

    private String creationDate;

    private Integer featureId;

    private String imagePath;

    protected String groupList;

    public CommentAdapter(Context context, List features, boolean isProfile) {
        super(context, 0, features);
        this.context = context;
    }

    protected static class ViewHolder {
        TextView reportDate;
        TextView reportOwner;
        TextView reportCaption;
        ImageView ownerAvatar;
        ImageView reportThumb;
        LinearLayout actionTaken;
        ImageView actionBadge;
        LinearLayout reportStub;
        TextView tracker;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.comment_container, parent, false);

            viewHolder = new ViewHolder();

            viewHolder.reportDate = (TextView) convertView.findViewById(R.id.report_date);
            viewHolder.reportOwner = (TextView) convertView.findViewById(R.id.report_owner);
            viewHolder.reportCaption = (TextView) convertView.findViewById(R.id.report_caption);
            viewHolder.ownerAvatar = (ImageView) convertView.findViewById(R.id.owner_avatar);
            viewHolder.reportThumb = (ImageView) convertView.findViewById(R.id.report_thumb);
            viewHolder.actionBadge = (ImageView) convertView.findViewById(R.id.action_badge);
            viewHolder.actionTaken = (LinearLayout) convertView.findViewById(R.id.action_taken);
            viewHolder.reportStub = (LinearLayout) convertView.findViewById(R.id.report_stub);
            viewHolder.tracker = (TextView) convertView.findViewById(R.id.tracker);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (ViewHolder) convertView.getTag();

        }

        // Get the data item for this position
        final Comment feature = (Comment) getItem(position);

        try {

            ReportPhoto image = (ReportPhoto) feature.properties.images.get(0);

            imagePath = (String) image.properties.square_retina;

            viewHolder.reportThumb.setVisibility(View.VISIBLE);

            Picasso.with(context).load(imagePath).fit().centerCrop().into(viewHolder.reportThumb);

        } catch (IndexOutOfBoundsException ib) {

            viewHolder.reportThumb.setVisibility(View.GONE);

        }

        creationDate = (String) AttributeTransformUtility.relativeTime(feature.properties.created);

        featureId = (Integer) feature.id;

        viewHolder.tracker.setText(String.valueOf(featureId));

        // Attach click listeners to active UI components

        viewHolder.ownerAvatar.setOnClickListener(new UserProfileListener(getContext(), feature.properties.owner));

        viewHolder.reportOwner.setOnClickListener(new UserProfileListener(getContext(), feature.properties.owner));

        // Populate the data into the template view using the data object
        viewHolder.reportDate.setText(creationDate);

        viewHolder.reportOwner.setText(String.format("%s %s", feature.properties.owner.properties.first_name, feature.properties.owner.properties.last_name));

        if (feature.properties.body != null && (feature.properties.body.length() > 0)) {

            viewHolder.reportCaption.setVisibility(View.VISIBLE);

            viewHolder.reportCaption.setText(feature.properties.body.trim());

        } else {

            viewHolder.reportCaption.setVisibility(View.GONE);

        }

        // Display badge if report is closed

        if ("closed".equals(feature.properties.report_state)) {

            viewHolder.actionBadge.setVisibility(View.VISIBLE);

            viewHolder.actionTaken.setVisibility(View.VISIBLE);

        } else {

            viewHolder.actionBadge.setVisibility(View.GONE);

            viewHolder.actionTaken.setVisibility(View.GONE);

        }

        Picasso.with(context).load(feature.properties.owner.properties.picture).placeholder(R.drawable.user_avatar_placeholder).transform(new CircleTransform()).into(viewHolder.ownerAvatar);

        // Return the completed view to render on screen
        return convertView;

    }

}

