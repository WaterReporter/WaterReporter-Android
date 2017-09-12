package com.viableindustries.waterreporter.user_interface.adapters;

import android.support.v7.widget.CardView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;

/**
 * Created by brendanmcintyre on 9/11/17.
 */

public class TimelineItemViewHolder {

    public LinearLayout postHeader;
    public TextView postDate;
    public TextView postOwner;
    public TextView postWatershed;
    public TextView postCaption;
    public FlexboxLayout postGroups;
    public ImageView ownerAvatar;
    public ImageView postThumb;
    public RelativeLayout actionBadge;
    public LinearLayout postStub;
    public RelativeLayout locationIcon;

    public FlexboxLayout commentIcon;
    public FlexboxLayout favoriteIcon;
    public RelativeLayout shareIcon;
    public RelativeLayout actionsEllipsis;
    public ImageView locationIconView;
    public ImageView extraActionsIconView;
    public ImageView shareIconView;

    // Action counts

    public TextView abbrFavoriteCount;
    public ImageView favoriteIconView;

    public TextView abbrCommentCount;
    public ImageView commentIconView;

    public TextView tracker;

    // Open Graph

    public CardView openGraphData;
    public ImageView ogImage;
    public TextView ogTitle;
    public TextView ogDescription;
    public TextView ogUrl;

}
