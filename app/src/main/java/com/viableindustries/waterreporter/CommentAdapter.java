package com.viableindustries.waterreporter;

import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import java.util.regex.Pattern;

public class CommentAdapter extends ArrayAdapter<Comment> {

    private final Context context;

    private String creationDate;

    private Integer featureId;

    private String imagePath;

    protected String groupList;

    public CommentAdapter(Context context, List<Comment> features, boolean isProfile) {
        super(context, 0, features);
        this.context = context;
    }

    protected static class ViewHolder {
        TextView reportDate;
        TextView reportOwner;
        TextView reportCaption;
        ImageView ownerAvatar;
        ImageView postThumb;
        LinearLayout actionTaken;
        ImageView actionBadge;
        LinearLayout reportStub;
        TextView tracker;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.comment_container, parent, false);

            viewHolder = new ViewHolder();

            viewHolder.reportDate = (TextView) convertView.findViewById(R.id.post_date);
            viewHolder.reportOwner = (TextView) convertView.findViewById(R.id.post_owner);
            viewHolder.reportCaption = (TextView) convertView.findViewById(R.id.post_caption);
            viewHolder.ownerAvatar = (ImageView) convertView.findViewById(R.id.owner_avatar);
            viewHolder.postThumb = (ImageView) convertView.findViewById(R.id.postThumb);
            viewHolder.actionBadge = (ImageView) convertView.findViewById(R.id.action_badge);
            viewHolder.actionTaken = (LinearLayout) convertView.findViewById(R.id.action_taken);
            viewHolder.reportStub = (LinearLayout) convertView.findViewById(R.id.post_stub);
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

            viewHolder.postThumb.setVisibility(View.VISIBLE);

            Picasso.with(context).load(imagePath).fit().centerCrop().into(viewHolder.postThumb);

        } catch (IndexOutOfBoundsException ib) {

            viewHolder.postThumb.setVisibility(View.GONE);

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

            new PatternEditableBuilder().
                    addPattern(context, Pattern.compile("\\#(\\w+)"), ContextCompat.getColor(context, R.color.waterreporter_blue),
                            new PatternEditableBuilder.SpannableClickedListener() {
                                @Override
                                public void onSpanClicked(String text) {

                                    Intent intent = new Intent(context, TagProfileActivity.class);
                                    intent.putExtra("tag", text);
                                    context.startActivity(intent);

                                }
                            }).into(viewHolder.reportCaption);

        } else {

            viewHolder.reportCaption.setVisibility(View.GONE);

        }

        // Display badge if report is closed

        if ("closed".equals(feature.properties.post_state)) {

            viewHolder.actionTaken.setVisibility(View.VISIBLE);

        } else {

            viewHolder.actionTaken.setVisibility(View.GONE);

        }

        Picasso.with(context).load(feature.properties.owner.properties.picture).placeholder(R.drawable.user_avatar_placeholder_003).transform(new CircleTransform()).into(viewHolder.ownerAvatar);

        // Return the completed view to render on screen
        return convertView;

    }

}

