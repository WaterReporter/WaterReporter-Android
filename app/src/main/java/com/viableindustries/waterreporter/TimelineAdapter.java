package com.viableindustries.waterreporter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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
import com.viableindustries.waterreporter.data.Favorite;
import com.viableindustries.waterreporter.data.HtmlCompat;
import com.viableindustries.waterreporter.data.PostCommentListener;
import com.viableindustries.waterreporter.data.PostDirectionsListener;
import com.viableindustries.waterreporter.data.PostFavoriteCountListener;
import com.viableindustries.waterreporter.data.PostMapListener;
import com.viableindustries.waterreporter.data.PostShareListener;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportHolder;
import com.viableindustries.waterreporter.data.UserHolder;
import com.viableindustries.waterreporter.data.UserProfileListener;
import com.viableindustries.waterreporter.dialogs.ReportActionDialogListener;

import java.util.List;

public class TimelineAdapter extends ArrayAdapter<Report> {

    private final Context mContext;

    private final boolean mIsProfile;

    protected SharedPreferences mSharedPreferences;

    final private String FILE_PROVIDER_AUTHORITY = "com.viableindustries.waterreporter.fileprovider";

    public TimelineAdapter(Activity activity, List<Report> aFeatures, boolean isProfile) {
        super(activity, 0, aFeatures);
        this.mContext = activity;
        this.mIsProfile = isProfile;
        this.mSharedPreferences = mContext.getSharedPreferences(mContext.getPackageName(), 0);
    }

    protected static class ViewHolder {
        TextView postDate;
        TextView postOwner;
        TextView postWatershed;
        TextView postCaption;
        FlexboxLayout postGroups;
        ImageView ownerAvatar;
        ImageView postThumb;
        RelativeLayout actionBadge;
        LinearLayout postStub;
        RelativeLayout locationIcon;
        RelativeLayout directionsIcon;
        RelativeLayout commentIcon;
        RelativeLayout favoriteIcon;
        RelativeLayout shareIcon;
        RelativeLayout actionsEllipsis;
        ImageView locationIconView;
        ImageView directionsIconView;
        ImageView shareIconView;
        ImageView commentIconView;
        ImageView favoriteIconView;

        // Action counts

        TextView postFavorites;
        RelativeLayout favoriteCount;

        TextView postComments;
        RelativeLayout commentCount;

        TextView tracker;

        // Open Graph

        CardView openGraphData;
        ImageView ogImage;
        TextView ogTitle;
        TextView ogDescription;
        TextView ogUrl;
    }

    protected void presentShareDialog(final Report report) {

        Resources res = mContext.getResources();

        String shareUrl = res.getString(R.string.share_post_url, report.id);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareUrl);
        sendIntent.setType("text/plain");
        mContext.startActivity(Intent.createChooser(sendIntent, res.getText(R.string.share_post_chooser_title)));

    }

    private void bindData(final Report post, final Context context, final SharedPreferences sharedPreferences, final boolean mIsProfile, final ViewHolder viewHolder, final int position) {

        boolean openGraphOnly = true;

        Log.d("target-post", post.properties.toString());

        viewHolder.tracker.setText(String.valueOf(post.id));

        // Set non-binary icon colors

        ImageView[] icons = new ImageView[]{
                viewHolder.locationIconView,
                viewHolder.directionsIconView,
                viewHolder.shareIconView
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

        TimelineAdapterHelpers.setCommentState(context, post, viewHolder.commentCount, viewHolder.postComments, viewHolder.commentIconView);

        // Add clickable organization logos

        TimelineAdapterHelpers.setOrganizations(context, viewHolder.postStub, post, viewHolder.postGroups);

        // Set action badge state

        TimelineAdapterHelpers.setActionBadge(post, viewHolder.actionBadge);

        // Set favorite state

        TimelineAdapterHelpers.setFavoriteState(
                context,
                post,
                viewHolder.favoriteCount,
                viewHolder.postFavorites,
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

        // Attach click listeners to active UI components

        viewHolder.commentCount.setOnClickListener(new PostCommentListener(context, post));

        viewHolder.commentIcon.setOnClickListener(new PostCommentListener(context, post));

        viewHolder.actionBadge.setOnClickListener(new PostCommentListener(context, post));

        viewHolder.locationIcon.setOnClickListener(new PostMapListener(context, post));

        viewHolder.directionsIcon.setOnClickListener(new PostDirectionsListener(context, post));

        viewHolder.shareIcon.setOnClickListener(new PostShareListener(context, post));

        viewHolder.favoriteCount.setOnClickListener(new PostFavoriteCountListener(context, post));

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
                                viewHolder.favoriteCount,
                                viewHolder.postFavorites,
                                viewHolder.favoriteIconView,
                                context,
                                sharedPreferences);

                        return;

                    }

                }

                TimelineAdapterHelpers.addFavorite(
                        position,
                        post,
                        post.properties.favorites.size(),
                        viewHolder.favoriteCount,
                        viewHolder.postFavorites,
                        viewHolder.favoriteIconView,
                        context,
                        sharedPreferences);

            }
        });

        // Context-dependent configuration

        if (!mIsProfile) {

            viewHolder.ownerAvatar.setOnClickListener(new UserProfileListener(context, post.properties.owner));

            viewHolder.postOwner.setOnClickListener(new UserProfileListener(context, post.properties.owner));

            viewHolder.actionsEllipsis.setVisibility(View.GONE);

        } else {

            // Here we're inside the profile context

            // Even within the profile context, we need to account for the fact that users will
            // take action on posts that they don't own. Therefore, profile routing should be
            // enabled when viewing a person's "actions" feed. We can determine the condition by
            // comparing the transient user id stored in the UserHolder class and the `owner_id`
            // field of the current post.

            if (UserHolder.getUser().properties.id != post.properties.owner_id) {

                viewHolder.ownerAvatar.setOnClickListener(new UserProfileListener(context, post.properties.owner));

                viewHolder.postOwner.setOnClickListener(new UserProfileListener(context, post.properties.owner));

            }

            // Determine whether or not we can expose the "additional actions" ellipsis for access to edit/delete.
            // This is a slightly different condition from the above because the id comparison must be against
            // the id of the authenticated user.

            if (sharedPreferences.getInt("user_id", 0) == post.properties.owner_id) {

                viewHolder.actionsEllipsis.setVisibility(View.VISIBLE);

                viewHolder.actionsEllipsis.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Resources res = context.getResources();

                        String[] options = res.getStringArray(R.array.post_action_options);

                        CharSequence[] renders = new CharSequence[2];

                        for (int i = 0; i < options.length; i++) {

                            renders[i] = HtmlCompat.fromHtml(options[i]);

                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);

                        builder.setItems(renders, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                ReportHolder.setReport(post);

                                // The 'which' argument contains the index position
                                // of the selected item
                                ReportActionDialogListener activity = (ReportActionDialogListener) context;

                                activity.onSelectAction(which);

                            }
                        });

                        // Create the AlertDialog object and return it
                        builder.create().show();

                    }
                });

            } else {

                viewHolder.actionsEllipsis.setVisibility(View.GONE);

            }

        }

    }

    @Override
    @NonNull
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

        final ViewHolder viewHolder;

        if (convertView == null) {

            convertView = LayoutInflater.from(getContext()).inflate(R.layout.timeline_item, parent, false);

            viewHolder = new ViewHolder();

            viewHolder.postDate = (TextView) convertView.findViewById(R.id.post_date);
            viewHolder.postOwner = (TextView) convertView.findViewById(R.id.post_owner);
            viewHolder.postWatershed = (TextView) convertView.findViewById(R.id.post_watershed);
            viewHolder.postComments = (TextView) convertView.findViewById(R.id.comment_count);
            viewHolder.postCaption = (TextView) convertView.findViewById(R.id.postCaption);
            viewHolder.ownerAvatar = (ImageView) convertView.findViewById(R.id.owner_avatar);
            viewHolder.postGroups = (FlexboxLayout) convertView.findViewById(R.id.post_groups);
            viewHolder.postThumb = (ImageView) convertView.findViewById(R.id.postThumb);
            viewHolder.actionBadge = (RelativeLayout) convertView.findViewById(R.id.action_badge);
            viewHolder.postStub = (LinearLayout) convertView.findViewById(R.id.post_stub);
            viewHolder.locationIcon = (RelativeLayout) convertView.findViewById(R.id.location_icon);
            viewHolder.directionsIcon = (RelativeLayout) convertView.findViewById(R.id.directions_icon);
            viewHolder.commentIcon = (RelativeLayout) convertView.findViewById(R.id.comment_icon);
            viewHolder.favoriteIcon = (RelativeLayout) convertView.findViewById(R.id.favorite_icon);
            viewHolder.shareIcon = (RelativeLayout) convertView.findViewById(R.id.share_icon);
            viewHolder.actionsEllipsis = (RelativeLayout) convertView.findViewById(R.id.action_ellipsis);
            viewHolder.locationIconView = (ImageView) convertView.findViewById(R.id.locationIconView);
            viewHolder.directionsIconView = (ImageView) convertView.findViewById(R.id.directionsIconView);
            viewHolder.shareIconView = (ImageView) convertView.findViewById(R.id.shareIconView);
            viewHolder.commentIconView = (ImageView) convertView.findViewById(R.id.commentIconView);
            viewHolder.favoriteIconView = (ImageView) convertView.findViewById(R.id.favoriteIconView);

            // Action counts

            viewHolder.postFavorites = (TextView) convertView.findViewById(R.id.favoriteCountText);
            viewHolder.favoriteCount = (RelativeLayout) convertView.findViewById(R.id.favoriteCount);

            viewHolder.postComments = (TextView) convertView.findViewById(R.id.commentCountText);
            viewHolder.commentCount = (RelativeLayout) convertView.findViewById(R.id.commentCount);

            viewHolder.tracker = (TextView) convertView.findViewById(R.id.tracker);

            // Open Graph

            viewHolder.openGraphData = (CardView) convertView.findViewById(R.id.ogData);
            viewHolder.ogImage = (ImageView) convertView.findViewById(R.id.ogImage);
            viewHolder.ogTitle = (TextView) convertView.findViewById(R.id.ogTitle);
            viewHolder.ogDescription = (TextView) convertView.findViewById(R.id.ogDescription);
            viewHolder.ogUrl = (TextView) convertView.findViewById(R.id.ogUrl);

            convertView.setTag(viewHolder);

        } else {

            viewHolder = (ViewHolder) convertView.getTag();

        }

        // Get the data item for this position

        final Report feature = (Report) getItem(position);

        bindData(feature, mContext, mSharedPreferences, mIsProfile, viewHolder, position);

        return convertView;

    }

}

