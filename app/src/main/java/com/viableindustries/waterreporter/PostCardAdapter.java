package com.viableindustries.waterreporter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.flexbox.FlexboxLayout;
import com.squareup.picasso.Picasso;
import com.viableindustries.waterreporter.data.Favorite;
import com.viableindustries.waterreporter.data.FavoritePostBody;
import com.viableindustries.waterreporter.data.FavoriteService;
import com.viableindustries.waterreporter.data.Geometry;
import com.viableindustries.waterreporter.data.HtmlCompat;
import com.viableindustries.waterreporter.data.OpenGraph;
import com.viableindustries.waterreporter.data.OpenGraphObject;
import com.viableindustries.waterreporter.data.Organization;
import com.viableindustries.waterreporter.data.OrganizationProfileListener;
import com.viableindustries.waterreporter.data.Report;
import com.viableindustries.waterreporter.data.ReportHolder;
import com.viableindustries.waterreporter.data.ReportPhoto;
import com.viableindustries.waterreporter.data.TerritoryProfileListener;
import com.viableindustries.waterreporter.data.UserHolder;
import com.viableindustries.waterreporter.data.UserProfileListener;
import com.viableindustries.waterreporter.dialogs.ReportActionDialogListener;

import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.content.Context.MODE_PRIVATE;
import static com.viableindustries.waterreporter.R.id.parent;

/**
 * Created by brendanmcintyre on 8/1/17.
 */

public class PostCardAdapter extends RecyclerView.Adapter<PostCardAdapter.ViewHolder> {

    private List<Report> mDataset;

    private Context mContext;
    
    private SharedPreferences mPreferences;

    private boolean mIsProfile;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public LinearLayout postCard;
        public TextView reportDate;
        public TextView reportOwner;
        public TextView reportWatershed;
        public TextView reportComments;
        public TextView postCaption;
        public FlexboxLayout reportGroups;
        public ImageView ownerAvatar;
        public ImageView reportThumb;
        public RelativeLayout actionBadge;
        public LinearLayout reportStub;
        public RelativeLayout locationIcon;
        public RelativeLayout directionsIcon;
        public  RelativeLayout commentIcon;
        public RelativeLayout favoriteIcon;
        public RelativeLayout shareIcon;
        public RelativeLayout actionsEllipsis;
        public ImageView commentIconView;
        public ImageView favoriteIconView;
        public TextView favoriteCounter;
        public TextView tracker;

        // Open Graph

        public CardView openGraphData;
        public ImageView ogImage;
        public TextView ogTitle;
        public TextView ogDescription;
        public TextView ogUrl;

        public ViewHolder(LinearLayout v) {
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

    // Provide a suitable constructor (depends on the kind of dataset)
    public PostCardAdapter(Activity activity, List<Report> reports, boolean isProfile) {
        mDataset = reports;
        mContext = activity;
        mPreferences = mContext.getSharedPreferences(mContext.getPackageName(), 0);
        mIsProfile = isProfile;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PostCardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                           int viewType) {
        // create a new view
        LinearLayout v = (LinearLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.timeline_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
//        ...
        PostCardAdapter.ViewHolder vh = new PostCardAdapter.ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(PostCardAdapter.ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final Report post = mDataset.get(position);


        boolean openGraphOnly = true;

        Log.d("target-report", post.properties.toString());

        String creationDate = (String) AttributeTransformUtility.relativeTime(post.properties.created);

        holder.tracker.setText(String.valueOf(post.id));

        // Extract watershed name, if any
        String watershedName = AttributeTransformUtility.parseWatershedName(post.properties.territory);

        // Extract group names, if any
        String groupList = AttributeTransformUtility.groupListSize(post.properties.groups);

        // Attach click listeners to active UI components

        holder.commentIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ReportHolder.setReport(post);

                Intent intent = new Intent(mContext, CommentActivity.class);

                mContext.startActivity(intent);

            }
        });

        final PostCardAdapter.ViewHolder mHolder = holder;

        holder.favoriteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Retrieve authenticated user's ID

                int authUserId = mPreferences.getInt("user_id", 0);

                // Loop over this post's list of favorites

                for (Favorite favorite : post.properties.favorites) {

                    // The can only be one favorite per use per post,
                    // so if the `owner_id` matches the authenticated
                    // user's ID, target that favorite for removal.

                    if (favorite.properties.owner_id == authUserId) {

                        undoFavorite(post,
                                favorite.properties.id,
                                post.properties.favorites.size(),
                                mHolder.favoriteCounter,
                                mHolder.favoriteIconView);

                        return;

                    }

                }

                addFavorite(mHolder.getAdapterPosition(), post, post.properties.favorites.size(), mHolder.favoriteCounter, mHolder.favoriteIconView);

            }
        });

        holder.actionBadge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ReportHolder.setReport(post);

                Intent intent = new Intent(mContext, CommentActivity.class);

                mContext.startActivity(intent);

            }
        });


        holder.locationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ReportHolder.setReport(post);

                Intent intent = new Intent(mContext, MapDetailActivity.class);

                mContext.startActivity(intent);

            }
        });

        holder.directionsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Geometry geometry = post.geometry.geometries.get(0);

                Log.d("geometry", geometry.toString());

                // Build the intent
                Uri location = Uri.parse(String.format("google.navigation:q=%s,%s", geometry.coordinates.get(1), geometry.coordinates.get(0)));

                Intent mapIntent = new Intent(Intent.ACTION_VIEW, location);

                // Verify it resolves
                PackageManager packageManager = mContext.getPackageManager();
                List<ResolveInfo> activities = packageManager.queryIntentActivities(mapIntent, 0);
                boolean isIntentSafe = activities.size() > 0;

                // Start an activity if it's safe
                if (isIntentSafe) {
                    mContext.startActivity(mapIntent);
                }

            }
        });

        // Allow user to share report content on Facebook/Twitter
        // if either or both of those applications is installed

        holder.reportThumb.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {

                presentShareDialog(post);

                return true;

            }

        });

        holder.shareIcon.setVisibility(View.VISIBLE);

        holder.shareIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                presentShareDialog(post);

            }
        });

        // Populate the data into the template view using the data object
        holder.reportDate.setText(creationDate);
        holder.reportOwner.setText(String.format("%s %s", post.properties.owner.properties.first_name, post.properties.owner.properties.last_name));

        // Display watershed name and add click listener if
        // a valid territory object is present

        holder.reportWatershed.setText(watershedName);

        holder.reportWatershed.setOnClickListener(new TerritoryProfileListener(mContext, post.properties.territory));

        // Display post caption, if any

        if (post.properties.report_description != null && (post.properties.report_description.length() > 0)) {

            openGraphOnly = false;

            holder.postCaption.setText(post.properties.report_description.trim());

            holder.postCaption.setVisibility(View.VISIBLE);

            new PatternEditableBuilder().
                    addPattern(mContext, Pattern.compile("\\#(\\w+)"), ContextCompat.getColor(mContext, R.color.waterreporter_blue),
                            new PatternEditableBuilder.SpannableClickedListener() {
                                @Override
                                public void onSpanClicked(String text) {

                                    Intent intent = new Intent(mContext, TagProfileActivity.class);
                                    intent.putExtra("tag", text);
                                    mContext.startActivity(intent);

                                }
                            }).into(holder.postCaption);

        } else {

            holder.postCaption.setText("");

            holder.postCaption.setVisibility(View.GONE);

        }

        // Add clickable organization views, if any

        holder.reportGroups.setVisibility(View.VISIBLE);

        holder.reportGroups.removeAllViews();

        if (post.properties.groups.size() > 0) {

            for (Organization organization : post.properties.groups) {

                ImageView groupView = (ImageView) LayoutInflater.from(mContext).inflate(R.layout.related_group_item, holder.postCard, false);

                Picasso.with(mContext).load(organization.properties.picture).placeholder(R.drawable.user_avatar_placeholder_003).transform(new CircleTransform()).into(groupView);

                groupView.setTag(organization);

                groupView.setOnClickListener(new OrganizationProfileListener(mContext, organization));

                holder.reportGroups.addView(groupView);

            }

        } else {

            holder.reportGroups.setVisibility(View.GONE);

        }

        // Display badge if report is closed
        if ("closed".equals(post.properties.state)) {

            holder.actionBadge.setVisibility(View.VISIBLE);

        } else {

            holder.actionBadge.setVisibility(View.GONE);

        }

        // Set value of comment count string
        int commentCount = post.properties.comments.size();

        if (commentCount > 0) {

            holder.reportComments.setText(String.valueOf(commentCount));

            // Make comment icon opaque

            holder.commentIconView.setAlpha(1.0f);

            // Change comment icon color

            Drawable myIcon = ContextCompat.getDrawable(mContext, R.drawable.ic_mode_comment_black_24dp);
            myIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.splash_blue), PorterDuff.Mode.SRC_ATOP);
            holder.commentIconView.setImageDrawable(myIcon);

        } else {

            holder.reportComments.setText("");

            // Revert comment icon opacity

            holder.commentIconView.setAlpha(0.4f);

            // Revert comment icon color

            Drawable myIcon = ContextCompat.getDrawable(mContext, R.drawable.ic_mode_comment_black_24dp);
            myIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            holder.commentIconView.setImageDrawable(myIcon);

        }

        int favoriteCount = post.properties.favorites.size();

        if (favoriteCount > 0) {

            holder.favoriteCounter.setText(String.valueOf(favoriteCount));

            // Make favorite icon opaque

            holder.favoriteIconView.setAlpha(1.0f);

            // Change favorite icon color

            Drawable myIcon = ContextCompat.getDrawable(mContext, R.drawable.ic_favorite_black_24dp);
            myIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.favorite_red), PorterDuff.Mode.SRC_ATOP);
            holder.favoriteIconView.setImageDrawable(myIcon);

        } else {

            holder.favoriteCounter.setText("");

            // Revert favorite icon opacity

            holder.favoriteIconView.setAlpha(0.4f);

            // Revert icon color

            Drawable myIcon = ContextCompat.getDrawable(mContext, R.drawable.ic_favorite_black_24dp);
            myIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
            holder.favoriteIconView.setImageDrawable(myIcon);

        }

        // Load user avatar

        Picasso.with(mContext).load(post.properties.owner.properties.picture).placeholder(R.drawable.user_avatar_placeholder_003).transform(new CircleTransform()).into(holder.ownerAvatar);

        // Load primary post image

        if (post.properties.images.size() > 0) {

            openGraphOnly = false;

            ReportPhoto image = (ReportPhoto) post.properties.images.get(0);

            String imagePath = (String) image.properties.square_retina;

            holder.reportThumb.setVisibility(View.VISIBLE);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DeviceDimensionsHelper.getDisplayWidth(mContext));

            holder.reportThumb.setLayoutParams(layoutParams);

            Picasso.with(mContext).load(imagePath).fit().into(holder.reportThumb);

            holder.reportThumb.setTag(imagePath);

        } else {

            holder.reportThumb.setImageDrawable(null);

            holder.reportThumb.setVisibility(View.GONE);

        }

        // Display Open Graph information, if any

        if (post.properties.open_graph.size() > 0) {

            final OpenGraphObject openGraphObject = post.properties.open_graph.get(0);

            // Make CardView visible

            holder.openGraphData.setVisibility(View.VISIBLE);

            holder.openGraphData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Uri webpage = Uri.parse(openGraphObject.properties.url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                    if (intent.resolveActivity(mContext.getPackageManager()) != null) {
                        mContext.startActivity(intent);
                    }
                }
            });

            // Load image

            Picasso.with(mContext).load(openGraphObject.properties.imageUrl).fit().into(holder.ogImage);

            // Load title, description and URL

            holder.ogTitle.setText(openGraphObject.properties.openGraphTitle);

            holder.ogDescription.setText(openGraphObject.properties.description);

            try {

                holder.ogUrl.setText(OpenGraph.getDomainName(openGraphObject.properties.url));

            } catch (URISyntaxException e) {

                holder.ogUrl.setText("");

            }

            if (openGraphOnly) {

                holder.reportWatershed.setText("");

                holder.locationIcon.setOnClickListener(null);

                holder.locationIcon.setVisibility(View.GONE);

                holder.directionsIcon.setOnClickListener(null);

                holder.directionsIcon.setVisibility(View.GONE);

            } else {

                holder.locationIcon.setVisibility(View.VISIBLE);

                holder.directionsIcon.setVisibility(View.VISIBLE);

            }

        } else {

            // Hide CardView

            holder.openGraphData.setVisibility(View.GONE);

            // Reset image

            holder.ogImage.setImageDrawable(null);

            // Reset title, description and URL

            holder.ogTitle.setText("");

            holder.ogDescription.setText("");

            holder.ogUrl.setText("");

        }

        // Context-dependent configuration

        if (!mIsProfile) {

            holder.ownerAvatar.setOnClickListener(new UserProfileListener(mContext, post.properties.owner));

            holder.reportOwner.setOnClickListener(new UserProfileListener(mContext, post.properties.owner));

            holder.actionsEllipsis.setVisibility(View.GONE);

        } else {

            // Here we're inside the profile mContext

            // Even within the profile mContext, we need to account for the fact that users will
            // take action on reports that they don't own. Therefore, profile routing should be
            // enabled when viewing a person's "actions" feed. We can determine the condition by
            // comparing the transient user id stored in the UserHolder class and the `owner_id`
            // field of the current report.

            if (UserHolder.getUser().properties.id != post.properties.owner_id) {

                holder.ownerAvatar.setOnClickListener(new UserProfileListener(mContext, post.properties.owner));

                holder.reportOwner.setOnClickListener(new UserProfileListener(mContext, post.properties.owner));

            }

            // Determine whether or not we can expose the "additional actions" ellipsis for access to edit/delete.
            // This is a slightly different condition from the above because the id comparison must be against
            // the id of the authenticated user.

            if (mPreferences.getInt("user_id", 0) == post.properties.owner_id) {

                holder.actionsEllipsis.setVisibility(View.VISIBLE);

                holder.actionsEllipsis.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Resources res = mContext.getResources();

                        String[] options = res.getStringArray(R.array.post_action_options);

                        CharSequence[] renders = new CharSequence[2];

                        for (int i = 0; i < options.length; i++) {

                            renders[i] = HtmlCompat.fromHtml(options[i]);

                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

                        builder.setItems(renders, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                ReportHolder.setReport(post);

                                // The 'which' argument contains the index position
                                // of the selected item
                                ReportActionDialogListener activity = (ReportActionDialogListener) mContext;

                                activity.onSelectAction(which);

                            }
                        });

                        // Create the AlertDialog object and return it
                        builder.create().show();

                    }
                });

            } else {

                holder.actionsEllipsis.setVisibility(View.GONE);

            }

        }


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
    
    // Display the system share dialog

    private void presentShareDialog(final Report report) {

        Resources res = mContext.getResources();

        String shareUrl = res.getString(R.string.share_post_url, report.id);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, shareUrl);
        sendIntent.setType("text/plain");
        mContext.startActivity(Intent.createChooser(sendIntent, res.getText(R.string.share_report_chooser_title)));

    }
    
    // Add favorite

    private void addFavorite(final int position, final Report post, final int currentCount, final TextView textView, final ImageView imageView) {

        // Retrieve API token

        final String accessToken = mPreferences.getString("access_token", "");

        // Build request object

        FavoritePostBody favoritePostBody = new FavoritePostBody(post.id);

        FavoriteService service = FavoriteService.restAdapter.create(FavoriteService.class);

        service.addFavorite(accessToken, "application/json", favoritePostBody, new Callback<Favorite>() {

            @Override
            public void success(Favorite favorite, Response response) {

                int newCount = currentCount + 1;

                textView.setText(String.valueOf(newCount));

                // Make favorite icon opaque

                imageView.setAlpha(1.0f);

                // Change favorite icon color

                Drawable myIcon = ContextCompat.getDrawable(mContext, R.drawable.ic_favorite_black_24dp);
                myIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.favorite_red), PorterDuff.Mode.SRC_ATOP);
                imageView.setImageDrawable(myIcon);

                // Replace the target post item with the
                // updated API response object

                post.properties.favorites.add(favorite);

            }

            @Override
            public void failure(RetrofitError error) {

                Response response = error.getResponse();

                int status = response.getStatus();

                error.printStackTrace();

            }

        });

    }
    
    // Undo favorite

    private void undoFavorite(final Report post, final int favoriteId, final int currentCount, final TextView textView, final ImageView imageView) {

        // Retrieve API token

        final String accessToken = mPreferences.getString("access_token", "");

        // Build request object

        FavoriteService service = FavoriteService.restAdapter.create(FavoriteService.class);

        service.undoFavorite(accessToken, "application/json", favoriteId, new Callback<Void>() {

            @Override
            public void success(Void v, Response response) {

                if (currentCount == 1) {

                    textView.setText("");

                    // Make favorite icon opaque

                    imageView.setAlpha(0.4f);

                    // Change favorite icon color

                    Drawable myIcon = ContextCompat.getDrawable(mContext, R.drawable.ic_favorite_black_24dp);
                    myIcon.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                    imageView.setImageDrawable(myIcon);

                } else {

                    int newCount = currentCount - 1;

                    textView.setText(String.valueOf(newCount));

                }

                for (Iterator<Favorite> iter = post.properties.favorites.listIterator(); iter.hasNext(); ) {
                    Favorite favorite = iter.next();
                    if (favorite.properties.id == favoriteId) {
                        iter.remove();
                    }
                }

            }

            @Override
            public void failure(RetrofitError error) {

                Response response = error.getResponse();

                int status = response.getStatus();

                error.printStackTrace();

            }

        });

    }

}
