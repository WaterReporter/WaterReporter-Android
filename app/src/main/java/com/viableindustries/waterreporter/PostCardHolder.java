package com.viableindustries.waterreporter;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.viableindustries.waterreporter.data.PostCommentListener;
import com.viableindustries.waterreporter.data.PostDirectionsListener;
import com.viableindustries.waterreporter.data.PostMapListener;
import com.viableindustries.waterreporter.data.PostShareListener;

/**
 * Created by brendanmcintyre on 8/1/17.
 */

// Provide a reference to the views for each data item
// Complex data items may need more than one view per item, and
// you provide access to all the views for a data item in a view holder

public class PostCardHolder extends RecyclerView.ViewHolder {

    LinearLayout postCard;
    TextView reportDate;
    TextView reportOwner;
    TextView reportWatershed;
    TextView reportComments;
    TextView postCaption;
    FlexboxLayout reportGroups;
    ImageView ownerAvatar;
    ImageView reportThumb;
    RelativeLayout actionBadge;
    LinearLayout reportStub;
    RelativeLayout locationIcon;
    RelativeLayout directionsIcon;
    RelativeLayout commentIcon;
    RelativeLayout favoriteIcon;
    RelativeLayout shareIcon;
    RelativeLayout actionsEllipsis;
    ImageView commentIconView;
    ImageView favoriteIconView;
    TextView favoriteCounter;
    TextView tracker;

    // Open Graph

    CardView openGraphData;
    ImageView ogImage;
    TextView ogTitle;
    TextView ogDescription;
    TextView ogUrl;

    public PostCardHolder(LinearLayout v) {

        super(v);
        postCard = v;

        reportDate = (TextView) v.findViewById(R.id.report_date);
        reportOwner = (TextView) v.findViewById(R.id.report_owner);
        reportWatershed = (TextView) v.findViewById(R.id.report_watershed);
        reportComments = (TextView) v.findViewById(R.id.comment_count);
        postCaption = (TextView) v.findViewById(R.id.postCaption);
        ownerAvatar = (ImageView) v.findViewById(R.id.owner_avatar);
        reportGroups = (FlexboxLayout) v.findViewById(R.id.report_groups);
        reportThumb = (ImageView) v.findViewById(R.id.report_thumb);
        actionBadge = (RelativeLayout) v.findViewById(R.id.action_badge);
        reportStub = (LinearLayout) v.findViewById(R.id.report_stub);
        locationIcon = (RelativeLayout) v.findViewById(R.id.location_icon);
        directionsIcon = (RelativeLayout) v.findViewById(R.id.directions_icon);
        commentIcon = (RelativeLayout) v.findViewById(R.id.comment_icon);
        favoriteIcon = (RelativeLayout) v.findViewById(R.id.favorite_icon);
        shareIcon = (RelativeLayout) v.findViewById(R.id.share_icon);
        actionsEllipsis = (RelativeLayout) v.findViewById(R.id.action_ellipsis);
        commentIconView = (ImageView) v.findViewById(R.id.commentIconView);
        favoriteIconView = (ImageView) v.findViewById(R.id.favoriteIconView);
        favoriteCounter = (TextView) v.findViewById(R.id.favorite_count);
        tracker = (TextView) v.findViewById(R.id.tracker);

        // Open Graph

        openGraphData = (CardView) v.findViewById(R.id.ogData);
        ogImage = (ImageView) v.findViewById(R.id.ogImage);
        ogTitle = (TextView) v.findViewById(R.id.ogTitle);
        ogDescription = (TextView) v.findViewById(R.id.ogDescription);
        ogUrl = (TextView) v.findViewById(R.id.ogUrl);

    }

}
