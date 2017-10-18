package com.viableindustries.waterreporter.user_interface.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.viableindustries.waterreporter.R;
import com.viableindustries.waterreporter.api.models.favorite.Favorite;
import com.viableindustries.waterreporter.api.models.post.Report;
import com.viableindustries.waterreporter.api.models.post.ReportHolder;
import com.viableindustries.waterreporter.api.models.user.User;
import com.viableindustries.waterreporter.api.models.user.UserHolder;
import com.viableindustries.waterreporter.user_interface.dialogs.ReportActionDialog;
import com.viableindustries.waterreporter.user_interface.listeners.PostCommentListener;
import com.viableindustries.waterreporter.user_interface.listeners.PostDetailListener;
import com.viableindustries.waterreporter.user_interface.listeners.PostMapListener;
import com.viableindustries.waterreporter.user_interface.listeners.PostShareListener;
import com.viableindustries.waterreporter.user_interface.listeners.UserProfileListener;
import com.viableindustries.waterreporter.utilities.ModelStorage;

import java.util.List;

public class TimelineAdapter extends ArrayAdapter<Report> {

    private final Context mContext;

    private final boolean mIsProfile;

    private final boolean mSelfContain;

    private final SharedPreferences mSharedPreferences;

    final private FragmentManager mFragmentManager;

    final private String FILE_PROVIDER_AUTHORITY = "com.viableindustries.waterreporter.fileprovider";

    public TimelineAdapter(Activity activity,
                           List<Report> aFeatures,
                           boolean isProfile,
                           boolean selfContain,
                           FragmentManager fragmentManager) {
        super(activity, 0, aFeatures);
        this.mContext = activity;
        this.mIsProfile = isProfile;
        this.mSelfContain = selfContain;
        this.mSharedPreferences = mContext.getSharedPreferences(mContext.getPackageName(), 0);
        this.mFragmentManager = fragmentManager;
    }

    public static void bindData(final Report post,
                                final Context context,
                                final SharedPreferences sharedPreferences,
                                final FragmentManager fragmentManager,
                                final TimelineItemViewHolder viewHolder,
                                final boolean mIsProfile,
                                final boolean selfContain) {

        Log.d("target-post", post.properties.toString());

        viewHolder.tracker.setText(String.valueOf(post.id));

        // Set non-binary icon colors

        ImageView[] icons = new ImageView[]{
                viewHolder.locationIconView,
                viewHolder.shareIconView,
                viewHolder.extraActionsIconView
        };

        TimelineAdapterHelpers.setIconColors(context, icons);

        // Set date

        TimelineAdapterHelpers.setDate(post, viewHolder.postDate);

        // Set author (owner)

        TimelineAdapterHelpers.setAuthor(context, post, viewHolder.postOwner, viewHolder.ownerAvatar);

        // Set post image

        TimelineAdapterHelpers.setImage(context, post, viewHolder.postThumb);

        // Set watershed

        TimelineAdapterHelpers.setWatershed(context, post, viewHolder.postWatershed);

        // Set caption

        TimelineAdapterHelpers.setCaption(context, post, viewHolder.postCaption);

        // Set comment state

        TimelineAdapterHelpers.setCommentState(context, post, viewHolder.abbrCommentCount, viewHolder.commentIconView);

        // Add clickable organization logos

        TimelineAdapterHelpers.setOrganizations(context, viewHolder.postStub, post, viewHolder.postGroups);

        // Set action badge state

        TimelineAdapterHelpers.setActionBadge(post, viewHolder.actionBadge);

        // Set favorite state

        TimelineAdapterHelpers.setFavoriteState(
                context,
                post,
                viewHolder.abbrFavoriteCount,
                viewHolder.favoriteIconView);

        // Display Open Graph information, if any

        TimelineAdapterHelpers.setOpenGraph(
                context,
                post,
                viewHolder.openGraphData,
                viewHolder.ogImage,
                viewHolder.ogTitle,
                viewHolder.ogDescription,
                viewHolder.ogUrl);

        // Attach gesture listeners to active UI components

        // Check post context to avoid creating new instances of PostDetailActivity

        if (!selfContain) viewHolder.postThumb.setOnClickListener(new PostDetailListener(context, post));

        viewHolder.commentIcon.setOnClickListener(new PostCommentListener(context, post));

        viewHolder.actionBadge.setOnClickListener(new PostCommentListener(context, post));

        viewHolder.locationIcon.setOnClickListener(new PostMapListener(context, post));

        viewHolder.shareIcon.setOnClickListener(new PostShareListener(context, post));

        viewHolder.favoriteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Retrieve authenticated user's ID

                int authUserId = sharedPreferences.getInt("user_id", 0);

                // Loop over this post's list of favorites

                for (Favorite favorite : post.properties.favorites) {

                    // The can only be one favorite per use per post,
                    // so if the `owner_id` matches the authenticated
                    // user's ID, target that favorite for removal.

                    if (favorite.properties.owner_id == authUserId) {

                        TimelineAdapterHelpers.undoFavorite(post,
                                favorite.properties.id,
                                post.properties.favorites.size(),
                                viewHolder.abbrFavoriteCount,
                                viewHolder.favoriteIconView,
                                context,
                                sharedPreferences);

                        return;

                    }

                }

                // Prevent users from liking their own posts

                if (TimelineAdapterHelpers.ownPost(context, post)) {

                    TimelineAdapterHelpers.addFavorite(
                            post,
                            post.properties.favorites.size(),
                            viewHolder.abbrFavoriteCount,
                            viewHolder.favoriteIconView,
                            context,
                            sharedPreferences);

                }

            }
        });

        // Context-dependent configuration

        if (!mIsProfile) {

            viewHolder.ownerAvatar.setOnClickListener(new UserProfileListener(context, post.properties.owner));

            viewHolder.postOwner.setOnClickListener(new UserProfileListener(context, post.properties.owner));

        } else {

            // Here we're inside the profile context

            // Even within the profile context, we need to account for the fact that users will
            // take action on posts that they don't own. Therefore, profile routing should be
            // enabled when viewing a person's "actions" feed. We can determine the condition by
            // comparing the transient user id stored in the UserHolder class and the `owner_id`
            // field of the current post.

            User user = ModelStorage.getStoredUser(sharedPreferences);

            if (user.properties.id != post.properties.owner_id) {

                viewHolder.ownerAvatar.setOnClickListener(new UserProfileListener(context, post.properties.owner));

                viewHolder.postOwner.setOnClickListener(new UserProfileListener(context, post.properties.owner));

            }

        }

        viewHolder.actionsEllipsis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int layoutType = 0;

                // Determine whether or not we can expose the "additional actions" ellipsis for access to edit/delete.
                // This is a slightly different condition from the above because the id comparison must be against
                // the id of the authenticated user.

                if (sharedPreferences.getInt("user_id", 0) == post.properties.owner_id) {

                    layoutType = 1;

                }

                final Bundle args = new Bundle();

                args.putInt("layout_type", layoutType);

                ReportHolder.setReport(post);

                ModelStorage.storeModel(sharedPreferences, post, "stored_post");

                ReportActionDialog reportActionDialog = new ReportActionDialog();

                reportActionDialog.setArguments(args);

                reportActionDialog.show(fragmentManager, "post-action-dialog");

            }
        });

    }

    @Override
    @NonNull
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

        final TimelineItemViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.timeline_item, parent, false);

            viewHolder = new TimelineItemViewHolder();

            viewHolder.postHeader = (LinearLayout) convertView.findViewById(R.id.postHeader);
            viewHolder.postDate = (TextView) convertView.findViewById(R.id.postDate);
            viewHolder.postOwner = (TextView) convertView.findViewById(R.id.postOwner);
            viewHolder.postWatershed = (TextView) convertView.findViewById(R.id.postWatershed);
            viewHolder.postCaption = (TextView) convertView.findViewById(R.id.postCaption);
            viewHolder.ownerAvatar = (ImageView) convertView.findViewById(R.id.ownerAvatar);
            viewHolder.postGroups = (FlexboxLayout) convertView.findViewById(R.id.postGroups);
            viewHolder.postThumb = (ImageView) convertView.findViewById(R.id.postThumb);
            viewHolder.actionBadge = (RelativeLayout) convertView.findViewById(R.id.actionBadge);
            viewHolder.postStub = (LinearLayout) convertView.findViewById(R.id.postStub);
            viewHolder.locationIcon = (RelativeLayout) convertView.findViewById(R.id.locationIcon);

            viewHolder.commentIcon = (FlexboxLayout) convertView.findViewById(R.id.commentIcon);
            viewHolder.favoriteIcon = (FlexboxLayout) convertView.findViewById(R.id.favoriteIcon);
            viewHolder.shareIcon = (RelativeLayout) convertView.findViewById(R.id.shareIcon);
            viewHolder.actionsEllipsis = (RelativeLayout) convertView.findViewById(R.id.actionEllipsis);
            viewHolder.locationIconView = (ImageView) convertView.findViewById(R.id.locationIconView);

            viewHolder.shareIconView = (ImageView) convertView.findViewById(R.id.shareIconView);
            viewHolder.commentIconView = (ImageView) convertView.findViewById(R.id.commentIconView);
            viewHolder.favoriteIconView = (ImageView) convertView.findViewById(R.id.favoriteIconView);
            viewHolder.extraActionsIconView = (ImageView) convertView.findViewById(R.id.extraActionsIconView);

            // Action counts

            viewHolder.abbrFavoriteCount = (TextView) convertView.findViewById(R.id.abbrFavoriteCount);

            viewHolder.abbrCommentCount = (TextView) convertView.findViewById(R.id.abbrCommentCount);

            viewHolder.tracker = (TextView) convertView.findViewById(R.id.tracker);

            // Open Graph

            viewHolder.openGraphData = (CardView) convertView.findViewById(R.id.ogData);
            viewHolder.ogImage = (ImageView) convertView.findViewById(R.id.ogImage);
            viewHolder.ogTitle = (TextView) convertView.findViewById(R.id.ogTitle);
            viewHolder.ogDescription = (TextView) convertView.findViewById(R.id.ogDescription);
            viewHolder.ogUrl = (TextView) convertView.findViewById(R.id.ogUrl);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (TimelineItemViewHolder) convertView.getTag();

        }

        // Get the api item for this position

        final Report feature = getItem(position);

        bindData(feature,
                mContext,
                mSharedPreferences,
                mFragmentManager,
                viewHolder,
                mIsProfile,
                mSelfContain);

        return convertView;

    }

}

